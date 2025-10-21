package com.nanhai.competition.dto;

import lombok.Data;

/**
 * 小组统计数据DTO
 */
@Data
public class SubGroupStatsDTO {
    
    private String subGroupName;
    private String groupType;
    private Integer userCount;
    private Integer passedCount;
    private Double passRate;
    private String averageTime;
    private Integer rank;
}

