# 使用官方 OpenJDK 17 基础镜像
FROM openjdk:17-jdk-slim

# 设置维护者信息
LABEL maintainer="your-email@example.com"

# 设置工作目录
WORKDIR /app

# 复制 Maven 构建的 jar 包到容器中
COPY campus-backend/target/campus-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数并启动应用
ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "-Dspring.profiles.active=prod", "app.jar"]
