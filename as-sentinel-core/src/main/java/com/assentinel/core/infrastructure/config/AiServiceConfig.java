package com.assentinel.core.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiServiceProperties.class)
public class AiServiceConfig {

    @Bean
    RestClient.Builder aiRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient aiRestClient(RestClient.Builder restClientBuilder, AiServiceProperties properties) {
        return restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
    }

}
