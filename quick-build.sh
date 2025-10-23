#!/bin/bash

# AI Competition Backend 快速编译脚本
# 适用于开发环境快速编译

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}AI Competition Backend 快速编译${NC}"
echo "=================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: Java未安装${NC}"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven未安装${NC}"
    exit 1
fi

echo -e "${YELLOW}开始编译...${NC}"

# 编译项目
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}编译成功！${NC}"
    echo ""
    echo "JAR文件位置: target/competition-backend-1.0.0.jar"
    echo ""
    echo "运行命令:"
    echo "  java -jar target/competition-backend-1.0.0.jar"
    echo ""
    echo "指定配置文件运行:"
    echo "  java -jar target/competition-backend-1.0.0.jar --spring.profiles.active=prod"
else
    echo -e "${RED}编译失败！${NC}"
    exit 1
fi
