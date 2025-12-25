package org.whu.timeflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.whu.timeflow.common.Result;
import org.whu.timeflow.dto.SyncDTO;
import org.whu.timeflow.entity.Bill;
import org.whu.timeflow.entity.Diary;
import org.whu.timeflow.entity.Todo;
import org.whu.timeflow.service.IBillService;
import org.whu.timeflow.service.IDiaryService;
import org.whu.timeflow.service.ITodoService;
import org.whu.timeflow.utils.UserContext;

import javax.swing.*;
import java.math.BigDecimal; // ✅ 引入 BigDecimal
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    @Autowired private IDiaryService diaryService;
    @Autowired private IBillService billService;
    @Autowired private ITodoService todoService;

    /**
     * PUSH: 接收前端的脏数据，批量写入/更新
     */
    @PostMapping("/push")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, List<String>>> push(@RequestBody SyncDTO dto) {
        String userId = UserContext.getUserId(); // 从 Token 获取
        Map<String, List<String>> successIds = new HashMap<>();

        int diaryTotal = dto.getDiaries() == null ? 0 : dto.getDiaries().size();
        int billTotal = dto.getBills() == null ? 0 : dto.getBills().size();
        int todoTotal = dto.getTodos() == null ? 0 : dto.getTodos().size();

        log.info("同步推送 开始 用户ID={} 日记数量={} 账单数量={} 待办数量={}", userId, diaryTotal, billTotal, todoTotal);

        long now = System.currentTimeMillis();

        // 1. 处理日记
        List<String> diaryIds = new ArrayList<>();
        if (dto.getDiaries() != null) {
            for (Diary d : dto.getDiaries()) {
                d.setUserId(userId);

                // 🛡️ 兜底：如果时间为空，补全为当前时间
                if (d.getCreateTime() == null) d.setCreateTime(now);
                if (d.getUpdateTime() == null) d.setUpdateTime(now);

                // 🛡️ 兜底：MyBatis-Plus 可能会忽略 null 字段，但数据库不能为 null
                // 如果 deleted 为空，默认为 0
                if (d.getIsDeleted() == null) d.setIsDeleted(0);

                if (diaryService.saveOrUpdate(d)) {
                    diaryIds.add(d.getId());
                }
            }
        }
        successIds.put("success_diary_ids", diaryIds);
        int diarySuccess = diaryIds.size();

        // 2. 处理账单
        List<String> billIds = new ArrayList<>();
        if (dto.getBills() != null) {
            for (Bill b : dto.getBills()) {
                b.setUserId(userId);

                // 🛡️ 兜底
                if (b.getCreateTime() == null) b.setCreateTime(now);
                if (b.getUpdateTime() == null) b.setUpdateTime(now);
                if (b.getIsDeleted() == null) b.setIsDeleted(0);

                // 🔥🔥🔥 新增修复：金额兜底 (适配 BigDecimal) 🔥🔥🔥
                // 防止 amount 为 null 导致 SQL 报错
                if (b.getAmount() == null) {
                    b.setAmount(BigDecimal.ZERO);
                }

                // 防止 amount 过大 (数据库定义是 DECIMAL(10,2)，最大 99999999.99)
                // 使用 BigDecimal 进行比较
                BigDecimal maxAmount = new BigDecimal("99999999.99");
                if (b.getAmount().compareTo(maxAmount) > 0) {
                    log.warn("账单金额过大，已截断: ID={} Amount={}", b.getId(), b.getAmount());
                    b.setAmount(maxAmount);
                }

                if (billService.saveOrUpdate(b)) {
                    billIds.add(b.getId());
                }
            }
        }
        successIds.put("success_bill_ids", billIds);
        int billSuccess = billIds.size();

        // 3. 处理待办
        List<String> todoIds = new ArrayList<>();
        if (dto.getTodos() != null) {
            for (Todo t : dto.getTodos()) {
                t.setUserId(userId);

                // 🛡️ 兜底
                if (t.getCreateTime() == null) t.setCreateTime(now);
                if (t.getUpdateTime() == null) t.setUpdateTime(now);
                if (t.getIsDeleted() == null) t.setIsDeleted(0);

                if (todoService.saveOrUpdate(t)) {
                    todoIds.add(t.getId());
                }
            }
        }
        successIds.put("success_todo_ids", todoIds);
        int todoSuccess = todoIds.size();

        log.info("同步推送 结果=成功 用户ID={} 日记成功/总数={}/{} 账单成功/总数={}/{} 待办成功/总数={}/{}",
                userId, diarySuccess, diaryTotal, billSuccess, billTotal, todoSuccess, todoTotal);
        return Result.success(successIds);
    }

    /**
     * PULL: 前端告诉我上次同步时间，我返回那之后的所有变动
     */
    @PostMapping("/pull")
    public Result<SyncDTO> pull(@RequestBody SyncDTO req) {
        String userId = UserContext.getUserId();
        Long lastTime = req.getLastSyncTime();
        if (lastTime == null) lastTime = 0L;
        log.info("同步拉取 开始 用户ID={} 上次同步时间={}", userId, lastTime);

        SyncDTO resp = new SyncDTO();

        // 查日记
        resp.setDiaries(diaryService.list(new LambdaQueryWrapper<Diary>()
                .eq(Diary::getUserId, userId)
                .gt(Diary::getUpdateTime, lastTime)));

        // 查账单
        resp.setBills(billService.list(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .gt(Bill::getUpdateTime, lastTime)));

        // 查待办
        resp.setTodos(todoService.list(new LambdaQueryWrapper<Todo>()
                .eq(Todo::getUserId, userId)
                .gt(Todo::getUpdateTime, lastTime)));

        int diaryCount = resp.getDiaries() == null ? 0 : resp.getDiaries().size();
        int billCount = resp.getBills() == null ? 0 : resp.getBills().size();
        int todoCount = resp.getTodos() == null ? 0 : resp.getTodos().size();
        log.info("同步拉取 结果=成功 用户ID={} 日记数量={} 账单数量={} 待办数量={}", userId, diaryCount, billCount, todoCount);

        return Result.success(resp);
    }
}