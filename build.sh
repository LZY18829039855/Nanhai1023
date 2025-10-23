#!/bin/bash

# AI Competition Backend 编译脚本
# 适用于Linux环境部署

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目信息
PROJECT_NAME="competition-backend"
VERSION="1.0.0"
JAR_NAME="${PROJECT_NAME}-${VERSION}.jar"
TARGET_DIR="target"

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

# 检查环境
check_environment() {
    log_info "检查编译环境..."
    
    # 检查Java版本
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装JDK 8或更高版本"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 8 ]; then
        log_error "Java版本过低，需要JDK 8或更高版本，当前版本: $JAVA_VERSION"
        exit 1
    fi
    
    log_success "Java版本检查通过: $(java -version 2>&1 | head -n 1)"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请先安装Maven 3.6+"
        exit 1
    fi
    
    log_success "Maven版本: $(mvn -version | head -n 1)"
}

# 清理项目
clean_project() {
    log_info "清理项目..."
    mvn clean
    log_success "项目清理完成"
}

# 编译项目
compile_project() {
    log_info "开始编译项目..."
    
    # 编译并打包
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        log_success "项目编译成功"
    else
        log_error "项目编译失败"
        exit 1
    fi
}

# 检查JAR文件
check_jar() {
    log_info "检查生成的JAR文件..."
    
    if [ -f "$TARGET_DIR/$JAR_NAME" ]; then
        JAR_SIZE=$(du -h "$TARGET_DIR/$JAR_NAME" | cut -f1)
        log_success "JAR文件生成成功: $TARGET_DIR/$JAR_NAME (大小: $JAR_SIZE)"
    else
        log_error "JAR文件未找到: $TARGET_DIR/$JAR_NAME"
        exit 1
    fi
}

# 显示部署信息
show_deployment_info() {
    log_info "部署信息:"
    echo "=================================="
    echo "项目名称: $PROJECT_NAME"
    echo "版本: $VERSION"
    echo "JAR文件: $TARGET_DIR/$JAR_NAME"
    echo "运行命令: java -jar $TARGET_DIR/$JAR_NAME"
    echo "=================================="
    echo ""
    echo "常用运行参数:"
    echo "  --server.port=8080                    # 指定端口"
    echo "  --spring.profiles.active=prod         # 指定环境"
    echo "  --spring.datasource.url=jdbc:mysql://localhost:3306/nanhai  # 指定数据库"
    echo ""
    echo "示例运行命令:"
    echo "  java -jar $TARGET_DIR/$JAR_NAME --spring.profiles.active=prod"
    echo "  java -Xms512m -Xmx1024m -jar $TARGET_DIR/$JAR_NAME"
}

# 主函数
main() {
    echo "=================================="
    echo "AI Competition Backend 编译脚本"
    echo "=================================="
    echo ""
    
    check_environment
    clean_project
    compile_project
    check_jar
    show_deployment_info
    
    log_success "编译完成！"
}

# 执行主函数
main "$@"
