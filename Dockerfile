FROM eclipse-temurin:21-jre-jammy

# 빌드 시 생성된 jar 파일을 컨테이너 내부로 복사 (plain jar 제외)
ARG JAR_FILE=build/libs/*[!-plain].jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]