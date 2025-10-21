package com.nanhai.competition.dto;

import lombok.Data;

import java.util.List;

/**
 * 比赛统计数据DTO
 */
@Data
public class CompetitionStatsDTO {
    
    /**
     * 比赛ID
     */
    private Long competitionId;
    
    /**
     * 比赛名称
     */
    private String competitionName;
    
    /**
     * 比赛状态
     */
    private String status;
    
    /**
     * 剩余时间（秒）
     */
    private Integer remainingTime;
    
    /**
     * 总参赛人数
     */
    private Integer totalParticipants;
    
    /**
     * 总体通过率
     */
    private Double overallPassRate;
    
    /**
     * AI组通过率
     */
    private Double aiPassRate;
    
    /**
     * 非AI组通过率
     */
    private Double nonAiPassRate;
    
    /**
     * AI组提交率
     */
    private Double aiSubmissionRate;
    
    /**
     * 非AI组提交率
     */
    private Double nonAiSubmissionRate;
    
    /**
     * AI组总人数
     */
    private Integer aiTotalCount;
    
    /**
     * 非AI组总人数
     */
    private Integer nonAiTotalCount;
    
    /**
     * AI组通过人数
     */
    private Integer aiSuccessCount;
    
    /**
     * 非AI组通过人数
     */
    private Integer nonAiSuccessCount;
    
    /**
     * AI组平均完成时间
     */
    private String aiAverageTime;
    
    /**
     * 非AI组平均完成时间
     */
    private String nonAiAverageTime;
    
    /**
     * TOP3排行榜
     */
    private List<UserRankDTO> top3Rankings;
    
    /**
     * 各小组统计数据
     */
    private List<SubGroupStatsDTO> subGroupStats;
}

