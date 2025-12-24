package org.whu.timeflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("流年笔记 (TimeFlow) 后端接口文档")
                        .description("鸿蒙期末大作业 - 后端服务")
                        .version("v1.0")
                        .summary("负责处理日记、账单、待办数据的同步"));
    }
}