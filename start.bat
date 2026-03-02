@echo off
echo ========================================
echo       南瓜云盘启动脚本
echo ========================================
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未检测到Java环境，请先安装JDK 8或更高版本
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)

echo 检测到Java环境:
java -version
echo.

REM 检查是否已构建
if not exist "target\nanguayunpan-1.0.jar" (
    echo 警告: 未找到构建文件，尝试构建项目...
    echo.

    REM 检查Maven是否安装
    mvn -version >nul 2>&1
    if errorlevel 1 (
        echo 错误: 未检测到Maven，请先安装Maven或手动构建项目
        echo 下载地址: https://maven.apache.org/download.cgi
        pause
        exit /b 1
    )

    echo 开始构建项目...
    mvn clean package -DskipTests

    if errorlevel 1 (
        echo 错误: 项目构建失败，请检查错误信息
        pause
        exit /b 1
    )

    echo 项目构建成功!
    echo.
)

echo 启动南瓜云盘服务...
echo 服务地址: http://localhost:7090
echo API地址: http://localhost:7090/api
echo.
echo 按 Ctrl+C 停止服务
echo ========================================
echo.

REM 启动应用
java -jar target\nanguayunpan-1.0.jar

pause