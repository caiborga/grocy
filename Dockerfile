# --- build stage: node + maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Node.js + npm installieren (Debian base)
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates curl gnupg && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    node -v && npm -v && \
    rm -rf /var/lib/apt/lists/*

# Build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# --- run stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
