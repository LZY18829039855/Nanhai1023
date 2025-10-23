# AI Competition Backend Dockerfile
# 基于OpenJDK 8构建

FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache curl

# 创建应用用户
RUN addgroup -g 1000 competition && \
    adduser -D -s /bin/sh -u 1000 -G competition competition

# 复制JAR文件
COPY target/competition-backend-1.0.0.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R competition:competition /app

# 切换到应用用户
USER competition

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
