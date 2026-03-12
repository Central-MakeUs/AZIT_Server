FROM eclipse-temurin:21-jre-jammy

# 뉴렐릭
RUN apt-get update && apt-get install -y curl && \
    curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar && \
    rm -rf /var/lib/apt/lists/*

# 빌드 시 생성된 jar 파일을 컨테이너 내부로 복사 (plain jar 제외)
ARG JAR_FILE=build/libs/*[!-plain].jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-javaagent:/newrelic.jar", "-jar", "/app.jar"]