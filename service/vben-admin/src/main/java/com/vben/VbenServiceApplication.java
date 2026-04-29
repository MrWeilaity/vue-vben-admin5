package com.vben;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * VbenServiceApplication 组件说明。
 */
@EnableAsync
@SpringBootApplication
public class VbenServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VbenServiceApplication.class, args);
    }
}
