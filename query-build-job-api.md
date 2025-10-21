# 查询构建任务接口文档

## 📋 功能说明

该功能实现了调用外部API查询构建任务记录并获取job_id的能力。

## 🏗️ 架构设计

### 1. 配置类 (`BuildApiConfig.java`)

负责管理构建API的配置信息：

```java
build:
  api:
    base-url: https://example.com/api/v1          # API基础URL
    query-job-endpoint: /build/query               # 查询端点
    private-token: ${BUILD_API_TOKEN:your-token}   # 私有Token（支持环境变量）
```

### 2. 响应DTO (`BuildJobResponseDTO.java`)

接收API返回的构建任务信息：

```json
{
  "info": [
    {
      "job_id": "6868679989"
    }
  ]
}
```

### 3. Service层实现

#### 方法签名

```java
BuildJobResponseDTO queryBuildJob(String buildPath, String branch)
```

#### 实现逻辑

1. **构建请求URL**：使用`UriComponentsBuilder`添加查询参数
   - `build_path`: 代码仓地址
   - `branch`: 分支名称

2. **设置请求头**：
   - `PRIVATE-TOKEN`: 私有Token用于认证
   - `Content-Type`: application/json

3. **发送HTTP请求**：使用`RestTemplate`发送GET请求

4. **解析响应**：使用FastJSON解析JSON响应

5. **提取job_id**：从响应的info数组中提取所有job_id

## 🔌 API接口

### 测试接口

**接口地址**: `GET /api/submission/query-build-job`

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| buildPath | String | 否 | https://github.com/example/repo.git | 代码仓地址 |
| branch | String | 否 | main | 分支名称 |

### 请求示例

#### cURL

```bash
curl -X GET "http://localhost:8080/api/submission/query-build-job?buildPath=https://github.com/example/repo.git&branch=main"
```

#### 浏览器直接访问

```
http://localhost:8080/api/submission/query-build-job
```

### 响应示例

#### 成功响应

```json
{
  "code": 200,
  "message": "查询构建任务成功",
  "data": {
    "info": [
      {
        "jobId": "6868679989"
      }
    ]
  },
  "timestamp": 1729425600000
}
```

#### 失败响应

```json
{
  "code": 500,
  "message": "查询构建任务失败: Connection refused",
  "data": null,
  "timestamp": 1729425600000
}
```

## ⚙️ 配置说明

### 1. application.yml配置

```yaml
build:
  api:
    base-url: https://your-build-api.com/api/v1
    query-job-endpoint: /build/query
    private-token: ${BUILD_API_TOKEN:default-token}
```

### 2. 环境变量配置

可以通过环境变量设置Token（推荐用于生产环境）：

**Windows (PowerShell)**:
```powershell
$env:BUILD_API_TOKEN="your-actual-token"
```

**Windows (CMD)**:
```cmd
set BUILD_API_TOKEN=your-actual-token
```

**Linux/Mac**:
```bash
export BUILD_API_TOKEN=your-actual-token
```

### 3. 启动时配置

```bash
java -jar competition-backend-1.0.0.jar --build.api.private-token=your-token
```

## 📝 使用示例

### Java代码调用

```java
@Autowired
private SubmissionService submissionService;

public void example() {
    String buildPath = "https://github.com/myorg/myrepo.git";
    String branch = "develop";
    
    BuildJobResponseDTO result = submissionService.queryBuildJob(buildPath, branch);
    
    if (result != null && result.getInfo() != null) {
        for (BuildJobResponseDTO.JobInfo jobInfo : result.getInfo()) {
            String jobId = jobInfo.getJobId();
            System.out.println("Job ID: " + jobId);
        }
    }
}
```

## 🔍 实际应用场景

在`handleBuildTrigger`接口中集成：

```java
@PostMapping("/build-trigger")
public ApiResponse<BuildTriggerDTO> handleBuildTrigger(@RequestBody String body) {
    JSONObject jsonObject = JSONObject.parseObject(body);
    String gitBatch = jsonObject.getString("git_batch");
    String userUsername = jsonObject.getString("user_username");
    
    // 1. 接收构建触发信息
    BuildTriggerDTO dto = new BuildTriggerDTO();
    dto.setGitBatch(gitBatch);
    dto.setUserUsername(userUsername);
    
    // 2. 查询构建任务（获取job_id）
    BuildJobResponseDTO jobResult = submissionService.queryBuildJob(
        "https://github.com/example/repo.git", 
        "main"
    );
    
    // 3. 后续可以使用job_id进行其他操作
    // 例如：轮询构建状态、记录构建历史等
    
    return ApiResponse.success("构建触发信息接收成功", dto);
}
```

## 🚨 注意事项

1. **网络超时**：RestTemplate默认没有超时设置，建议在生产环境配置超时时间
2. **重试机制**：建议添加重试逻辑处理网络波动
3. **Token安全**：不要将Token硬编码在代码中，使用环境变量或配置中心
4. **异常处理**：当前实现会抛出RuntimeException，可根据需要自定义异常
5. **日志记录**：所有请求和响应都会记录在日志中，方便排查问题

## 📊 日志输出示例

```
2025-10-20 20:43:37 [http-nio-8080-exec-1] INFO  c.n.c.s.i.SubmissionServiceImpl - 开始查询构建任务 - buildPath: https://github.com/example/repo.git, branch: main
2025-10-20 20:43:37 [http-nio-8080-exec-1] INFO  c.n.c.s.i.SubmissionServiceImpl - 请求URL: https://example.com/api/v1/build/query?build_path=https://github.com/example/repo.git&branch=main
2025-10-20 20:43:38 [http-nio-8080-exec-1] INFO  c.n.c.s.i.SubmissionServiceImpl - 响应状态码: 200
2025-10-20 20:43:38 [http-nio-8080-exec-1] INFO  c.n.c.s.i.SubmissionServiceImpl - 响应内容: {"info":[{"job_id":"6868679989"}]}
2025-10-20 20:43:38 [http-nio-8080-exec-1] INFO  c.n.c.s.i.SubmissionServiceImpl - 获取到job_id: 6868679989
```

## 🎯 下一步扩展

1. 添加重试机制和超时配置
2. 实现构建状态轮询
3. 将job_id关联到用户提交记录
4. 添加构建结果回调处理
5. 实现WebSocket实时推送构建状态


