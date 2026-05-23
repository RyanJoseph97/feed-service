FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /build
ARG GITHUB_TOKEN
RUN mkdir -p /root/.m2 && \
    echo "<settings><servers><server><id>github</id><username>token</username><password>${GITHUB_TOKEN}</password></server></servers></settings>" \
    > /root/.m2/settings.xml
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
