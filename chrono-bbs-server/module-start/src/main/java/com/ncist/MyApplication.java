package com.ncist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 网上超市系统启动类。
 *
 * <p>启用 Spring Boot 自动装配与定时任务调度，作为服务端唯一运行入口。</p>
 */
@EnableScheduling
@SpringBootApplication
public class MyApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}