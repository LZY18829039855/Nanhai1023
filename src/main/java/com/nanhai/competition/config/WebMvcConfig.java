package com.nanhai.competition.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 静态资源处理 - 匹配 /nanhai/api 上下文路径
        registry.addResourceHandler("/nanhai/api/static/**")
                .addResourceLocations("classpath:/static/");
        
        // 默认静态资源处理 - 匹配 /nanhai/api 上下文路径
        registry.addResourceHandler("/nanhai/api/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // 视图控制器 - 匹配 /nanhai/api 上下文路径
        registry.addViewController("/nanhai/api/").setViewName("forward:/index.html");
    }
}

