package com.assentinel.core.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "as-sentinel.ai")
public record AiServiceProperties(String baseUrl) {
}
