FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/BackEnd_SGALB-0.0.1-SNAPSHOT.jar /app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app.jar"]