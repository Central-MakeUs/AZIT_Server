FROM eclipse-temurin:21-jre-jammy

# 뉴렐릭
ARG NR_AGENT_VERSION=9.1.0
RUN apt-get update && apt-get install -y curl && \
    curl -fSL https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NR_AGENT_VERSION}/newrelic-agent-${NR_AGENT_VERSION}.jar -o /newrelic.jar && \
    rm -rf /var/lib/apt/lists/*

# 빌드 시 생성된 jar 파일을 컨테이너 내부로 복사 (plain jar 제외)
ARG JAR_FILE=build/libs/*[!-plain].jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/newrelic.jar -jar /app.jar"]