package com.vben.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动成功日志。
 */
@Slf4j
@Component
public class ApplicationStartupLogger {

    private static final String STARTUP_BANNER = """
            ███████╗████████╗███████╗      ██╗   ██╗██████╗ ███████╗███╗   ██╗
            ╚══███╔╝╚══██╔══╝██╔════╝      ██║   ██║██╔══██╗██╔════╝████╗  ██║
              ███╔╝    ██║   █████╗        ██║   ██║██████╔╝█████╗  ██╔██╗ ██║
             ███╔╝     ██║   ██╔══╝        ╚██╗ ██╔╝██╔══██╗██╔══╝  ██║╚██╗██║
            ███████╗   ██║   ██║            ╚████╔╝ ██████╔╝███████╗██║ ╚████║
            ╚══════╝   ╚═╝   ╚═╝             ╚═══╝  ╚═════╝ ╚══════╝╚═╝  ╚═══╝
            """;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String version = SpringBootVersion.getVersion();
        log.info("\n{}\n:: ztf-vben启动成功 :: {}\n", STARTUP_BANNER, version);
    }
}
