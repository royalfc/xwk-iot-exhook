FROM maven:3.8.5-openjdk-17

# 包名称
ARG PACKAGE_NAME=exhook-svr-1.0-jar-with-dependencies

# RUN ["ls", "-alh"]

WORKDIR /app
RUN ["ls", "-alh", "target/"]
COPY target/${PACKAGE_NAME}.jar /app/${PACKAGE_NAME}.jar
RUN chmod +x /app/${PACKAGE_NAME}.jar


# 设置默认环境变量
ENV JAVA_OPTS=""
# ENV SPRING_PROFILES_ACTIVE="dev"
# 设置默认端口值（如果没有从环境变量中传递）
# ENV PORT 8000
# 暴露端口
EXPOSE 9000

# 启动应用程序时添加环境变量参数
# java -jar target/exhook-svr-1.0-jar-with-dependencies.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/${PACKAGE_NAME}.jar"]