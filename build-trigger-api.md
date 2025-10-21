# 构建触发接口文档

## 接口信息

- **接口路径**: `/api/submission/build-trigger`
- **请求方法**: `POST`
- **Content-Type**: `application/json`

## 请求参数

请求Body为JSON字符串，包含以下字段：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| git_batch | String | 是 | Git批次号 |
| user_username | String | 是 | 用户名 |

## 请求示例

### cURL示例

```bash
curl -X POST http://localhost:8080/api/submission/build-trigger \
  -H "Content-Type: application/json" \
  -d '{
    "git_batch": "batch_20251020_001",
    "user_username": "张小明"
  }'
```

### 原始JSON请求体

```json
{
  "git_batch": "batch_20251020_001",
  "user_username": "张小明"
}
```

## 响应示例

### 成功响应

```json
{
  "code": 200,
  "message": "构建触发信息接收成功",
  "data": {
    "gitBatch": "batch_20251020_001",
    "userUsername": "张小明"
  },
  "timestamp": 1729425600000
}
```

### 错误响应

```json
{
  "code": 500,
  "message": "JSON解析失败",
  "data": null,
  "timestamp": 1729425600000
}
```

## 接口说明

1. 该接口用于接收用户触发构建时的信息
2. 接收到的JSON字符串会被解析为JSONObject
3. 从中提取`git_batch`和`user_username`两个字段
4. 接口仅接收数据，不执行其他业务逻辑
5. 成功接收后返回解析后的数据作为确认

## 注意事项

- 请确保请求体为有效的JSON格式
- `git_batch`和`user_username`字段必须存在
- 如果字段缺失，接口会返回null值


