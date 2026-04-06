package org.example.nitrowizard.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public class ApiConfig {
    private final String baseUrl;
    private final String ingredientsPath;
    private final String elixirsPath;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final int maxRetries;
    private final long backoffMillis;

    public ApiConfig(String baseUrl,
                     String ingredientsPath,
                     String elixirsPath,
                     Duration connectTimeout,
                     Duration requestTimeout,
                     int maxRetries,
                     long backoffMillis) {
        this.baseUrl = baseUrl;
        this.ingredientsPath = ingredientsPath;
        this.elixirsPath = elixirsPath;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        this.maxRetries = maxRetries;
        this.backoffMillis = backoffMillis;
    }

    public static ApiConfig fromEnv() {
        Properties properties = loadProperties("config.properties");
        String baseUrl = getRequired(properties, "wizard.api.baseUrl");
        String ingredientsPath = getRequired(properties, "wizard.api.ingredientsPath");
        String elixirsPath = getRequired(properties, "wizard.api.elixirsPath");
        Duration connectTimeout = Duration.ofSeconds(parseInt(getRequired(properties, "wizard.api.connectTimeoutSeconds")));
        Duration requestTimeout = Duration.ofSeconds(parseInt(getRequired(properties, "wizard.api.requestTimeoutSeconds")));
        int maxRetries = parseInt(getRequired(properties, "wizard.api.maxRetries"));
        long backoffMillis = parseLong(getRequired(properties, "wizard.api.backoffMillis"));
        return new ApiConfig(baseUrl, ingredientsPath, elixirsPath, connectTimeout, requestTimeout, maxRetries, backoffMillis);
    }

    private static Properties loadProperties(String resourceName) {
        Properties properties = new Properties();
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IllegalStateException("Missing configuration file: " + resourceName);
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load configuration file: " + resourceName, ex);
        }
        return properties;
    }

    private static String getRequired(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: " + key);
        }
        return value;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid integer configuration: " + value, ex);
        }
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid long configuration: " + value, ex);
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getIngredientsPath() {
        return ingredientsPath;
    }

    public String getElixirsPath() {
        return elixirsPath;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getBackoffMillis() {
        return backoffMillis;
    }
}
