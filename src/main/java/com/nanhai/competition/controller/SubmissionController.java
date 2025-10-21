package com.nanhai.competition.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nanhai.competition.dto.ApiResponse;
import com.nanhai.competition.dto.BuildTriggerDTO;
import com.nanhai.competition.dto.SubmissionDTO;
import com.nanhai.competition.dto.UserRankDTO;
import com.nanhai.competition.entity.Submission;
import com.nanhai.competition.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提交记录控制器
 */
@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {
    
    private final SubmissionService submissionService;
    
    /**
     * 创建提交记录
     */
    @PostMapping("/create")
    public ApiResponse<Submission> createSubmission(
            @RequestParam Long userId,
            @RequestParam String branch,
            @RequestParam(required = false) Integer passed,
            @RequestParam(required = false) Integer completionTime) {
        Submission submission = submissionService.createSubmission(userId, branch, passed, completionTime);
        return ApiResponse.success(submission);
    }
    
    /**
     * 获取用户的提交记录
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<SubmissionDTO>> getUserSubmissions(@PathVariable Long userId) {
        List<SubmissionDTO> submissions = submissionService.getUserSubmissions(userId);
        return ApiResponse.success(submissions);
    }
    
    /**
     * 获取所有提交记录
     */
    @GetMapping("/all")
    public ApiResponse<List<SubmissionDTO>> getAllSubmissions() {
        List<SubmissionDTO> submissions = submissionService.getAllSubmissions();
        return ApiResponse.success(submissions);
    }
    
    /**
     * 根据分支名称获取提交记录
     */
    @GetMapping("/branch/{branch}")
    public ApiResponse<List<SubmissionDTO>> getSubmissionsByBranch(@PathVariable String branch) {
        List<SubmissionDTO> submissions = submissionService.getSubmissionsByBranch(branch);
        return ApiResponse.success(submissions);
    }
    
    /**
     * 根据通过用例数范围获取提交记录
     */
    @GetMapping("/passed")
    public ApiResponse<List<SubmissionDTO>> getSubmissionsByPassedRange(
            @RequestParam Integer minPassed) {
        List<SubmissionDTO> submissions = submissionService.getSubmissionsByPassedRange(minPassed);
        return ApiResponse.success(submissions);
    }
    
    /**
     * 根据完成时间范围获取提交记录
     */
    @GetMapping("/completion-time")
    public ApiResponse<List<SubmissionDTO>> getSubmissionsByCompletionTimeRange(
            @RequestParam Integer maxCompletionTime) {
        List<SubmissionDTO> submissions = submissionService.getSubmissionsByCompletionTimeRange(maxCompletionTime);
        return ApiResponse.success(submissions);
    }
    
    /**
     * 计算平均完成时间
     */
    @GetMapping("/stats/average-completion-time")
    public ApiResponse<Double> getAverageCompletionTime() {
        Double averageTime = submissionService.calculateAverageCompletionTime();
        return ApiResponse.success(averageTime);
    }
    
    /**
     * 计算平均通过用例数
     */
    @GetMapping("/stats/average-passed")
    public ApiResponse<Double> getAveragePassed() {
        Double averagePassed = submissionService.calculateAveragePassed();
        return ApiResponse.success(averagePassed);
    }
    
    /**
     * 获取TOP3提交达人（通过用例数为20且时间最早的用户）
     */
    @GetMapping("/top3")
    public ApiResponse<List<UserRankDTO>> getTop3Submissions() {
        List<UserRankDTO> top3 = submissionService.getTop3Submissions();
        return ApiResponse.success(top3);
    }

    /**
     * 获取指定组别全部通过（passed=20）的人员列表（按最早用时升序）
     */
    @GetMapping("/full-pass")
    public ApiResponse<List<UserRankDTO>> getFullPassUsersByGroup(@RequestParam String groupType) {
        List<UserRankDTO> list = submissionService.getFullPassUsersByGroup(groupType);
        return ApiResponse.success(list);
    }
    
    /**
     * 获取最近的提交记录（用于实时动态显示）
     */
    @GetMapping("/recent")
    public ApiResponse<List<SubmissionDTO>> getRecentSubmissions(@RequestParam(defaultValue = "10") Integer limit) {
        List<SubmissionDTO> submissions = submissionService.getRecentSubmissions(limit);
        return ApiResponse.success(submissions);
    }
    
    /**
     * 接收构建触发信息，并在接收后调用查询构建记录
     * 
     * @param body JSON字符串，包含git_batch和user_username
     * @return 接收成功响应
     */
    @PostMapping("/build-trigger")
    public ApiResponse<BuildTriggerDTO> handleBuildTrigger(@RequestBody String body) {
        // 将字符串转换为JSONObject
        JSONObject jsonObject = JSONObject.parseObject(body);
        
        // 获取git_batch和user_username字段
        String gitBatch = jsonObject.getString("git_batch");
        String userUsername = jsonObject.getString("user_username");
        
        // 封装到DTO
        BuildTriggerDTO dto = new BuildTriggerDTO();
        dto.setGitBatch(gitBatch);
        dto.setUserUsername(userUsername);
        
        // 在接收后调用查询构建记录（参数先假定）
        submissionService.queryBuildJob("https://github.com/example/repo.git", "main");
        
        return ApiResponse.success("构建触发信息接收成功", dto);
    }
}

