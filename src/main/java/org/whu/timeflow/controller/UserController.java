package org.whu.timeflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.whu.timeflow.common.Result;
import org.whu.timeflow.dto.UserDTO;
import org.whu.timeflow.entity.User;
import org.whu.timeflow.mapper.UserMapper;
import org.whu.timeflow.utils.JwtUtils;
import org.whu.timeflow.utils.UserContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 盐值：混在密码里，防止被彩虹表破解
    // 随便写一串复杂的字符串
    private static final String SALT = "TimeFlow_@WHU_2025_#Salt!";

    /**
     * 内部工具方法：MD5加密
     * 算法：MD5( 密码 + 盐 )
     */
    private String encrypt(String originPassword) {
        if (originPassword == null) return null;
        String base = originPassword + SALT;
        return DigestUtils.md5DigestAsHex(base.getBytes(StandardCharsets.UTF_8));
    }

    // ================= 业务接口 =================

    // 1. 注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO dto) {
        // 基础校验
        if (StringUtils.isBlank(dto.getEmail()) || StringUtils.isBlank(dto.getPassword())) {
            return Result.error("邮箱和密码不能为空");
        }

        // 查重
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (count > 0) {
            return Result.error("该邮箱已被注册");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(dto.getEmail());
        // 加密存储
        user.setPassword(encrypt(dto.getPassword()));
        user.setNickname(StringUtils.isBlank(dto.getNickname()) ? "流年用户" : dto.getNickname());

        userMapper.insert(user);
        return Result.success("注册成功");
    }

    // 2. 登录
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody UserDTO dto) {
        if (StringUtils.isBlank(dto.getEmail()) || StringUtils.isBlank(dto.getPassword())) {
            return Result.error("请输入账号密码");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, dto.getEmail()));

        // 用户不存在
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 比对密码：用同样的算法加密输入值，看和数据库里是否一样
        String inputHash = encrypt(dto.getPassword());
        if (!user.getPassword().equals(inputHash)) {
            return Result.error("密码错误"); // 生产环境通常提示“账号或密码错误”模糊具体原因，但作业可以具体点
        }

        // 生成 Token
        String token = JwtUtils.createToken(user.getId(), user.getEmail());

        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("nickname", user.getNickname());
        map.put("id", user.getId());
        map.put("email", user.getEmail());

        return Result.success(map);
    }

    // 3. 修改昵称
    @PostMapping("/update/nickname")
    public Result<String> updateNickname(@RequestBody UserDTO dto) {
        String userId = UserContext.getUserId();
        if (StringUtils.isBlank(dto.getNickname())) {
            return Result.error("昵称不能为空");
        }

        // 确认用户是否存在
        User existUser = userMapper.selectById(userId);
        if (existUser == null) return Result.error("用户不存在");

        User update = new User();
        update.setId(userId);
        update.setNickname(dto.getNickname());

        userMapper.updateById(update);
        return Result.success("昵称修改成功");
    }

    // 4. 修改密码
    @PostMapping("/update/password")
    public Result<String> updatePassword(@RequestBody UserDTO dto) {
        String userId = UserContext.getUserId();

        // 校验参数
        if (StringUtils.isBlank(dto.getOldPassword()) || StringUtils.isBlank(dto.getNewPassword())) {
            return Result.error("旧密码和新密码均不能为空");
        }

        // 1. 查出当前用户
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            return Result.error("用户异常，请重新登录");
        }

        // 2. 核心逻辑：验证旧密码是否正确
        String oldPassHash = encrypt(dto.getOldPassword());
        if (!currentUser.getPassword().equals(oldPassHash)) {
            return Result.error("旧密码错误，修改失败");
        }

        // 3. 加密新密码并更新
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(encrypt(dto.getNewPassword()));

        userMapper.updateById(updateUser);
        return Result.success("密码修改成功");
    }
}