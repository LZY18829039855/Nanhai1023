# 去除比赛表改造指南

## 背景
如果确定只有一场固定比赛，可以去除competitions表，简化数据模型。

---

## 改造方案

### 1. 修改配置文件

**application.yml**
```yaml
competition:
  name: "南海会议2025实战演练"
  description: "云核心网2025南海会议 · 实战演练"
  duration: 20  # 比赛时长（分钟）
  start-time: "2025-10-18T15:00:00"  # 比赛开始时间
```

### 2. 创建配置类

**CompetitionConfig.java**
```java
@Configuration
@ConfigurationProperties(prefix = "competition")
@Data
public class CompetitionConfig {
    private String name;
    private String description;
    private Integer duration;
    private LocalDateTime startTime;
    
    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration);
    }
    
    public Integer getRemainingSeconds() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return duration * 60;
        }
        LocalDateTime endTime = getEndTime();
        if (now.isAfter(endTime)) {
            return 0;
        }
        return (int) Duration.between(now, endTime).getSeconds();
    }
    
    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return "WAITING";
        } else if (now.isAfter(getEndTime())) {
            return "ENDED";
        } else {
            return "RUNNING";
        }
    }
}
```

### 3. 简化统计Service

**StatisticsService.java** (新建)
```java
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final CompetitionConfig competitionConfig;
    
    /**
     * 获取比赛统计数据
     */
    public CompetitionStatsDTO getCompetitionStats() {
        CompetitionStatsDTO stats = new CompetitionStatsDTO();
        
        // 比赛信息
        stats.setStatus(competitionConfig.getStatus());
        stats.setRemainingTime(competitionConfig.getRemainingSeconds());
        
        // 参赛人数统计（实时查询）
        Long totalUsers = userRepository.count();
        Long aiGroupUsers = userRepository.countByGroupType("AI");
        Long nonAiGroupUsers = userRepository.countByGroupType("非AI");
        
        stats.setTotalParticipants(totalUsers.intValue());
        
        // 通过率统计
        Long totalSubmissions = submissionRepository.count();
        Long totalSuccess = submissionRepository.countByIsSuccess(true);
        stats.setOverallPassRate(calculateRate(totalSuccess, totalSubmissions));
        
        // AI组统计
        Long aiSubmissions = submissionRepository.countByGroupType("AI");
        Long aiSuccess = submissionRepository.countByGroupTypeAndIsSuccess("AI", true);
        stats.setAiPassRate(calculateRate(aiSuccess, aiSubmissions));
        stats.setAiSuccessCount(aiSuccess.intValue());
        
        // 非AI组统计
        Long nonAiSubmissions = submissionRepository.countByGroupType("非AI");
        Long nonAiSuccess = submissionRepository.countByGroupTypeAndIsSuccess("非AI", true);
        stats.setNonAiPassRate(calculateRate(nonAiSuccess, nonAiSubmissions));
        stats.setNonAiSuccessCount(nonAiSuccess.intValue());
        
        // 平均完成时间
        Double aiAvgTime = submissionRepository.calculateAverageTimeByGroupType("AI");
        Double nonAiAvgTime = submissionRepository.calculateAverageTimeByGroupType("非AI");
        stats.setAiAverageTime(formatTime(aiAvgTime));
        stats.setNonAiAverageTime(formatTime(nonAiAvgTime));
        
        return stats;
    }
    
    private Double calculateRate(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0) {
            return 0.0;
        }
        return (numerator.doubleValue() / denominator.doubleValue()) * 100;
    }
    
    private String formatTime(Double seconds) {
        if (seconds == null || seconds == 0) {
            return "0:00";
        }
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", minutes, secs);
    }
}
```

### 4. 简化Controller

**StatisticsController.java** (新建，替代CompetitionController)
```java
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final CompetitionConfig competitionConfig;
    
    /**
     * 获取比赛信息
     */
    @GetMapping("/competition")
    public ApiResponse<Map<String, Object>> getCompetitionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", competitionConfig.getName());
        info.put("description", competitionConfig.getDescription());
        info.put("duration", competitionConfig.getDuration());
        info.put("startTime", competitionConfig.getStartTime());
        info.put("endTime", competitionConfig.getEndTime());
        info.put("status", competitionConfig.getStatus());
        return ApiResponse.success(info);
    }
    
    /**
     * 获取比赛统计数据
     */
    @GetMapping("/data")
    public ApiResponse<CompetitionStatsDTO> getStats() {
        CompetitionStatsDTO stats = statisticsService.getCompetitionStats();
        return ApiResponse.success(stats);
    }
    
    /**
     * 获取剩余时间
     */
    @GetMapping("/remaining-time")
    public ApiResponse<Integer> getRemainingTime() {
        Integer remainingTime = competitionConfig.getRemainingSeconds();
        return ApiResponse.success(remainingTime);
    }
}
```

### 5. 删除的文件清单

```
❌ src/main/java/.../entity/Competition.java
❌ src/main/java/.../repository/CompetitionRepository.java
❌ src/main/java/.../service/CompetitionService.java
❌ src/main/java/.../service/impl/CompetitionServiceImpl.java
❌ src/main/java/.../controller/CompetitionController.java
```

### 6. 数据库变更

```sql
-- 删除比赛表
DROP TABLE IF EXISTS competitions;
```

---

## 改造后的数据模型

### 核心2张表

**1. users（用户表）**
```
存储所有参赛用户信息
- 比赛开始前批量导入
- 比赛期间不变
```

**2. submissions（提交记录表）**
```
存储用户的每次提交
- 持续增长
- 记录所有提交历史
```

### 比赛配置
```
存储在配置文件中
- application.yml
- 可以根据环境切换
```

---

## API接口变更

### 旧接口（使用competitions表）
```
POST /api/competition/start
POST /api/competition/end/{id}
GET  /api/competition/current
GET  /api/competition/stats/{id}
GET  /api/competition/status/{id}
GET  /api/competition/remaining-time/{id}
```

### 新接口（使用配置）
```
GET  /api/stats/competition        # 获取比赛信息
GET  /api/stats/data               # 获取统计数据
GET  /api/stats/remaining-time     # 获取剩余时间
```

---

## 优缺点对比

### 优点 ✅
1. 数据模型更简单（2张表）
2. 无需维护比赛表
3. 配置文件管理更灵活
4. 统计数据实时计算，永远准确

### 缺点 ❌
1. 无法记录比赛历史
2. 无法支持多场比赛
3. 比赛时间只能通过配置修改
4. 需要重构现有代码

---

## 迁移步骤

1. ✅ 创建CompetitionConfig配置类
2. ✅ 创建StatisticsService
3. ✅ 创建StatisticsController
4. ✅ 更新前端API调用
5. ✅ 删除旧的Competition相关类
6. ✅ 删除competitions表
7. ✅ 测试所有功能

---

## 测试清单

- [ ] 获取比赛信息
- [ ] 获取统计数据
- [ ] 计算剩余时间
- [ ] 用户提交功能
- [ ] TOP3排行榜
- [ ] 实时提交动态
- [ ] 各组通过率统计

---

## 结论

如果确定只有一场固定比赛，去除competitions表是可行的。
但需要权衡简化带来的好处 vs 重构的工作量。

建议：
- 如果系统已经上线运行 → 保留表，不做改动
- 如果还在开发阶段 → 可以考虑简化
- 如果未来可能多场比赛 → 必须保留表

