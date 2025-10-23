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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
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

    public RestTemplate createRestTemplate() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            return new RestTemplate();
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        return new RestTemplate(requestFactory);
    }

    /**
     * 通过job_id查询包运行结果（BBBB接口）
     */
    private BuildPackageRunResponseDTO queryPackageRunByJobId(Submission submission, String jobId) {
        try {
                        
            // 原始外部API调用代码 - 暂时注释
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
            String package_name = "";
            String name = "";

            BuildPackageRunResponseDTO dto = JSON.parseObject(response.getBody(), BuildPackageRunResponseDTO.class);

            if (dto.getPackage_run_results() == null) {
                if (submission!=null) {
                    submission.setPassed(0);
                    submissionRepository.save(submission);
                    webSocketHandler.broadcastStats("refresh");
                    return dto;
                }
            }

            for(BuildPackageRunResponseDTO.PackageRunResult result : dto.getPackage_run_results()) {
                if (result.getPackage_name().equalsIgnoreCase("go_ut")) {
                    package_name = result.getPackage_name();
                    name = "report.xml";
                    break;
                } else if (result.getPackage_name().equalsIgnoreCase("py_ut")) {
                    package_name = result.getPackage_name();
                    name = "report.json";
                    break;
                }
            }
            fetchReportSummary(submission, jobId, package_name, name);
            return dto;
        } catch (Exception e) {
            log.error("通过job_id查询包运行结果失败", e);
            return null;
        }
    }

    /**
     * 调用第三个接口获取报告汇总（通过率等）
     */
    public void fetchReportSummary(Submission submission, String jobId, String packageName, String name) {
        final int maxPollingTimeMs = 20000;
        final int pollingIntervalMs = 2000;
        final Instant startTime = Instant.now();
        boolean isSuccess = false;

        while (!isSuccess) {
            try {
                if (Duration.between(startTime, Instant.now()).toMillis() >= maxPollingTimeMs) {
                    break;
                }
                String url = UriComponentsBuilder
                    .fromHttpUrl(buildApiConfig.getQueryReportUrl())
                    .queryParam("job_id", jobId)
                    .queryParam("package_name", packageName)
                    .queryParam("name", name)
                    .toUriString();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().trim().startsWith("{\"error\":}")) {
                    int passed = 0;
                    String responseBody = response.getBody();
                    if (responseBody == null) {
                        continue;
                    }
                    if (name.endsWith(".xml")) {
                        try{
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            Document doc = builder.parse(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));

                            Element rootElement = doc.getDocumentElement();
                            int totalTests = Integer.parseInt(rootElement.getAttribute("tests"));
                            int totalFailures = Integer.parseInt(rootElement.getAttribute("failures"));
                            int totalErrors = Integer.parseInt(rootElement.getAttribute("errors"));
                            passed = totalTests-totalFailures-totalErrors;

                        }catch(Exception e){
                            log.error("解析XML报告失败", e);
                            continue;
                        }
                        
                    }else if (name.endsWith(".json")) {
                        try{
                            JSONObject obj = JSONObject.parseObject(responseBody);
                            JSONObject summary = obj.getJSONObject("summary");
                            passed = summary != null ? summary.getInteger("passed") : 0;
                        }catch(Exception e){
                            log.error("解析JSON报告失败", e);
                            continue;
                        }
                    }
                    
                    if (submission != null) {
                        submission.setPassed(passed);
                        submissionRepository.save(submission);
                        webSocketHandler.broadcastStats("refresh");
                        isSuccess = true;
                    }
                } else {
                    Thread.sleep(pollingIntervalMs);
                }
            } catch (Exception e) {
                try{
                    Thread.sleep(pollingIntervalMs);
                }catch(InterruptedException ie){
                    log.error("轮询等待被中断", ie);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
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
                ResponseEntity<String> response = createRestTemplate().exchange(
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

