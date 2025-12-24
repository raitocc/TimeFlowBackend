package org.whu.timeflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "用户", description = "用户账号相关接口")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

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
    @Operation(
            summary = "用户注册",
            description = "参数：email(必填)、password(必填)、nickname(可选)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "注册信息"
            )
    )
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO dto) {
        // 基础校验
        if (StringUtils.isBlank(dto.getEmail()) || StringUtils.isBlank(dto.getPassword())) {
            log.info("用户注册 结果=失败 原因=参数缺失 邮箱={}", dto.getEmail());
            return Result.error("邮箱和密码不能为空");
        }

        // 查重
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (count > 0) {
            log.info("用户注册 结果=失败 原因=邮箱已存在 邮箱={}", dto.getEmail());
            return Result.error("该邮箱已被注册");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(dto.getEmail());
        // 加密存储
        user.setPassword(encrypt(dto.getPassword()));
        user.setNickname(StringUtils.isBlank(dto.getNickname()) ? "流年用户" : dto.getNickname());

        userMapper.insert(user);
        log.info("用户注册 结果=成功 用户ID={} 邮箱={} 昵称={}", user.getId(), user.getEmail(), user.getNickname());
        return Result.success("注册成功", "注册成功");
    }

    // 2. 登录
    @Operation(
            summary = "用户登录",
            description = "参数：email(必填)、password(必填)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "登录信息"
            )
    )
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody UserDTO dto) {
        if (StringUtils.isBlank(dto.getEmail()) || StringUtils.isBlank(dto.getPassword())) {
            log.info("用户登录 结果=失败 原因=参数缺失 邮箱={}", dto.getEmail());
            return Result.error("请输入账号密码");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, dto.getEmail()));

        // 用户不存在
        if (user == null) {
            log.info("用户登录 结果=失败 原因=用户不存在 邮箱={}", dto.getEmail());
            return Result.error("用户不存在");
        }

        // 比对密码：用同样的算法加密输入值，看和数据库里是否一样
        String inputHash = encrypt(dto.getPassword());
        if (!user.getPassword().equals(inputHash)) {
            log.info("用户登录 结果=失败 原因=密码错误 用户ID={} 邮箱={}", user.getId(), user.getEmail());
            return Result.error("密码错误"); // 生产环境通常提示“账号或密码错误”模糊具体原因，但作业可以具体点
        }

        // 生成 Token
        String token = JwtUtils.createToken(user.getId(), user.getEmail());

        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("nickname", user.getNickname());
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        log.info("用户登录 结果=成功 用户ID={} 邮箱={} 昵称={}", user.getId(), user.getEmail(), user.getNickname());

        return Result.success(map, "登录成功");
    }

    // 3. 修改昵称
    @Operation(
            summary = "修改昵称",
            description = "参数：nickname(必填)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "昵称信息"
            )
    )
    @PostMapping("/update/nickname")
    public Result<String> updateNickname(@RequestBody UserDTO dto) {
        String userId = UserContext.getUserId();
        if (StringUtils.isBlank(dto.getNickname())) {
            log.info("修改昵称 结果=失败 原因=昵称为空 用户ID={}", userId);
            return Result.error("昵称不能为空");
        }

        // 确认用户是否存在
        User existUser = userMapper.selectById(userId);
        if (existUser == null) {
            log.info("修改昵称 结果=失败 原因=用户不存在 用户ID={}", userId);
        }
        if (existUser == null) return Result.error("用户不存在");

        User update = new User();
        update.setId(userId);
        update.setNickname(dto.getNickname());

        userMapper.updateById(update);
        log.info("修改昵称 结果=成功 用户ID={} 昵称={}", userId, dto.getNickname());
        return Result.success("昵称修改成功", "昵称修改成功");
    }

    // 4. 修改密码
    @Operation(
            summary = "修改密码",
            description = "参数：oldPassword(必填)、newPassword(必填)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "密码信息"
            )
    )
    @PostMapping("/update/password")
    public Result<String> updatePassword(@RequestBody UserDTO dto) {
        String userId = UserContext.getUserId();

        // 校验参数
        if (StringUtils.isBlank(dto.getOldPassword()) || StringUtils.isBlank(dto.getNewPassword())) {
            log.info("修改密码 结果=失败 原因=参数缺失 用户ID={}", userId);
            return Result.error("旧密码和新密码均不能为空");
        }

        // 1. 查出当前用户
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            log.info("修改密码 结果=失败 原因=用户不存在 用户ID={}", userId);
            return Result.error("用户异常，请重新登录");
        }

        // 2. 核心逻辑：验证旧密码是否正确
        String oldPassHash = encrypt(dto.getOldPassword());
        if (!currentUser.getPassword().equals(oldPassHash)) {
            log.info("修改密码 结果=失败 原因=旧密码错误 用户ID={}", userId);
            return Result.error("旧密码错误，修改失败");
        }

        // 3. 加密新密码并更新
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(encrypt(dto.getNewPassword()));

        userMapper.updateById(updateUser);
        log.info("修改密码 结果=成功 用户ID={}", userId);
        return Result.success("密码修改成功", "密码修改成功");
    }
}
