# 多阶段构建：先打包，再运行
# ============ Stage 1: 构建 jar ============
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn package -DskipTests -B -q

# ============ Stage 2: 运行 ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Railway 自动注入 MYSQL_URL 等环境变量，我们用 prod profile + 环境变量覆盖
COPY --from=build /app/target/gym-server-1.0.0.jar app.jar

# 健康检查（Railway 用这个判断服务是否就绪）
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/dashboard/summary || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar", "--spring.profiles.active=prod"]
