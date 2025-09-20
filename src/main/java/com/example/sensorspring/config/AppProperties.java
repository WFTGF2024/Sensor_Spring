package com.example.sensorspring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Upload upload = new Upload();
    private RateLimit rateLimit = new RateLimit();

    public Upload getUpload() { return upload; }
    public RateLimit getRateLimit() { return rateLimit; }

    public static class Upload {
        private String uploadRoot = "/data/uploads";
        public String getUploadRoot() { return uploadRoot; }
        public void setUploadRoot(String uploadRoot) { this.uploadRoot = uploadRoot; }
    }
    public static class RateLimit {
        private int windowSeconds = 10;
        private int maxRequests = 300;
        public int getWindowSeconds() { return windowSeconds; }
        public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
        public int getMaxRequests() { return maxRequests; }
        public void setMaxRequests(int maxRequests) { this.maxRequests = maxRequests; }
    }
}
