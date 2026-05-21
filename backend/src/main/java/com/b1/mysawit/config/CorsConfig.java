package com.b1.mysawit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final DeliveryFeatureInterceptor deliveryFeatureInterceptor;

    public CorsConfig(DeliveryFeatureInterceptor deliveryFeatureInterceptor) {
        this.deliveryFeatureInterceptor = deliveryFeatureInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deliveryFeatureInterceptor)
                .addPathPatterns("/api/delivery/**");
    }
}
