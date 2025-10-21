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
        // Drop the known benign WS close-code 1005 errors that appear as onErrorDropped in Gateway
        Hooks.onErrorDropped(ex -> {
            // unwrap common wrapping layers to inspect the root cause
            Throwable t = ex;
            while (t.getCause() != null && t.getCause() != t) {
                t = t.getCause();
            }

            if (t instanceof IllegalArgumentException && t.getMessage() != null
                && t.getMessage().contains("WebSocket close status code does NOT comply with RFC-6455: 1005")) {
                // swallow this specific, harmless noise
                return;
            }

            // Log other dropped errors explicitly (since we've overridden Reactor's default handler)
            log.error("Dropped reactive error", ex);
        });
    }
}
