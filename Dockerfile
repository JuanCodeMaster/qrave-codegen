FROM maven:3.9-amazoncorretto-21 AS builder


WORKDIR /api

COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /api/target/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
RUN mvn package

FROM amazoncorretto:21
COPY --from=builder /api/target/*.jar app.jar
EXPOSE 8081
CMD ["java" ,"-jar", "app.jar"]
