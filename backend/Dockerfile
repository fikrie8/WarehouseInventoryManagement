# Stage 1: Build the app inside Docker
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# Copies your main backend files into Docker
COPY . .
# Navigates to where your pom.xml is and builds it
RUN cd fikrie && mvn clean package -DskipTests

# Stage 2: Create the final lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Safely extracts the jar from the internal build folder
COPY --from=build /app/fikrie/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
