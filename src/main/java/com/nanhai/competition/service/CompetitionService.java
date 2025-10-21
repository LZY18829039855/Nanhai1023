package com.nanhai.competition.service;

import com.nanhai.competition.dto.CompetitionStatsDTO;
import com.nanhai.competition.entity.Competition;

/**
 * 比赛服务接口
 */
public interface CompetitionService {
    
    /**
     * 开始比赛（更新比赛开始时间）
     */
    Competition startCompetition();
    
    /**
     * 获取当前比赛
     */
    Competition getCurrentCompetition();
    
    /**
     * 获取比赛统计数据
     */
    CompetitionStatsDTO getCompetitionStats(Long competitionId);
    
    /**
     * 获取比赛开始时间
     */
    Competition getCompetitionStartTime();
}

