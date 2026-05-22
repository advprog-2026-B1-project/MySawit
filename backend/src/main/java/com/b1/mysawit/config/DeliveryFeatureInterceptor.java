package com.b1.mysawit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DeliveryFeatureInterceptor implements HandlerInterceptor {

    private final FeatureFlags featureFlags;

    public DeliveryFeatureInterceptor(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!featureFlags.isDeliveryEnabled()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Fitur pengiriman saat ini tidak tersedia\"}");
            return false;
        }
        return true;
    }
}
