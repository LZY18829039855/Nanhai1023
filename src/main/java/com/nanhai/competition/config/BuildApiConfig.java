package com.nanhai.competition.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 构建API配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "build.api")
public class BuildApiConfig {
    
    /**
     * 构建API基础URL（用于queryBuildJob和queryPackageRunByJobId）
     */
    private String baseUrl = "https://example.com/api/v1";
    
    /**
     * 报告API基础URL（用于fetchReportSummary）
     */
    private String reportBaseUrl = "https://example.com/api/v1";
    
    /**
     * 查询构建记录的端点
     */
    private String queryJobEndpoint = "/build/query";
    
    /**
     * 通过job_id查询包运行结果的端点（用于推断语言）
     */
    private String queryPackageRunEndpoint = "/build/package-run";

    /**
     * 通过job_id与package_name查询报告汇总
     */
    private String queryReportEndpoint = "/data-api/report-fail";
    
    /**
     * 私有Token
     */
    private String privateToken = "your-private-token-here";
    
    /**
     * 获取完整的查询URL
     */
    public String getQueryJobUrl() {
        return baseUrl + queryJobEndpoint;
    }
    
    /**
     * 获取通过job_id查询包运行结果的URL
     */
    public String getQueryPackageRunUrl() {
        return baseUrl + queryPackageRunEndpoint;
    }

    /**
     * 获取报告查询URL
     */
    public String getQueryReportUrl() {
        return reportBaseUrl + queryReportEndpoint;
    }
}

