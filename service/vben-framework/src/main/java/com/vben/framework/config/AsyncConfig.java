package com.vben.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步线程池配置。
 */
@Configuration
public class AsyncConfig {

    @Bean("operationLogExecutor")
    public Executor operationLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("operation-log-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(2000);
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }
}
