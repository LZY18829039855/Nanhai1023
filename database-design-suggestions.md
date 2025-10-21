# 数据库设计建议文档

## 业务场景
- 一场固定比赛
- 用户信息预先录入
- 用户持续提交答案（分支提交）
- 记录成功/失败、通过率、提交时间

## 现有表结构评估 ✅

现有的3张表完全满足需求，无需修改：

### 1. competitions（比赛表）
```sql
CREATE TABLE competitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    duration INTEGER NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_participants INTEGER NOT NULL DEFAULT 0,
    ai_group_count INTEGER NOT NULL DEFAULT 0,
    non_ai_group_count INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);
```

### 2. users（用户表）
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    employee_id VARCHAR(50),
    group_type VARCHAR(20) NOT NULL,
    sub_group VARCHAR(20) NOT NULL,
    user_category VARCHAR(50),
    avatar VARCHAR(255)
);
```

### 3. submissions（提交记录表）⭐ 核心表
```sql
CREATE TABLE submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    group_type VARCHAR(20) NOT NULL,
    sub_group VARCHAR(20) NOT NULL,
    submission_id VARCHAR(100),        -- Git分支名称或提交ID
    is_success BOOLEAN NOT NULL,       -- 是否成功
    success_rate DOUBLE,               -- 通过比例（百分比）
    completion_time INTEGER,           -- 完成时间（秒）
    is_first_success BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否首次成功
    submit_time TIMESTAMP NOT NULL     -- 提交时间（自动记录）
);
```

---

## 数据流程设计

### 阶段1：比赛初始化
```
1. 创建比赛记录（status=RUNNING）
2. 批量导入所有参赛用户
3. 统计各组人数更新到competitions表
```

### 阶段2：用户提交阶段（核心流程）
```
每次用户提交分支：
1. 接收提交信息（用户ID、分支名、测试结果）
2. 创建submissions记录
   - user_id: 提交用户
   - submission_id: 分支名称
   - is_success: 测试是否全部通过
   - success_rate: 测试通过率（如95.5%表示测试用例通过了95.5%）
   - completion_time: 从比赛开始到现在的秒数
   - is_first_success: 如果是该用户首次成功提交则为TRUE
3. 实时统计数据供前端展示
```

### 阶段3：实时统计
```
前端需要的统计数据：
- 各组通过率
- 各小组通过率
- TOP3最快完成用户
- 实时提交动态
- 平均完成时间

这些统计都基于submissions表聚合计算
```

---

## 性能优化建议

### 1. 索引优化
```sql
-- 单列索引
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
CREATE INDEX idx_submissions_group_type ON submissions(group_type);
CREATE INDEX idx_submissions_sub_group ON submissions(sub_group);
CREATE INDEX idx_submissions_is_success ON submissions(is_success);
CREATE INDEX idx_submissions_is_first_success ON submissions(is_first_success);
CREATE INDEX idx_submissions_submit_time ON submissions(submit_time DESC);

