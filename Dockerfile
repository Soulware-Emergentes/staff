FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /src
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests && cp target/*.jar /src/app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /src/app.jar ./app.jar
RUN mkdir logs
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-jar", "app.jar"]
