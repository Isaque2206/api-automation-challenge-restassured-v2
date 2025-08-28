package com.isaque.qa.util;

import java.time.Duration;

public final class Env {
    private Env() {}

    public static String baseUrl() {
        return System.getProperty("baseUrl", System.getenv().getOrDefault("BASE_URL", "https://serverest.dev"));
    }

    public static String adminFlag() {
        return System.getProperty("ADMIN", System.getenv().getOrDefault("ADMIN", "true"));
    }

    public static boolean enableRateLimitTest() {
        return Boolean.parseBoolean(System.getProperty("ENABLE_RATE_LIMIT_TEST", System.getenv().getOrDefault("ENABLE_RATE_LIMIT_TEST","false")));
    }

    public static Duration rateLimitInterval() {
        String val = System.getProperty("RATE_LIMIT_INTERVAL_MS", System.getenv().getOrDefault("RATE_LIMIT_INTERVAL_MS", "700"));
        return Duration.ofMillis(Long.parseLong(val));
    }
}
