#!/bin/bash

# 南瓜云盘启动脚本

echo "========================================"
echo "       南瓜云盘启动脚本"
echo "========================================"
echo

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未检测到Java环境，请先安装JDK 8或更高版本"
    echo "下载地址: https://adoptium.net/"
    exit 1
fi

echo "检测到Java环境:"
java -version
echo

# 检查是否已构建
if [ ! -f "target/nanguayunpan-1.0.jar" ]; then
    echo "警告: 未找到构建文件，尝试构建项目..."
    echo

    # 检查Maven是否安装
    if ! command -v mvn &> /dev/null; then
        echo "错误: 未检测到Maven，请先安装Maven或手动构建项目"
        echo "下载地址: https://maven.apache.org/download.cgi"
        exit 1
    fi

    echo "开始构建项目..."
    mvn clean package -DskipTests

    if [ $? -ne 0 ]; then
        echo "错误: 项目构建失败，请检查错误信息"
        exit 1
    fi

    echo "项目构建成功!"
    echo
fi

echo "启动南瓜云盘服务..."
echo "服务地址: http://localhost:7090"
echo "API地址: http://localhost:7090/api"
echo
echo "按 Ctrl+C 停止服务"
echo "========================================"
echo

# 启动应用
java -jar target/nanguayunpan-1.0.jar