package com.nanhai.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.nanhai.competition.config.BuildApiConfig;
import com.nanhai.competition.dto.BuildJobResponseDTO;
import com.nanhai.competition.dto.BuildPackageRunResponseDTO;
import com.alibaba.fastjson2.JSONObject;
import com.nanhai.competition.dto.SubmissionDTO;
import com.nanhai.competition.dto.UserRankDTO;
import com.nanhai.competition.entity.Submission;
import com.nanhai.competition.entity.UserInfo;
import com.nanhai.competition.repository.SubmissionRepository;
import com.nanhai.competition.repository.UserInfoRepository;
import com.nanhai.competition.service.SubmissionService;
import com.nanhai.competition.service.CompetitionService;
import com.nanhai.competition.websocket.CompetitionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * 提交记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserInfoRepository userInfoRepository;
    private final RestTemplate restTemplate;
    private final BuildApiConfig buildApiConfig;
    private final CompetitionWebSocketHandler webSocketHandler;
    private final CompetitionService competitionService;

    @Override
    @Transactional
    public Submission createSubmission(Long userId, String branch, Integer passed, Integer completionTime) {
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setBranch(branch);
        submission.setPassed(passed);
        submission.setCompletionTime(completionTime);

        submission = submissionRepository.save(submission);
        log.info("创建提交记录成功: userId={}, branch={}, passed={}", userId, branch, passed);
        
        return submission;
    }

    @Override
    public List<SubmissionDTO> getUserSubmissions(Long userId) {
        List<Submission> submissions = submissionRepository.findByUserId(userId);
        return convertToDTO(submissions);
    }

    @Override
    public List<SubmissionDTO> getAllSubmissions() {
        List<Submission> submissions = submissionRepository.findAllOrderBySubmitTimeDesc();
        return convertToDTO(submissions);
    }

    @Override
    public List<SubmissionDTO> getSubmissionsByBranch(String branch) {
        List<Submission> submissions = submissionRepository.findByBranch(branch);
        return convertToDTO(submissions);
    }

    @Override
    public List<SubmissionDTO> getSubmissionsByPassedRange(Integer minPassed) {
        List<Submission> submissions = submissionRepository.findByPassedGreaterThanEqual(minPassed);
        return convertToDTO(submissions);
    }

    @Override
    public List<SubmissionDTO> getSubmissionsByCompletionTimeRange(Integer maxCompletionTime) {
        List<Submission> submissions = submissionRepository.findByCompletionTimeLessThanEqual(maxCompletionTime);
        return convertToDTO(submissions);
    }

    @Override
    public Double calculateAverageCompletionTime() {
        return submissionRepository.calculateAverageCompletionTime();
    }

    @Override
    public Double calculateAveragePassed() {
        return submissionRepository.calculateAveragePassed();
    }

    @Override
    public BuildJobResponseDTO queryBuildJob(Submission submission, String buildPath, String branch) {
        log.info("开始查询构建任务 - submission: {}, buildPath: {}, branch: {}", submission, buildPath, branch);
        
        try {

            // 调用外部API获取构建任务信息，支持轮询机制
            String jobId = queryBuildJobWithPolling(buildPath, branch);
            
            if (jobId != null) {
                log.info("获取到job_id: {}", jobId);
                // 调用后续接口
                queryPackageRunByJobId(submission, jobId);
            } else {
                log.warn("轮询超时，未能获取到job_id");
            }
            
            // 返回结果
            BuildJobResponseDTO result = new BuildJobResponseDTO();

            
            return result;
            
        } catch (Exception e) {
            log.error("查询构建任务失败", e);
            throw new RuntimeException("查询构建任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过job_id查询包运行结果（BBBB接口）
     */
    private BuildPackageRunResponseDTO queryPackageRunByJobId(Submission submission, String jobId) {
        try {
            // 暂时注释掉外部API调用，用于测试WebSocket功能
            log.info("模拟调用BBBB接口查询包运行结果 - jobId: {}", jobId);
            
            // 模拟处理包运行结果
            log.info("模拟处理包运行结果完成");
            
            // 模拟调用第三个接口
            fetchReportSummary(submission, jobId, "py_ut", "report.json");
            
            // 返回模拟结果
            BuildPackageRunResponseDTO dto = new BuildPackageRunResponseDTO();
            // BuildPackageRunResponseDTO 可能没有 setCode 和 setMessage 方法，直接返回空对象
            
            /* 原始外部API调用代码 - 暂时注释
            String url = UriComponentsBuilder
                    .fromHttpUrl(buildApiConfig.getQueryPackageRunUrl())
                    .queryParam("job_id", jobId)
                    .toUriString();

            log.info("调用BBBB接口查询包运行结果，URL: {}", url);

            // BBBB接口无需token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("BBBB接口响应状态码: {}", response.getStatusCode());
            log.info("BBBB接口响应内容: {}", response.getBody());

            BuildPackageRunResponseDTO dto = JSON.parseObject(response.getBody(), BuildPackageRunResponseDTO.class);

            if (dto != null && dto.getPackage_run_results() != null) {
            */
            
            return dto;
        } catch (Exception e) {
            log.error("通过job_id查询包运行结果失败", e);
            return null;
        }
    }

    /**
     * 调用第三个接口获取报告汇总（通过率等）
     */
    private void fetchReportSummary(Submission submission, String packageName, String name) {
        try {
            // 暂时注释掉外部API调用，用于测试WebSocket功能
            log.info("模拟调用报告接口查询summary -  packageName: {}, name: {}", jobId, packageName, name);
            
            // 模拟处理报告数据
            log.info("模拟处理报告数据完成");
            
            // 更新submission数据并保存到数据库
            if (submission != null) {
                // 获取当前比赛的总用例数
                Integer totalCases = competitionService.getCurrentCompetition().getTotalCases();
                if (totalCases == null) {
                    totalCases = 20; // 默认值
                }
                
                // 模拟更新通过用例数和完成时间
                submission.setPassed(totalCases); // 模拟全部通过，使用动态的总用例数
                submission.setCompletionTime(120); // 模拟2分钟完成
                submission.setSubmitTime(java.time.LocalDateTime.now()); // 更新提交时间
                
                // 保存到数据库
                submissionRepository.save(submission);
                log.info("报告数据已更新并保存到数据库: {}", submission);
            }
            
            // 触发页面数据刷新
            log.info("触发页面数据刷新");
            webSocketHandler.broadcastStats("refresh");
            
            /* 原始外部API调用代码 - 暂时注释
            String url = UriComponentsBuilder
                    .fromHttpUrl(buildApiConfig.getQueryReportUrl())
                    .queryParam("job_id", jobId)
                    .queryParam("package_name", packageName)
                    .queryParam("name", name)
                    .toUriString();

            log.info("调用报告接口查询summary，URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("报告接口响应状态码: {}", response.getStatusCode());
            log.info("报告接口响应内容: {}", response.getBody());

            // 响应为字符串形式的JSON，示例：{"summary":{"passed":16,"total":17}}
            if (response.getBody() != null) {
                try {
                    JSONObject obj = JSONObject.parseObject(response.getBody());
                    JSONObject summary = obj.getJSONObject("summary");
                    if (summary != null) {
                        Integer passed = summary.getInteger("passed");
                        Integer total = summary.getInteger("total");
                        if (passed != null && total != null && total > 0) {
                            double passRate = passed * 100.0 / total;
                            log.info("用例通过率: {}% (passed={}, total={})", String.format("%.1f", passRate), passed, total);
                        }
                    }
                } catch (Exception parseEx) {
                    log.warn("报告summary解析失败", parseEx);
                }
            }
            */
            
        } catch (Exception e) {
            log.error("获取报告汇总失败", e);
        }
    }

    @Override
    public List<UserRankDTO> getTop3Submissions() {
        // 获取通过用例数为20的提交记录，按完成时间升序排序，取前3条
        List<Submission> topSubmissions = submissionRepository.findByPassedOrderBySubmitTimeAsc(20)
                .stream()
                .limit(3)
                .collect(Collectors.toList());
        
        return topSubmissions.stream().map(submission -> {
            UserRankDTO dto = new UserRankDTO();
            dto.setUserId(submission.getUserId());
            dto.setCompletionTime(submission.getCompletionTime());
            dto.setSubmitTime(submission.getSubmitTime());
            
            // 获取用户信息
            UserInfo userInfo = userInfoRepository.findById(submission.getUserId()).orElse(null);
            if (userInfo != null) {
                dto.setUsername(userInfo.getUserName());
                dto.setGroupType(userInfo.getGroupType());
                dto.setSubGroup(userInfo.getSubGroup());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserRankDTO> getFullPassUsersByGroup(String groupType, Integer totalCases) {
        List<Object[]> rows = submissionRepository.findFullPassUsersByGroup(groupType, totalCases);
        return rows.stream().map(row -> {
            Long userId = ((Number) row[0]).longValue();
            Integer minCompletionTime = row[1] == null ? null : ((Number) row[1]).intValue();
            // 第三个字段为最早提交时刻，可能为null
            java.time.LocalDateTime minSubmitTime = row[2] == null ? null : ((java.sql.Timestamp) row[2]).toLocalDateTime();

            UserRankDTO dto = new UserRankDTO();
            dto.setUserId(userId);
            dto.setCompletionTime(minCompletionTime);
            dto.setSubmitTime(minSubmitTime);

            UserInfo userInfo = userInfoRepository.findById(userId).orElse(null);
            if (userInfo != null) {
                dto.setUsername(userInfo.getUserName());
                dto.setGroupType(userInfo.getGroupType());
                dto.setSubGroup(userInfo.getSubGroup());
            }
            return dto;
        }).sorted(Comparator.comparing(UserRankDTO::getSubmitTime, Comparator.nullsLast(Comparator.naturalOrder())))
          .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionDTO> getRecentSubmissions(Integer limit) {
        List<Submission> submissions = submissionRepository.findRecentSubmissionsOrderBySubmitTimeDesc();
        if (limit != null && limit > 0 && limit < submissions.size()) {
            submissions = submissions.subList(0, limit);
        }
        return convertToDTO(submissions);
    }

    /**
     * 转换为DTO
     */
    private List<SubmissionDTO> convertToDTO(List<Submission> submissions) {
        return submissions.stream().map(submission -> {
            SubmissionDTO dto = new SubmissionDTO();
            dto.setId(submission.getId());
            dto.setUserId(submission.getUserId());
            dto.setBranch(submission.getBranch());
            dto.setPassed(submission.getPassed());
            dto.setCompletionTime(submission.getCompletionTime());
            dto.setSubmitTime(submission.getSubmitTime());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 带轮询机制的构建任务查询
     * 如果接口响应为{"info":null}，则每3秒轮询一次，最多20秒
     * 
     * @param buildPath 代码仓地址
     * @param branch 分支名称
     * @return job_id，如果轮询超时则返回null
     */
    private String queryBuildJobWithPolling(String buildPath, String branch) {
        int maxAttempts = 7; // 最多轮询7次 (20秒 / 3秒 ≈ 6.67，向上取整为7)
        int pollIntervalSeconds = 3; // 轮询间隔3秒
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("第{}次尝试查询构建任务 - buildPath: {}, branch: {}", attempt, buildPath, branch);
                
                // 构建请求URL，添加查询参数
                String url = UriComponentsBuilder
                        .fromHttpUrl(buildApiConfig.getQueryJobUrl())
                        .queryParam("build_path", buildPath)
                        .queryParam("branch", branch)
                        .toUriString();
                
                log.info("请求URL: {}", url);
                
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.set("PRIVATE-TOKEN", buildApiConfig.getPrivateToken());
                headers.set("Content-Type", "application/json");
                
                // 创建请求实体
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                // 发送GET请求
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );
                
                log.info("第{}次请求响应状态码: {}", attempt, response.getStatusCode());
                log.info("第{}次请求响应内容: {}", attempt, response.getBody());
                
                // 解析响应
                BuildJobResponseDTO dto = JSON.parseObject(response.getBody(), BuildJobResponseDTO.class);
                
                if (dto != null && dto.getInfo() != null && !dto.getInfo().isEmpty()) {
                    // 成功获取到信息，返回第一个job的job_id
                    String jobId = dto.getInfo().get(0).getJobId();
                    log.info("第{}次尝试成功获取到构建任务信息: {}", attempt, jobId);
                    return jobId;
                } else {
                    // 响应为{"info":null}或空列表，需要继续轮询
                    log.info("第{}次尝试获取到空信息，准备进行第{}次轮询", attempt, attempt + 1);
                    
                    if (attempt < maxAttempts) {
                        // 不是最后一次尝试，等待3秒后继续
                        try {
                            Thread.sleep(pollIntervalSeconds * 1000);
                        } catch (InterruptedException e) {
                            log.warn("轮询等待被中断", e);
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
            } catch (Exception e) {
                log.error("第{}次尝试查询构建任务失败", attempt, e);
                
                if (attempt < maxAttempts) {
                    // 不是最后一次尝试，等待3秒后继续
                    try {
                        Thread.sleep(pollIntervalSeconds * 1000);
                    } catch (InterruptedException ie) {
                        log.warn("轮询等待被中断", ie);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.warn("轮询{}次后仍未获取到构建任务信息，停止轮询", maxAttempts);
        return null;
    }
}

