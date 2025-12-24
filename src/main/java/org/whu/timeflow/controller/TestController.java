package org.whu.timeflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.whu.timeflow.common.Result;

@RestController
@RequestMapping("/test")
@Tag(name = "测试模块", description = "用于验证鸿蒙与后端连接") // Swagger 分组名称
public class TestController {

    @GetMapping("/hello")
    @Operation(summary = "Hello测试", description = "返回一个打招呼的JSON") // 接口描述
    public Result<String> hello() {
        // 模拟业务逻辑
        String message = "Hello HarmonyOS! 这里是流年笔记后端，时间：" + java.time.LocalDateTime.now();

        // 返回统一格式
        return Result.success(message);
    }
}