FROM gradle:6.2.0-jdk8 as diamant_backend_builder
ADD . .
RUN gradle bootJar

FROM openjdk:8-slim as diamant_backend_runer
COPY --from=diamant_backend_builder /home/gradle/build/libs/backend_service.jar /

ENV SERVER_PORT=8181 \
    APP_SERVER_BASE_URL=http://localhost:8181

CMD ["java", "-jar", "/backend_service.jar"]

EXPOSE 8181
