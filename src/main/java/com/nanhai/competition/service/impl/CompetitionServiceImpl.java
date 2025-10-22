package com.nanhai.competition.service.impl;

import com.nanhai.competition.dto.CompetitionStatsDTO;
import com.nanhai.competition.dto.SubGroupStatsDTO;
import com.nanhai.competition.entity.Competition;
import com.nanhai.competition.exception.ResourceNotFoundException;
import com.nanhai.competition.repository.CompetitionRepository;
import com.nanhai.competition.repository.SubmissionRepository;
import com.nanhai.competition.repository.UserInfoRepository;
import com.nanhai.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 比赛服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final UserInfoRepository userInfoRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public Competition startCompetition() {
        // 获取或创建比赛记录
        Competition competition = getCurrentCompetition();
        
        // 更新比赛开始时间
        competition.setStartTime(LocalDateTime.now());
        competition = competitionRepository.save(competition);
        
        log.info("比赛开始时间已更新: {}", competition.getStartTime());
        return competition;
    }

    @Override
    public Competition getCurrentCompetition() {
        // 获取第一条比赛记录，如果不存在则创建
        return competitionRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Competition competition = new Competition();
                    competition.setStartTime(LocalDateTime.now());
                    competition.setTotalCases(20); // 默认总用例数为20
                    return competitionRepository.save(competition);
                });
    }

    @Override
    public CompetitionStatsDTO getCompetitionStats(Long competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("比赛不存在"));

        CompetitionStatsDTO stats = new CompetitionStatsDTO();
        stats.setCompetitionId(competition.getId());
        stats.setCompetitionName("AI编程大赛");
        stats.setStatus("RUNNING");
        stats.setRemainingTime(0); // 简化处理，不计算剩余时间
        stats.setStartTime(competition.getStartTime().toString()); // 设置比赛开始时间

        // 获取总用例数
        Integer totalCases = competition.getTotalCases() != null ? competition.getTotalCases() : 20;
        stats.setTotalCases(totalCases); // 设置总用例数到返回的DTO中

        // 从user_info表获取用户统计（仅未删除的用户）
        Long totalUsers = userInfoRepository.countByIsDeleted("N");
        Long aiGroupUsers = userInfoRepository.countByGroupType("AI组");
        Long nonAiGroupUsers = userInfoRepository.countByGroupType("非AI组");
        
        stats.setTotalParticipants(totalUsers.intValue());
        
        // 计算总体通过率：所有成员最好通过的用例数总和 / (总人数 × total_cases)
        Long allUsersMaxPassedSum = submissionRepository.getAllUsersMaxPassedSum();
        double overallPassRate = totalUsers > 0 ? (allUsersMaxPassedSum.doubleValue() / (totalUsers * totalCases)) * 100 : 0.0;
        stats.setOverallPassRate(Math.round(overallPassRate * 100.0) / 100.0); // 保留两位小数
        
        // AI组统计
        stats.setAiTotalCount(aiGroupUsers.intValue());
        // 计算AI组通过率：AI组所有成员最好通过的用例数总和 / (AI组成员总数 × total_cases)
        Long aiGroupMaxPassedSum = submissionRepository.getAiGroupMaxPassedSum();
        double aiPassRate = aiGroupUsers > 0 ? (aiGroupMaxPassedSum.doubleValue() / (aiGroupUsers * totalCases)) * 100 : 0.0;
        stats.setAiPassRate(Math.round(aiPassRate * 100.0) / 100.0); // 保留两位小数
        stats.setAiSubmissionRate(0.0); // Placeholder
        stats.setAiSuccessCount(aiGroupMaxPassedSum.intValue());
        
        // 非AI组统计
        stats.setNonAiTotalCount(nonAiGroupUsers.intValue());
        // 计算非AI组通过率：非AI组所有成员最好通过的用例数总和 / (非AI组成员总数 × total_cases)
        Long nonAiGroupMaxPassedSum = submissionRepository.getNonAiGroupMaxPassedSum();
        double nonAiPassRate = nonAiGroupUsers > 0 ? (nonAiGroupMaxPassedSum.doubleValue() / (nonAiGroupUsers * totalCases)) * 100 : 0.0;
        stats.setNonAiPassRate(Math.round(nonAiPassRate * 100.0) / 100.0); // 保留两位小数
        stats.setNonAiSubmissionRate(0.0); // Placeholder
        stats.setNonAiSuccessCount(nonAiGroupMaxPassedSum.intValue());
        
            // 平均完成时间 - 计算全部通过的平均用时
            Double aiAvgTime = submissionRepository.getAiGroupFullPassAverageTime(totalCases);
            Double nonAiAvgTime = submissionRepository.getNonAiGroupFullPassAverageTime(totalCases);
            stats.setAiAverageTime(formatTime(aiAvgTime));
            stats.setNonAiAverageTime(formatTime(nonAiAvgTime));
        
        // TOP3排行榜 - 暂时为空
        stats.setTop3Rankings(new ArrayList<>());
        
        // 各小组统计
        List<SubGroupStatsDTO> subGroupStats = new ArrayList<>();
        
        // AI组各小组统计
        String[] aiSubGroups = {"AI-1小组", "AI-2小组", "AI-3小组", "AI-4小组"};
        for (String subGroup : aiSubGroups) {
            SubGroupStatsDTO subGroupStat = new SubGroupStatsDTO();
            subGroupStat.setSubGroupName(subGroup);
            subGroupStat.setGroupType("AI组");
            
            Long subGroupUserCount = userInfoRepository.countBySubGroup(subGroup);
            Long subGroupMaxPassedSum = submissionRepository.getSubGroupMaxPassedSum(subGroup);
            Long subGroupFullPassUserCount = submissionRepository.getSubGroupFullPassUserCount(subGroup, totalCases);
            Double subGroupAverageTime = submissionRepository.getSubGroupFullPassAverageTime(subGroup, totalCases);
            
            subGroupStat.setUserCount(subGroupUserCount.intValue());
            subGroupStat.setPassedCount(subGroupFullPassUserCount.intValue()); // 改为显示通过人数
            
            // 计算小组通过率：小组内各成员的最好通过用例数之和 / (小组总人数 × total_cases)
            double subGroupPassRate = subGroupUserCount > 0 ? 
                (subGroupMaxPassedSum.doubleValue() / (subGroupUserCount * totalCases)) * 100 : 0.0;
            subGroupStat.setPassRate(Math.round(subGroupPassRate * 100.0) / 100.0);
            
            // 设置平均完成时间
            if (subGroupAverageTime != null) {
                subGroupStat.setAverageTime(formatTime(subGroupAverageTime));
            } else {
                subGroupStat.setAverageTime("暂无数据");
            }
            
            subGroupStats.add(subGroupStat);
        }
        
        // 非AI组各小组统计
        String[] nonAiSubGroups = {"非AI-1小组", "非AI-2小组"};
        for (String subGroup : nonAiSubGroups) {
            SubGroupStatsDTO subGroupStat = new SubGroupStatsDTO();
            subGroupStat.setSubGroupName(subGroup);
            subGroupStat.setGroupType("非AI组");
            
            Long subGroupUserCount = userInfoRepository.countBySubGroup(subGroup);
            Long subGroupMaxPassedSum = submissionRepository.getSubGroupMaxPassedSum(subGroup);
            Long subGroupFullPassUserCount = submissionRepository.getSubGroupFullPassUserCount(subGroup, totalCases);
            Double subGroupAverageTime = submissionRepository.getSubGroupFullPassAverageTime(subGroup, totalCases);
            
            subGroupStat.setUserCount(subGroupUserCount.intValue());
            subGroupStat.setPassedCount(subGroupFullPassUserCount.intValue()); // 改为显示通过人数
            
            // 计算小组通过率：小组内各成员的最好通过用例数之和 / (小组总人数 × total_cases)
            double subGroupPassRate = subGroupUserCount > 0 ? 
                (subGroupMaxPassedSum.doubleValue() / (subGroupUserCount * totalCases)) * 100 : 0.0;
            subGroupStat.setPassRate(Math.round(subGroupPassRate * 100.0) / 100.0);
            
            // 设置平均完成时间
            if (subGroupAverageTime != null) {
                subGroupStat.setAverageTime(formatTime(subGroupAverageTime));
            } else {
                subGroupStat.setAverageTime("暂无数据");
            }
            
            subGroupStats.add(subGroupStat);
        }
        
        // 为所有小组按通过率排序，通过率相同时按平均完成时间排序（时间越短排名越高）
        subGroupStats.sort((a, b) -> {
            // 首先按通过率排序（从高到低）
            int passRateCompare = Double.compare(b.getPassRate(), a.getPassRate());
            if (passRateCompare != 0) {
                return passRateCompare;
            }
            
            // 通过率相同时，按平均完成时间排序（从短到长）
            // 需要解析时间字符串进行比较
            try {
                double timeA = parseTimeToSeconds(a.getAverageTime());
                double timeB = parseTimeToSeconds(b.getAverageTime());
                return Double.compare(timeA, timeB); // 时间越短排名越高
            } catch (Exception e) {
                // 如果时间解析失败，保持原有顺序
                return 0;
            }
        });
        
        for (int i = 0; i < subGroupStats.size(); i++) {
            subGroupStats.get(i).setRank(i + 1);
        }
        
        stats.setSubGroupStats(subGroupStats);
        
        return stats;
    }
    
    /**
     * 解析时间字符串为秒数
     * 支持格式：如 "2分30秒" -> 150秒
     */
    private double parseTimeToSeconds(String timeStr) {
        if (timeStr == null || timeStr.equals("暂无数据")) {
            return Double.MAX_VALUE; // 没有数据的时间设为最大值，排名靠后
        }
        
        try {
            // 解析 "X分Y秒" 格式
            if (timeStr.contains("分") && timeStr.contains("秒")) {
                String[] parts = timeStr.replace("分", ",").replace("秒", "").split(",");
                int minutes = Integer.parseInt(parts[0].trim());
                int seconds = Integer.parseInt(parts[1].trim());
                return minutes * 60 + seconds;
            }
            // 解析 "X秒" 格式
            else if (timeStr.contains("秒")) {
                return Double.parseDouble(timeStr.replace("秒", "").trim());
            }
            // 解析纯数字（假设为秒）
            else {
                return Double.parseDouble(timeStr.trim());
            }
        } catch (Exception e) {
            return Double.MAX_VALUE; // 解析失败时设为最大值
        }
    }

    @Override
    public Competition getCompetitionStartTime() {
        return getCurrentCompetition();
    }

    /**
     * 格式化时间（秒转为分:秒格式）
     */
    private String formatTime(Double seconds) {
        if (seconds == null || seconds == 0) {
            return "0:00";
        }
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", minutes, secs);
    }
}