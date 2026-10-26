# 使用 Eclipse Temurin JDK 17（OpenJDK 官方已弃用，Temurin 是主流替代）
FROM eclipse-temurin:17-jre-alpine

# 设置维护者信息
LABEL maintainer="your-email@example.com"

# 设置时区为上海（解决容器内时间不一致问题）
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

# 设置工作目录
WORKDIR /app

# 复制 Maven 构建的 jar 包到容器中
COPY campus-backend/target/campus-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数并启动应用
ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Shanghai", "app.jar"]
