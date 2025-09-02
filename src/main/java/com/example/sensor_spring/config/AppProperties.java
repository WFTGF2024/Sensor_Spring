package com.example.sensor_spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String uploadRoot = "/root/python project_remote/download/";
    private long inMemoryUploadLimit = 268435456; // 256MB
    private RateLimit rateLimit = new RateLimit();

    public static class RateLimit {
        private Duration windowDuration = Duration.ofSeconds(10);
        private int maxCalls = 1000;
        private boolean exitOnExceed = true;

        public Duration getWindowDuration() { return windowDuration; }
        public void setWindowDuration(Duration windowDuration) { this.windowDuration = windowDuration; }
        public int getMaxCalls() { return maxCalls; }
        public void setMaxCalls(int maxCalls) { this.maxCalls = maxCalls; }
        public boolean isExitOnExceed() { return exitOnExceed; }
        public void setExitOnExceed(boolean exitOnExceed) { this.exitOnExceed = exitOnExceed; }
    }

    public String getUploadRoot() { return uploadRoot; }
    public void setUploadRoot(String uploadRoot) { this.uploadRoot = uploadRoot; }
    public long getInMemoryUploadLimit() { return inMemoryUploadLimit; }
    public void setInMemoryUploadLimit(long inMemoryUploadLimit) { this.inMemoryUploadLimit = inMemoryUploadLimit; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
}
