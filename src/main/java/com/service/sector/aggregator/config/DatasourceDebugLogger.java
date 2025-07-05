package com.service.sector.aggregator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("debug-ds")
public class DatasourceDebugLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(DatasourceDebugLogger.class);
    private final DataSourceProperties dsp;

    public DatasourceDebugLogger(DataSourceProperties dsp) {
        this.dsp = dsp;          // already populated by Spring Boot
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=== Datasource properties resolved (ApplicationReady) ===");
        log.info("spring.datasource.url      = {}", dsp.getUrl());
        log.info("spring.datasource.username = {}", dsp.getUsername());
        log.info("spring.datasource.password = {}", mask(dsp.getPassword()));
        log.info("================================================================");
    }

    private String mask(String pwd) {
        return (pwd == null) ? null : "*".repeat(Math.max(4, pwd.length()));
    }
}
