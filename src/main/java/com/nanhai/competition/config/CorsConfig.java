package com.nanhai.competition.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS跨域配置
 */
@Configuration
public class CorsConfig {

    @Value("${competition.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${competition.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${competition.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${competition.cors.allow-credentials}")
    private Boolean allowCredentials;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源（使用通配符模式，支持null origin用于本地文件访问）
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            config.addAllowedOriginPattern(origin.trim());
        }
        
        // 允许的HTTP方法
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        
        // 允许的请求头
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        
        // 允许携带凭证
        config.setAllowCredentials(allowCredentials);
        
        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);
        
        // 暴露的响应头
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

