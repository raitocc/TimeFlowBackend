package org.whu.timeflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class TimeflowApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(TimeflowApplication.class, args);

        Environment env = application.getEnvironment();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (path == null) path = ""; // 防止空指针

        // 使用 log.info 打印，会有时间戳和线程信息，看着更舒服
        log.info("""
                        
                        ----------------------------------------------------------
                        \t\
                        Application 'TimeFlow' is running! Access URLs:
                        \t\
                        👉 Swagger文档: \thttp://localhost:{}{}/swagger-ui/index.html
                        ----------------------------------------------------------""",
                port, path);
    }
}