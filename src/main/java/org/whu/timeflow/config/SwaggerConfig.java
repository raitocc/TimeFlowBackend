package org.whu.timeflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                // 1. 配置基本信息
                .info(new Info()
                        .title("流年笔记 (TimeFlow) 后端接口文档")
                        .description("鸿蒙期末大作业 - 后端服务")
                        .version("v1.0")
                        .summary("负责处理日记、账单、待办数据的同步"))

                // 2. 配置 JWT 认证方式 (这一步会在 UI 上显示“锁”)
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", // 名字随便起，但要和下面对应
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP) // 类型是 HTTP
                                        .scheme("bearer")               // 方案是 bearer
                                        .bearerFormat("JWT")            // 格式是 JWT
                                        .name("Authorization")))        // 头名称

                // 3. 全局应用这个安全配置 (意味着所有接口默认都带锁)
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}