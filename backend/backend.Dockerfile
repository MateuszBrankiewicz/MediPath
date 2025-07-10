FROM openjdk:25-slim-bookworm
COPY backend/ /usr/src/backend
WORKDIR /usr/src/backend
RUN apt update -y && apt install maven -y
RUN ./mvnw package
CMD ["java", "-jar", "target/medipathbackend-0.0.1.jar"]