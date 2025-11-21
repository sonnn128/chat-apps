package com.nnson128.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import reactor.core.publisher.Hooks;

@Configuration
public class ReactorNoiseFilterConfig {

    private static final Logger log = LoggerFactory.getLogger(ReactorNoiseFilterConfig.class);

    @EventListener(ApplicationReadyEvent.class)
    public void installReactorFilters() {
        Hooks.onErrorDropped(ex -> {
            Throwable t = ex;
            while (t.getCause() != null && t.getCause() != t) {
                t = t.getCause();
            }

            if (t instanceof IllegalArgumentException && t.getMessage() != null
                && t.getMessage().contains("WebSocket close status code does NOT comply with RFC-6455: 1005")) {
                return;
            }
        });
    }
}
