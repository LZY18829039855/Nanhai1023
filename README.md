# AI编程大赛后端系统

云核心网2025南海会议实战演练后端系统 - Spring Boot框架

## 项目概述

本项目是AI编程大赛的后端系统，提供比赛管理、用户管理、提交记录管理、实时数据推送等功能。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0+ / H2(测试)
- **ORM**: Spring Data JPA
- **WebSocket**: Spring WebSocket + STOMP
- **构建工具**: Maven
- **Java版本**: JDK 17+

## 项目结构

```
competition-backend/
├── src/
│   ├── main/
│   │   ├── java/com/nanhai/competition/
│   │   │   ├── config/              # 配置类
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   ├── controller/          # 控制器层
│   │   │   │   ├── CompetitionController.java
│   │   │   │   ├── SubmissionController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                 # 数据传输对象
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── CompetitionStatsDTO.java
│   │   │   │   ├── SubmissionDTO.java
│   │   │   │   ├── UserRankDTO.java
│   │   │   │   └── SubGroupStatsDTO.java
│   │   │   ├── entity/              # 实体类
│   │   │   │   ├── Competition.java
│   │   │   │   ├── Submission.java
│   │   │   │   └── User.java
│   │   │   ├── exception/           # 异常处理
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/          # 数据访问层
│   │   │   │   ├── CompetitionRepository.java
│   │   │   │   ├── SubmissionRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/             # 服务层接口
│   │   │   │   ├── CompetitionService.java
│   │   │   │   ├── SubmissionService.java
│   │   │   │   └── UserService.java
│   │   │   ├── websocket/           # WebSocket处理
│   │   │   │   └── CompetitionWebSocketHandler.java
│   │   │   └── CompetitionApplication.java  # 启动类
│   │   └── resources/
│   │       ├── application.yml      # 主配置文件
│   │       ├── application-dev.yml  # 开发环境配置
│   │       └── application-prod.yml # 生产环境配置
│   └── test/                        # 测试代码
└── pom.xml                          # Maven配置

```

## 主要功能

### 1. 比赛管理
- 开始/结束比赛
- 获取比赛状态和统计数据
- 计算剩余时间

### 2. 用户管理
- 用户注册和查询
- 按组/小组查询用户
- 统计用户数量

### 3. 提交记录管理
- 创建提交记录
- 查询提交历史
- 计算通过率
- TOP3排行榜

### 4. 实时数据推送（WebSocket）
- 实时提交记录推送
- 统计数据更新推送
- 排行榜更新推送
- 比赛状态变更推送

## API接口文档

### 比赛相关接口

#### 1. 开始比赛
```
POST /api/competition/start
参数: name, description, duration
```

#### 2. 结束比赛
```
POST /api/competition/end/{competitionId}
```

#### 3. 获取当前比赛
```
GET /api/competition/current
```

#### 4. 获取比赛统计
```
GET /api/competition/stats/{competitionId}
```

### 提交记录相关接口

#### 1. 创建提交记录
```
POST /api/submission/create
参数: userId, submissionId, isSuccess, successRate
```

#### 2. 获取TOP3排行榜
```
GET /api/submission/top3
```

#### 3. 获取最近提交
```
GET /api/submission/recent?limit=10
```

#### 4. 获取组通过率
```
GET /api/submission/pass-rate/group/{groupType}
```

### 用户相关接口

#### 1. 创建用户
```
POST /api/user/create
参数: username, realName, groupType, subGroup
```

#### 2. 获取所有用户
```
GET /api/user/all
```

#### 3. 获取用户总数
```
GET /api/user/count/total
```

## WebSocket连接

### 连接端点
```
ws://localhost:8080/api/ws/competition
```

### 订阅主题
- `/topic/submissions` - 提交记录更新
- `/topic/stats` - 统计数据更新
- `/topic/rankings` - 排行榜更新
- `/topic/status` - 比赛状态更新

## 配置说明

### 数据库配置
在 `application.yml` 中修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/competition_db
    username: your_username
    password: your_password
```

### CORS配置
```yaml
competition:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:8080
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
```

### WebSocket配置
```yaml
competition:
  websocket:
    endpoint: /ws/competition
    allowed-origins: "*"
```

## 运行项目

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库准备
```sql
CREATE DATABASE competition_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 启动项目

#### 开发环境
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 生产环境
```bash
mvn clean package
java -jar target/competition-backend-1.0.0.jar --spring.profiles.active=prod
```

### 4. 访问地址
- 后端API: http://localhost:8080/api
- 健康检查: http://localhost:8080/api/actuator/health

## 注意事项

1. **业务层代码未实现**: 当前仅提供框架代码，Service接口的实现类需要根据业务需求编写
2. **数据库表**: 首次运行会自动创建表结构（ddl-auto: update）
3. **安全性**: 生产环境需要添加Spring Security进行认证授权
4. **性能优化**: 建议添加Redis缓存和消息队列
5. **日志**: 生产环境建议使用ELK或其他日志收集系统

## 后续开发建议

1. 实现Service层业务逻辑
2. 添加单元测试和集成测试
3. 添加Redis缓存支持
4. 添加消息队列（RabbitMQ/Kafka）
5. 添加Spring Security安全认证
6. 添加API文档（Swagger/Knife4j）
7. 添加性能监控（Prometheus + Grafana）
8. 添加分布式追踪（Zipkin）

## 联系方式

项目负责人: 南海团队

# mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=h2"

