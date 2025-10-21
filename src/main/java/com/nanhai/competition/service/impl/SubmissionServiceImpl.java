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
        List<Submission> submissions = submissionRepository.findAll();
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
    public BuildJobResponseDTO queryBuildJob(String buildPath, String branch) {
        log.info("开始查询构建任务 - buildPath: {}, branch: {}", buildPath, branch);
        
        try {
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
            
            // 记录响应
            log.info("响应状态码: {}", response.getStatusCode());
            log.info("响应内容: {}", response.getBody());
            
            // 解析响应为DTO
            BuildJobResponseDTO result = JSON.parseObject(response.getBody(), BuildJobResponseDTO.class);
            
            // 提取job_id
            if (result != null && result.getInfo() != null && !result.getInfo().isEmpty()) {
                for (BuildJobResponseDTO.JobInfo jobInfo : result.getInfo()) {
                    String jobId = jobInfo.getJobId();
                    log.info("获取到job_id: {}", jobId);
                    // 通过job_id调用BBBB接口查询语言/包运行结果
                    queryPackageRunByJobId(jobId);
                }
            } else {
                log.warn("未获取到构建任务信息");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("查询构建任务失败", e);
            throw new RuntimeException("查询构建任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过job_id查询包运行结果（BBBB接口）
     */
    private BuildPackageRunResponseDTO queryPackageRunByJobId(String jobId) {
        try {
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
                for (BuildPackageRunResponseDTO.PackageRunResult item : dto.getPackage_run_results()) {
                    String packageName = item.getPackage_name();
                    log.info("package_name: {}", packageName);
                    // 获取通过率报告
                    fetchReportSummary(jobId, packageName, "report.json");
                }
            } else {
                log.warn("BBBB接口未返回有效的package_run_results");
            }

            return dto;
        } catch (Exception e) {
            log.error("通过job_id查询包运行结果失败", e);
            return null;
        }
    }

    /**
     * 调用第三个接口获取报告汇总（通过率等）
     */
    private void fetchReportSummary(String jobId, String packageName, String name) {
        try {
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
        } catch (Exception e) {
            log.error("获取报告汇总失败", e);
        }
    }

    @Override
    public List<UserRankDTO> getTop3Submissions() {
        // 获取通过用例数为20的提交记录，按完成时间升序排序，取前3条
        List<Submission> topSubmissions = submissionRepository.findByPassedOrderByCompletionTimeAsc(20)
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
    public List<UserRankDTO> getFullPassUsersByGroup(String groupType) {
        List<Object[]> rows = submissionRepository.findFullPassUsersByGroup(groupType);
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
        }).sorted(Comparator.comparing(UserRankDTO::getCompletionTime, Comparator.nullsLast(Comparator.naturalOrder())))
          .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionDTO> getRecentSubmissions(Integer limit) {
        List<Submission> submissions = submissionRepository.findTop10ByOrderBySubmitTimeDesc();
        if (limit != null && limit < submissions.size()) {
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
}

