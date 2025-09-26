package org.example.airesumescoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    private String modelDefault;

    // Getters and Setters
    public String getModelDefault() {
        return modelDefault;
    }

    public void setModelDefault(String modelDefault) {
        this.modelDefault = modelDefault;
    }
}