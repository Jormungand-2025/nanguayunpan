# 南瓜云盘 - 部署指南

## 项目简介

南瓜云盘是一个基于 Spring Boot 的文件存储和分享平台，提供用户管理、文件上传下载、分享功能。

## 环境要求

### 系统要求
- **操作系统**: Windows 10/11, Linux, macOS
- **Java版本**: JDK 8 或更高版本
- **内存**: 至少 2GB 可用内存
- **磁盘空间**: 至少 1GB 可用空间

### 软件依赖
- **MySQL**: 5.7 或 8.0 版本
- **Redis**: 3.0 或更高版本
- **Maven**: 3.6 或更高版本（用于构建）

## 快速开始

### 1. 环境准备

#### 安装 Java
```bash
# Windows: 下载并安装 JDK 8+
# 验证安装
java -version
```

#### 安装 MySQL
```bash
# Windows: 下载 MySQL Installer
# Linux (Ubuntu):
sudo apt update
sudo apt install mysql-server

# 启动 MySQL 服务
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### 安装 Redis
```bash
# Windows: 下载 Redis for Windows
# Linux (Ubuntu):
sudo apt install redis-server

# 启动 Redis 服务
sudo systemctl start redis
sudo systemctl enable redis
```

### 2. 数据库配置

#### 创建数据库
```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE nanguayunpan DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE nanguayunpan;

-- 执行初始化脚本
source /path/to/schema.sql
```

#### 初始化脚本位置
项目中的数据库初始化脚本位于：
`src/main/resources/sql/schema.sql`

### 3. 项目配置

#### 修改配置文件
编辑 `src/main/resources/application.properties`：

```properties
# 数据库配置（根据实际环境修改）
spring.datasource.url=jdbc:mysql://localhost:3306/nanguayunpan?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=your_password

# Redis配置（根据实际环境修改）
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=

# 邮件配置（根据实际邮箱修改）
spring.mail.host=smtp.qq.com
spring.mail.port=465
spring.mail.username=your_email@qq.com
spring.mail.password=your_email_password

# 文件存储路径（根据实际路径修改）
project.folder=/path/to/your/storage/

# JWT密钥（生产环境请修改）
jwt.secret=your-production-secret-key
```

#### 重要配置说明
- **数据库密码**: 修改为实际的 MySQL 密码
- **Redis密码**: 如果 Redis 设置了密码，需要配置
- **邮箱配置**: 需要配置真实的邮箱和授权码
- **文件存储路径**: 确保路径存在且有写入权限

### 4. 构建项目

#### 使用 Maven 构建
```bash
# 进入项目根目录
cd nanguayunpan

# 清理并打包
mvn clean package -DskipTests

# 构建成功后，会在 target 目录生成 jar 文件
# nanguayunpan-1.0.jar
```

#### 直接下载预构建版本（可选）
如果不想构建，可以直接下载预编译的 jar 文件。

### 5. 运行项目

#### 方式一：使用 Java 命令运行
```bash
# 进入 target 目录
cd target

# 运行项目
java -jar nanguayunpan-1.0.jar

# 或者指定配置文件
java -jar nanguayunpan-1.0.jar --spring.config.location=application.properties
```

#### 方式二：使用 Docker 运行（可选）
```bash
# 构建 Docker 镜像
docker build -t nanguayunpan .

# 运行容器
docker run -d -p 7090:7090 --name nanguayunpan nanguayunpan
```

### 6. 验证部署

#### 检查服务状态
访问健康检查接口：
```bash
curl http://localhost:7090/api/health
```

#### 测试用户注册
```bash
# 发送邮箱验证码
curl -X POST http://localhost:7090/api/user/sendEmailCode \
  -d "email=test@example.com&type=0"

# 用户注册（需要先获取验证码）
curl -X POST http://localhost:7090/api/user/register \
  -d "email=test@example.com&nickName=测试用户&password=123456&checkCode=123456"
```

## 生产环境部署

### 1. 安全配置

#### 修改默认密码
- 修改数据库默认密码
- 修改 Redis 密码（如果使用）
- 生成强密码的 JWT 密钥

#### 配置 HTTPS
```properties
# 启用 HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

### 2. 性能优化

#### 数据库连接池配置
```properties
# 根据实际负载调整
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
```

#### 文件上传限制
```properties
# 调整文件上传大小限制
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### 3. 使用系统服务运行

#### Windows 服务
创建批处理文件 `start.bat`：
```batch
@echo off
java -jar nanguayunpan-1.0.jar
pause
```

#### Linux Systemd 服务
创建服务文件 `/etc/systemd/system/nanguayunpan.service`：
```ini
[Unit]
Description=南瓜云盘服务
After=network.target

[Service]
Type=simple
User=nginx
ExecStart=/usr/bin/java -jar /opt/nanguayunpan/nanguayunpan-1.0.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

启用并启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable nanguayunpan
sudo systemctl start nanguayunpan
```

## 故障排除

### 常见问题

#### 1. 端口被占用
```bash
# 检查端口占用
netstat -ano | findstr :7090  # Windows
lsof -i :7090                 # Linux/macOS

# 修改端口
java -jar nanguayunpan-1.0.jar --server.port=8080
```

#### 2. 数据库连接失败
- 检查 MySQL 服务是否启动
- 验证数据库连接参数
- 检查防火墙设置

#### 3. 文件上传失败
- 检查文件存储路径权限
- 验证磁盘空间是否充足
- 检查文件大小限制

#### 4. 邮件发送失败
- 检查邮箱配置是否正确
- 验证邮箱授权码
- 检查网络连接

### 日志查看

#### 查看运行日志
```bash
# 默认日志路径
tail -f logs/application.log

# 或查看控制台输出
java -jar nanguayunpan-1.0.jar
```

#### 日志配置
编辑 `src/main/resources/logback-spring.xml` 调整日志级别和输出路径。

## 更新和维护

### 版本更新
1. 备份数据库和上传的文件
2. 停止当前服务
3. 部署新版本
4. 启动服务
5. 验证功能正常

### 数据备份
```bash
# 备份数据库
mysqldump -u root -p nanguayunpan > backup_$(date +%Y%m%d).sql

# 备份上传的文件
tar -czf files_backup_$(date +%Y%m%d).tar.gz /path/to/storage/
```

## 技术支持

如有问题，请联系：
- 项目维护团队：南瓜云盘开发团队
- 文档版本：v1.0
- 最后更新：2026-02-27

---

**注意**: 在生产环境部署前，请务必修改所有默认密码和密钥，并进行充分测试。