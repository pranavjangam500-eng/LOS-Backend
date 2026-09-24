package com.bank.los.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance sliding-window in-memory Rate Limiter to prevent brute-force attacks and DoS.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.security.rate-limit.auth-per-minute:10}")
    private int authLimitPerMinute;

    @Value("${app.security.rate-limit.api-per-minute:120}")
    private int apiLimitPerMinute;

    private final Map<String, RequestBucket> authRequestBuckets = new ConcurrentHashMap<>();
    private final Map<String, RequestBucket> apiRequestBuckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Skip rate limiting for health check / actuator endpoints
        if (path.equals("/health") || path.equals("/api/v1/health") || path.equals("/api/v1/auth/health") || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        boolean isAuthEndpoint = path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/forgot-password") ||
                path.startsWith("/api/v1/auth/reset-password") ||
                path.startsWith("/api/v1/auth/refresh-token") ||
                path.startsWith("/api/v1/auth/change-password");

        int maxLimit = isAuthEndpoint ? authLimitPerMinute : apiLimitPerMinute;
        Map<String, RequestBucket> buckets = isAuthEndpoint ? authRequestBuckets : apiRequestBuckets;

        long currentMinute = System.currentTimeMillis() / 60000;
        String key = clientIp + ":" + currentMinute;

        RequestBucket bucket = buckets.computeIfAbsent(key, k -> new RequestBucket());

        if (bucket.incrementAndGet() > maxLimit) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            sendRateLimitErrorResponse(response, isAuthEndpoint ? "Too many authentication attempts. Please try again after 1 minute." : "Too many requests. Please slow down.");
            return;
        }

        // Periodically cleanup stale buckets every 500 requests
        cleanupStaleBuckets(buckets, currentMinute);

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private void sendRateLimitErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "TOO_MANY_REQUESTS");
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void cleanupStaleBuckets(Map<String, RequestBucket> buckets, long currentMinute) {
        if (buckets.size() > 2000) {
            buckets.keySet().removeIf(k -> {
                try {
                    String[] parts = k.split(":");
                    long minute = Long.parseLong(parts[1]);
                    return minute < currentMinute - 1;
                } catch (Exception e) {
                    return true;
                }
            });
        }
    }

    private static class RequestBucket {
        private final AtomicInteger count = new AtomicInteger(0);

        public int incrementAndGet() {
            return count.incrementAndGet();
        }
    }
}
