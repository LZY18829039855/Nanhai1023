package com.nanhai.competition.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nanhai.competition.dto.ApiResponse;
import com.nanhai.competition.dto.BuildTriggerDTO;
import com.nanhai.competition.dto.SubmissionDTO;
import com.nanhai.competition.dto.UserRankDTO;
import com.nanhai.competition.entity.Submission;
import com.nanhai.competition.service.SubmissionService;
import com.nanhai.competition.service.CompetitionService;
import com.nanhai.competition.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 提交记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {
    
    private final SubmissionService submissionService;
    private final CompetitionService competitionService;
    private final UserInfoService userInfoService;
    
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
     * 获取指定组别全部通过的人员列表（按最早用时升序）
     */
    @GetMapping("/full-pass")
    public ApiResponse<List<UserRankDTO>> getFullPassUsersByGroup(@RequestParam String groupType) {
        // 获取比赛信息以获取总用例数
        com.nanhai.competition.entity.Competition competition = competitionService.getCurrentCompetition();
        Integer totalCases = competition.getTotalCases() != null ? competition.getTotalCases() : 20;
        
        List<UserRankDTO> list = submissionService.getFullPassUsersByGroup(groupType, totalCases);
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
        String gitBatch = jsonObject.getString("git_branch");
        String userUsername = jsonObject.getString("user_username");
        String webUrl = jsonObject.getJSONObject("project").getString("web_url");
        
        String codehubRepo = "";
        Pattern pattern = Pattern.compile("huawei.com/(.+)");
        Matcher matcher = pattern.matcher(webUrl);

        if (matcher.find()) {
            codehubRepo = matcher.group(1);
        } else {
            codehubRepo = "innersource/fuyao_G/CodeAgent/Permission";
        }

        // 封装到DTO
        BuildTriggerDTO dto = new BuildTriggerDTO();
        dto.setGitBatch(gitBatch);
        dto.setUserUsername(userUsername);
        
        // 根据userUsername解析工号并查询用户ID
        Long userId = null;
        if (userUsername != null && userUsername.length() > 1) {
            try {
                // userUsername格式：姓首字母+employ_id工号，去除首字母获取工号
                String employId = userUsername.substring(1);
                log.info("解析工号: {}", employId);
                
                // 根据工号查询用户信息
                com.nanhai.competition.dto.UserInfoDTO userInfo = userInfoService.getUserInfoByEmployId(employId);
                if (userInfo != null) {
                    userId = userInfo.getId();
                    log.info("根据工号 {} 找到用户ID: {}", employId, userId);
                } else {
                    log.warn("根据工号 {} 未找到用户信息", employId);
                }
            } catch (Exception e) {
                log.error("解析userUsername失败: {}", userUsername, e);
            }
        }
        
        // 如果未找到用户，使用默认用户ID（使用实际存在的用户ID）
        if (userId == null) {
            userId = 4L; // 默认用户ID，使用实际存在的用户ID
            log.warn("使用默认用户ID: {}", userId);
        }
        
        // 创建Submission对象
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setBranch(gitBatch); // 使用git_batch作为分支名称
        submission.setCompletionTime(0); // 初始完成时间为0
        submission.setSubmitTime(java.time.LocalDateTime.now()); // 设置提交时间为当前时间
        
        // 调用查询构建记录方法，传入Submission对象
        submissionService.queryBuildJob(submission, "codehub-open.huawei.com", gitBatch);
        
        
        return ApiResponse.success("构建触发信息接收成功", dto);
    }
}

