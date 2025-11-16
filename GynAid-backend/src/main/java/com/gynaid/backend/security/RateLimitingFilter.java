package com.gynaid.backend.security;

import com.gynaid.backend.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting Filter for API security
 * Integrates RateLimitingService to prevent abuse
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    public RateLimitingFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // Skip rate limiting for public endpoints
        if (isPublicEndpoint(requestUri, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for health checks and actuator endpoints
        if (requestUri.startsWith("/actuator/") || 
            requestUri.equals("/api/health") ||
            requestUri.equals("/api/auth/test")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Get user ID if authenticated
            String userId = getCurrentUserId(request);

            // Check API rate limit
            RateLimitingService.RateLimitResult rateLimitResult = rateLimitingService.checkApiRateLimit(clientIp, 
                userId != null ? Long.parseLong(userId) : null);

            if (!rateLimitResult.isAllowed()) {
                log.warn("Rate limit exceeded for IP: {}, UserId: {}, Request: {} {}, Remaining time: {}s",
                    maskIpAddress(clientIp), userId, method, requestUri, rateLimitResult.getRemainingTimeSeconds());

                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(String.format(
                    "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"retryAfter\":%d}",
                    rateLimitResult.getRemainingTimeSeconds()
                ));
                return;
            }

            // Add rate limit headers for monitoring
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitResult.getMaxAttempts()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.getRemainingAttempts()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimitResult.getRemainingTimeSeconds()));

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in rate limiting filter", e);
            // Fail open - allow request if rate limiting service fails
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPublicEndpoint(String requestUri, String method) {
        // Public endpoints that don't need rate limiting
        return requestUri.startsWith("/api/auth/login") ||
               requestUri.startsWith("/api/auth/register") ||
               requestUri.startsWith("/api/auth/verify-email") ||
               requestUri.startsWith("/api/auth/reset-password") ||
               requestUri.startsWith("/api/webhooks/") ||
               requestUri.equals("/h2-console/") ||
               requestUri.equals("/error");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getCurrentUserId(HttpServletRequest request) {
        // Extract user ID from JWT token if present
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                // For now, return null - JWT parsing would be done here
                // In production, parse JWT to extract user ID
                return null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String maskIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "unknown";
        }
        // Mask IP for logging
        if (ip.contains(".")) {
            return ip.replaceAll("(\\d+\\.\\d+\\.\\d+\\.)\\d+", "$1***");
        }
        return "***";
    }
}