-- 组合索引（用于复合查询）
CREATE INDEX idx_submissions_group_success ON submissions(group_type, is_success);
CREATE INDEX idx_submissions_subgroup_success ON submissions(sub_group, is_success);
CREATE INDEX idx_submissions_user_success ON submissions(user_id, is_success);
CREATE INDEX idx_submissions_first_success_time ON submissions(is_first_success, submit_time);
```

### 2. 查询优化示例

#### 统计AI组通过率
```sql
SELECT 
    COUNT(*) as total_submissions,
    SUM(CASE WHEN is_success = TRUE THEN 1 ELSE 0 END) as success_count,
    ROUND(SUM(CASE WHEN is_success = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as pass_rate
FROM submissions 
WHERE group_type = 'AI';
```

#### 查询TOP3最快完成用户
```sql
SELECT 
    u.username,
    u.employee_id,
    u.group_type,
    u.sub_group,
    s.completion_time,
    s.submit_time
FROM submissions s
JOIN users u ON s.user_id = u.id
WHERE s.is_first_success = TRUE
ORDER BY s.submit_time ASC
LIMIT 3;
```

#### 查询用户的所有提交历史
```sql
SELECT 
    submission_id,
    is_success,
    success_rate,
    completion_time,
    submit_time
FROM submissions
WHERE user_id = ?
ORDER BY submit_time DESC;
```

#### 实时获取最新10条提交
```sql
SELECT 
    s.username,
    s.group_type,
    s.submission_id,
    s.is_success,
    s.success_rate,
    s.submit_time
FROM submissions s
ORDER BY s.submit_time DESC
LIMIT 10;
```

---

## 数据示例

### 比赛表（只有1条记录）
```
id | name                    | status  | duration | start_time          | total_participants
1  | 南海会议2025实战演练     | RUNNING | 20       | 2025-10-18 15:00:00 | 60
```

### 用户表（预导入所有参赛用户）
```
id | username | employee_id | group_type | sub_group | user_category
1  | zhangsan | EMP001      | AI         | AI-1      | 开发人员
2  | lisi     | EMP002      | AI         | AI-1      | 开发人员
3  | wangwu   | EMP003      | 非AI       | 非AI-1    | 测试人员
...
```

### 提交记录表（持续增长）
```
id | user_id | username | submission_id    | is_success | success_rate | completion_time | is_first_success | submit_time
1  | 1       | zhangsan | branch-v1        | FALSE      | 60.5         | 120             | FALSE            | 15:02:00
2  | 2       | lisi     | feature-login    | TRUE       | 95.0         | 90              | TRUE             | 15:03:30
3  | 1       | zhangsan | branch-v2        | FALSE      | 75.0         | 180             | FALSE            | 15:05:00
4  | 1       | zhangsan | branch-v3        | TRUE       | 98.5         | 240             | TRUE             | 15:07:00
5  | 3       | wangwu   | fix-bug-001      | TRUE       | 92.0         | 150             | TRUE             | 15:08:00
...
```

---

## 业务逻辑说明

### 1. is_first_success 字段的意义
- 每个用户可能提交多次
- 只有第一次成功的提交会标记 is_first_success = TRUE
- 其他所有提交（包括后续成功的）都是 FALSE
- 用于计算TOP3排名（按首次成功的时间排序）

### 2. success_rate vs is_success
- **success_rate**: 测试用例通过率（如95.5%表示通过了95.5%的测试）
- **is_success**: 是否完全通过（通常是 success_rate >= 100%）

### 3. completion_time
- 从比赛开始到提交的秒数
- 用于计算各组平均完成时间
- 用于排序和统计

---

## API接口调用流程

### 初始化流程
```bash
# 1. 开始比赛
curl -X POST "http://localhost:8080/api/competition/start?name=南海会议2025&duration=20"

# 2. 批量创建用户（脚本循环）
for user in users:
    curl -X POST "http://localhost:8080/api/user/create" \
      -d "username=${user.name}" \
      -d "employeeId=${user.id}" \
      -d "groupType=${user.group}" \
      -d "subGroup=${user.subGroup}"
```

### 运行阶段（用户提交）
```bash
# 用户每次提交都调用
curl -X POST "http://localhost:8080/api/submission/create" \
  -d "userId=1" \
  -d "submissionId=branch-feature-001" \
  -d "isSuccess=true" \
  -d "successRate=95.5"
```

### 实时查询
```bash
# 获取比赛统计
curl "http://localhost:8080/api/competition/stats/1"

# 获取TOP3
curl "http://localhost:8080/api/submission/top3"

# 获取最新提交
curl "http://localhost:8080/api/submission/recent?limit=10"
```

---

## 结论

✅ **现有表结构完美支持您的业务场景，无需任何修改！**

核心优势：
1. submissions表支持一个用户多次提交
2. 自动记录提交时间
3. 支持成功/失败和通过率记录
4. 支持首次成功标记（用于排名）
5. 冗余字段设计提升查询性能

建议：
1. 添加索引提升查询性能
2. 使用现有API接口即可完成所有操作
3. 前端实时刷新显示最新数据

