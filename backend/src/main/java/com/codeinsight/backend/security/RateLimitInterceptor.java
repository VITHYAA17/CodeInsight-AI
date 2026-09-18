package com.codeinsight.backend.security;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS pre-flight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // Skip non-API and health/documentation endpoints
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.equals("/api/auth/health")) {
            return true;
        }

        RateLimitPlan plan = resolvePlan(uri);
        String clientKey = resolveClientKey(request);

        ConsumptionProbe probe = rateLimitService.tryConsume(clientKey, plan);

        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.addHeader("X-Rate-Limit-Limit", String.valueOf(plan.getCapacity()));

        if (!probe.isConsumed()) {
            long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
            response.setStatus(429);
            response.setContentType("application/json");
            response.addHeader("Retry-After", String.valueOf(retryAfterSeconds));

            log.warn("[RATE LIMIT EXCEEDED] Client '{}' exceeded limit for plan '{}' on '{}'. Retry after {}s",
                    clientKey, plan.name(), uri, retryAfterSeconds);

            String body = String.format("{\"success\":false,\"message\":\"Rate limit exceeded for %s. Please retry in %d seconds.\",\"retryAfterSeconds\":%d}",
                    plan.name(), retryAfterSeconds, retryAfterSeconds);
            response.getWriter().write(body);
            return false;
        }

        return true;
    }

    private RateLimitPlan resolvePlan(String uri) {
        if (uri.startsWith("/api/ai")) {
            return RateLimitPlan.AI;
        }
        if (uri.equals("/api/platforms/refresh")) {
            return RateLimitPlan.SYNC;
        }
        if (uri.equals("/api/auth/login")) {
            return RateLimitPlan.AUTH;
        }
        return RateLimitPlan.DEFAULT;
    }

    private String resolveClientKey(HttpServletRequest request) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            if (email != null && !email.trim().isEmpty() && !"anonymousUser".equalsIgnoreCase(email)) {
                return "user:" + email;
            }
        } catch (Exception ignored) {
        }
        return "ip:" + extractClientIp(request);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
