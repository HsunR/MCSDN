package com.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60000;
    private static final long CLEANUP_INTERVAL_MS = 300000;

    private final Map<String, RateLimitEntry> ipAttempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/login")) {
            if (!isAllowed(ip)) {
                response.setStatus(429);
                response.getWriter().write("{\"error\": \"Too many login attempts. Please try again later.\"}");
                response.setContentType("application/json");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        RateLimitEntry entry = ipAttempts.computeIfAbsent(ip, k -> new RateLimitEntry());

        if (now - entry.windowStart > WINDOW_MS) {
            entry.count.set(1);
            entry.windowStart = now;
            return true;
        }

        if (entry.count.incrementAndGet() > MAX_ATTEMPTS) {
            return false;
        }

        return true;
    }

    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        ipAttempts.entrySet().removeIf(entry -> 
            now - entry.getValue().windowStart > WINDOW_MS * 2
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitEntry {
        volatile long windowStart = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(1);
    }
}
