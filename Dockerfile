# Stage 1: Download Dependencies to cache them for futher builds
FROM maven:3.9.11-eclipse-temurin-25-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Stage 2: Build project
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 3: Deploy
FROM tomcat:11.0.13-jdk25-temurin-noble
COPY --from=builder /app/target/webapp.war $CATALINA_HOME/webapps/ROOT.war
EXPOSE 8080