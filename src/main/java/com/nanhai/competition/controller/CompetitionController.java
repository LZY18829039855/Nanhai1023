package com.nanhai.competition.controller;

import com.nanhai.competition.dto.ApiResponse;
import com.nanhai.competition.dto.CompetitionStatsDTO;
import com.nanhai.competition.entity.Competition;
import com.nanhai.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 比赛控制器
 */
@RestController
@RequestMapping("/competition")
@RequiredArgsConstructor
public class CompetitionController {
    
    private final CompetitionService competitionService;
    
    /**
     * 开始比赛（更新比赛开始时间）
     */
    @PostMapping("/start")
    public ApiResponse<Competition> startCompetition() {
        Competition competition = competitionService.startCompetition();
        return ApiResponse.success("比赛开始时间已更新", competition);
    }
    
    /**
     * 获取当前比赛
     */
    @GetMapping("/current")
    public ApiResponse<Competition> getCurrentCompetition() {
        Competition competition = competitionService.getCurrentCompetition();
        return ApiResponse.success(competition);
    }
    
    /**
     * 获取比赛统计数据
     */
    @GetMapping("/stats/{competitionId}")
    public ApiResponse<CompetitionStatsDTO> getCompetitionStats(@PathVariable Long competitionId) {
        CompetitionStatsDTO stats = competitionService.getCompetitionStats(competitionId);
        return ApiResponse.success(stats);
    }
    
    /**
     * 获取比赛开始时间
     */
    @GetMapping("/start-time")
    public ApiResponse<Competition> getCompetitionStartTime() {
        Competition competition = competitionService.getCompetitionStartTime();
        return ApiResponse.success(competition);
    }
    
    /**
     * 获取当前比赛统计数据
     */
    @GetMapping("/current-stats")
    public ApiResponse<CompetitionStatsDTO> getCurrentCompetitionStats() {
        Competition competition = competitionService.getCurrentCompetition();
        CompetitionStatsDTO stats = competitionService.getCompetitionStats(competition.getId());
        return ApiResponse.success(stats);
    }
}

