package org.example.nitrowizard.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "wizard.api")
@Validated
public class ApiConfig {
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String ingredientsPath;
    @NotBlank
    private String elixirsPath;
    @NotNull
    private Duration connectTimeout;
    @NotNull
    private Duration requestTimeout;
    @Min(0)
    private int maxRetries;
    @Min(0)
    private long backoffMillis;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getIngredientsPath() {
        return ingredientsPath;
    }

    public void setIngredientsPath(String ingredientsPath) {
        this.ingredientsPath = ingredientsPath;
    }

    public String getElixirsPath() {
        return elixirsPath;
    }

    public void setElixirsPath(String elixirsPath) {
        this.elixirsPath = elixirsPath;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getBackoffMillis() {
        return backoffMillis;
    }

    public void setBackoffMillis(long backoffMillis) {
        this.backoffMillis = backoffMillis;
    }
}
