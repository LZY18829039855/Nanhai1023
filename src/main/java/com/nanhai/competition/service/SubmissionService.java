package com.nanhai.competition.service;

import com.nanhai.competition.dto.BuildJobResponseDTO;
import com.nanhai.competition.dto.SubmissionDTO;
import com.nanhai.competition.dto.UserRankDTO;
import com.nanhai.competition.entity.Submission;

import java.util.List;

/**
 * 提交记录服务接口
 */
public interface SubmissionService {
    
    /**
     * 创建提交记录
     */
    Submission createSubmission(Long userId, String branch, Integer passed, Integer completionTime);
    
    /**
     * 获取用户的所有提交记录
     */
    List<SubmissionDTO> getUserSubmissions(Long userId);
    
    /**
     * 获取所有提交记录
     */
    List<SubmissionDTO> getAllSubmissions();
    
    /**
     * 根据分支名称获取提交记录
     */
    List<SubmissionDTO> getSubmissionsByBranch(String branch);
    
    /**
     * 根据通过用例数范围获取提交记录
     */
    List<SubmissionDTO> getSubmissionsByPassedRange(Integer minPassed);
    
    /**
     * 根据完成时间范围获取提交记录
     */
    List<SubmissionDTO> getSubmissionsByCompletionTimeRange(Integer maxCompletionTime);
    
    /**
     * 计算平均完成时间
     */
    Double calculateAverageCompletionTime();
    
    /**
     * 计算平均通过用例数
     */
    Double calculateAveragePassed();
    
    /**
     * 获取TOP3提交达人（通过用例数为20且时间最早的用户）
     */
    List<UserRankDTO> getTop3Submissions();
    
    /**
     * 查询构建任务记录
     * 
     * @param buildPath 代码仓地址
     * @param branch 分支名称
     * @return 构建任务响应
     */
    BuildJobResponseDTO queryBuildJob(String buildPath, String branch);

    /**
     * 获取指定组别（AI组/非AI组）全部通过（passed=20）人员列表，按最早用时升序
     */
    List<UserRankDTO> getFullPassUsersByGroup(String groupType);
    
    /**
     * 获取最近的提交记录（用于实时动态显示）
     */
    List<SubmissionDTO> getRecentSubmissions(Integer limit);
}

