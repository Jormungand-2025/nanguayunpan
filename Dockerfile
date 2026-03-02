# 使用官方 OpenJDK 8 运行时作为父镜像
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 将 jar 文件复制到容器中
COPY target/nanguayunpan-1.0.jar app.jar

# 创建文件存储目录
RUN mkdir -p /app/storage

# 暴露端口
EXPOSE 7090

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 运行 jar 文件
ENTRYPOINT ["java", "-jar", "app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:7090/api/health || exit 1