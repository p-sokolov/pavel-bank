package com.example.bank_app.common.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = request.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(clientId);

        if (bucket.tryConsume(1)) {
            return true;
        }

        throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
    }
}