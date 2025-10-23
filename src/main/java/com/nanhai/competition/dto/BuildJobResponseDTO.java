package com.nanhai.competition.dto;

import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 构建任务响应DTO
 */
@Data
public class BuildJobResponseDTO {
    
    /**
     * 构建任务信息列表
     */
    private List<JobInfo> info;
    
    /**
     * 构建任务详细信息
     */
    @Data
    public static class JobInfo {
        /**
         * 任务ID
         */
        @JsonProperty(value = "job_id")
        private String jobId;
        
        // 可以根据实际响应添加更多字段
    }
}


