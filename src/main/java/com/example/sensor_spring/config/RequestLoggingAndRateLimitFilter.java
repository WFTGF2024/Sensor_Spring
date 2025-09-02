package com.example.sensor_spring.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RequestLoggingAndRateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingAndRateLimitFilter.class);
    private final AppProperties props;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private volatile long windowStartMillis;
    private final ReentrantLock lock = new ReentrantLock();

    public RequestLoggingAndRateLimitFilter(AppProperties props) {
        this.props = props;
        this.windowStartMillis = System.currentTimeMillis();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String addr = request.getRemoteAddr();
        String path = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        log.info("-> {} {} {}", addr, request.getMethod(), path);
        enforceRateLimit();
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("<- {} {} {} -> {}", addr, request.getMethod(), path, response.getStatus());
        }
    }

    private void enforceRateLimit() {
        long now = System.currentTimeMillis();
        long windowMs = props.getRateLimit().getWindowDuration().toMillis();
        lock.lock();
        try {
            if (now - windowStartMillis <= windowMs) {
                int current = callCount.incrementAndGet();
                if (current > props.getRateLimit().getMaxCalls()) {
                    log.error("Rate limit exceeded: {} calls in {}ms. Exiting.", current, windowMs);
                    if (props.getRateLimit().isExitOnExceed()) {
                        System.exit(1);
                    }
                }
            } else {
                windowStartMillis = now;
                callCount.set(1);
            }
        } finally {
            lock.unlock();
        }
    }
}
