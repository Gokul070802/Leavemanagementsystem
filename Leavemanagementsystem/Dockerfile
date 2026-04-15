FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace
COPY pom.xml ./
COPY Leavepal ./Leavepal
RUN mvn -DskipTests clean package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /workspace/target/prj1-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
