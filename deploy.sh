#!/bin/bash

# AI Competition Backend 部署脚本
# 适用于Linux环境部署

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置参数
PROJECT_NAME="competition-backend"
VERSION="1.0.0"
JAR_NAME="${PROJECT_NAME}-${VERSION}.jar"
DEPLOY_DIR="/opt/competition-backend"
SERVICE_NAME="competition-backend"
USER="competition"
PORT="8080"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用root用户运行此脚本"
        exit 1
    fi
}

# 创建部署用户
create_user() {
    log_info "创建部署用户..."
    
    if ! id "$USER" &>/dev/null; then
        useradd -r -s /bin/false -d "$DEPLOY_DIR" "$USER"
        log_success "用户 $USER 创建成功"
    else
        log_warning "用户 $USER 已存在"
    fi
}

# 创建部署目录
create_directories() {
    log_info "创建部署目录..."
    
    mkdir -p "$DEPLOY_DIR"/{bin,logs,config,backup}
    chown -R "$USER:$USER" "$DEPLOY_DIR"
    log_success "部署目录创建完成: $DEPLOY_DIR"
}

# 复制JAR文件
copy_jar() {
    log_info "复制JAR文件..."
    
    if [ ! -f "target/$JAR_NAME" ]; then
        log_error "JAR文件不存在，请先运行编译脚本"
        exit 1
    fi
    
    cp "target/$JAR_NAME" "$DEPLOY_DIR/bin/"
    chown "$USER:$USER" "$DEPLOY_DIR/bin/$JAR_NAME"
    log_success "JAR文件复制完成"
}

# 创建配置文件
create_config() {
    log_info "创建配置文件..."
    
    cat > "$DEPLOY_DIR/config/application-prod.yml" << EOF
server:
  port: $PORT

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nanhai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 1234
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

logging:
  level:
    com.nanhai.competition: INFO
  file:
    name: $DEPLOY_DIR/logs/competition.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
EOF

    chown "$USER:$USER" "$DEPLOY_DIR/config/application-prod.yml"
    log_success "配置文件创建完成"
}

# 创建启动脚本
create_startup_script() {
    log_info "创建启动脚本..."
    
    cat > "$DEPLOY_DIR/bin/start.sh" << 'EOF'
#!/bin/bash

# 启动脚本
JAR_NAME="competition-backend-1.0.0.jar"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
CONFIG_DIR="/opt/competition-backend/config"
LOG_DIR="/opt/competition-backend/logs"

cd /opt/competition-backend/bin

# 检查JAR文件是否存在
if [ ! -f "$JAR_NAME" ]; then
    echo "错误: JAR文件不存在"
    exit 1
fi

# 启动应用
echo "启动 AI Competition Backend..."
java $JAVA_OPTS \
    -Dspring.profiles.active=prod \
    -Dspring.config.location=classpath:/application.yml,file:$CONFIG_DIR/ \
    -Dlogging.file.name=$LOG_DIR/competition.log \
    -jar "$JAR_NAME" \
    "$@"
EOF

    chmod +x "$DEPLOY_DIR/bin/start.sh"
    chown "$USER:$USER" "$DEPLOY_DIR/bin/start.sh"
    log_success "启动脚本创建完成"
}

# 创建systemd服务
create_systemd_service() {
    log_info "创建systemd服务..."
    
    cat > "/etc/systemd/system/$SERVICE_NAME.service" << EOF
[Unit]
Description=AI Competition Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=$USER
Group=$USER
WorkingDirectory=$DEPLOY_DIR/bin
ExecStart=$DEPLOY_DIR/bin/start.sh
ExecStop=/bin/kill -15 \$MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# 环境变量
Environment=JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
Environment=SPRING_PROFILES_ACTIVE=prod

# 资源限制
LimitNOFILE=65536
LimitNPROC=32768

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    log_success "systemd服务创建完成"
}

