package com.nanhai.competition.dto;

import lombok.Data;

/**
 * 构建触发DTO
 */
@Data
public class BuildTriggerDTO {
    
    /**
     * Git批次
     */
    private String gitBatch;
    
    /**
     * 用户用户名
     */
    private String userUsername;
}
