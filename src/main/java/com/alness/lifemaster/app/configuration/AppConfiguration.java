package com.alness.lifemaster.app.configuration;

import org.springframework.context.annotation.Configuration;

import com.alness.lifemaster.app.service.AppConfigService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AppConfiguration {
    private final AppConfigService appConfigService;

    @PostConstruct
    public void init() {
        log.info("Response: {}", appConfigService.createDefaultValues());
    }
}