# 创建停止脚本
create_stop_script() {
    log_info "创建停止脚本..."
    
    cat > "$DEPLOY_DIR/bin/stop.sh" << 'EOF'
#!/bin/bash

# 停止脚本
SERVICE_NAME="competition-backend"

echo "停止 AI Competition Backend 服务..."
systemctl stop "$SERVICE_NAME"

# 等待服务完全停止
sleep 3

# 检查是否还有Java进程
PIDS=$(pgrep -f "competition-backend-1.0.0.jar")
if [ ! -z "$PIDS" ]; then
    echo "强制停止残留进程..."
    kill -9 $PIDS
fi

echo "服务已停止"
EOF

    chmod +x "$DEPLOY_DIR/bin/stop.sh"
    chown "$USER:$USER" "$DEPLOY_DIR/bin/stop.sh"
    log_success "停止脚本创建完成"
}

# 创建状态检查脚本
create_status_script() {
    log_info "创建状态检查脚本..."
    
    cat > "$DEPLOY_DIR/bin/status.sh" << 'EOF'
#!/bin/bash

# 状态检查脚本
SERVICE_NAME="competition-backend"

echo "=== AI Competition Backend 服务状态 ==="
echo ""

# 检查systemd服务状态
echo "1. Systemd服务状态:"
systemctl status "$SERVICE_NAME" --no-pager -l

echo ""
echo "2. 进程状态:"
PIDS=$(pgrep -f "competition-backend-1.0.0.jar")
if [ ! -z "$PIDS" ]; then
    ps -p $PIDS -o pid,ppid,cmd,etime,pcpu,pmem
else
    echo "未找到运行中的进程"
fi

echo ""
echo "3. 端口监听状态:"
netstat -tlnp | grep :8080 || echo "端口8080未被监听"

echo ""
echo "4. 日志文件大小:"
if [ -f "/opt/competition-backend/logs/competition.log" ]; then
    ls -lh /opt/competition-backend/logs/competition.log
else
    echo "日志文件不存在"
fi
EOF

    chmod +x "$DEPLOY_DIR/bin/status.sh"
    chown "$USER:$USER" "$DEPLOY_DIR/bin/status.sh"
    log_success "状态检查脚本创建完成"
}

# 启动服务
start_service() {
    log_info "启动服务..."
    
    systemctl enable "$SERVICE_NAME"
    systemctl start "$SERVICE_NAME"
    
    sleep 3
    
    if systemctl is-active --quiet "$SERVICE_NAME"; then
        log_success "服务启动成功"
    else
        log_error "服务启动失败"
        systemctl status "$SERVICE_NAME" --no-pager -l
        exit 1
    fi
}

# 显示部署信息
show_deployment_info() {
    log_info "部署完成信息:"
    echo "=================================="
    echo "服务名称: $SERVICE_NAME"
    echo "部署目录: $DEPLOY_DIR"
    echo "运行用户: $USER"
    echo "监听端口: $PORT"
    echo "=================================="
    echo ""
    echo "常用命令:"
    echo "  启动服务: systemctl start $SERVICE_NAME"
    echo "  停止服务: systemctl stop $SERVICE_NAME"
    echo "  重启服务: systemctl restart $SERVICE_NAME"
    echo "  查看状态: systemctl status $SERVICE_NAME"
    echo "  查看日志: journalctl -u $SERVICE_NAME -f"
    echo "  应用日志: tail -f $DEPLOY_DIR/logs/competition.log"
    echo ""
    echo "管理脚本:"
    echo "  启动: $DEPLOY_DIR/bin/start.sh"
    echo "  停止: $DEPLOY_DIR/bin/stop.sh"
    echo "  状态: $DEPLOY_DIR/bin/status.sh"
    echo ""
    echo "访问地址: http://服务器IP:$PORT"
}

# 主函数
main() {
    echo "=================================="
    echo "AI Competition Backend 部署脚本"
    echo "=================================="
    echo ""
    
    check_root
    create_user
    create_directories
    copy_jar
    create_config
    create_startup_script
    create_stop_script
    create_status_script
    create_systemd_service
    start_service
    show_deployment_info
    
    log_success "部署完成！"
}

# 执行主函数
main "$@"
