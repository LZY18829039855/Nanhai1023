package com.nanhai.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户排名DTO
 */
@Data
public class UserRankDTO {
    
    private Long userId;
    private String username;
    private String userCategory;
    private String groupType;
    private String subGroup;
    private String avatar;
    private Integer completionTime;
    private LocalDateTime submitTime;
    private Integer rank;
    private Double successRate;
}

