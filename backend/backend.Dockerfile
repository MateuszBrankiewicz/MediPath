FROM amazoncorretto:25-alpine
COPY backend/ /usr/src/backend
WORKDIR /usr/src/backend
RUN apk update && apk add maven
RUN ./mvnw package -Dmaven.test.skip=true
CMD ["java", "-jar", "target/medipathbackend-0.0.1.jar"]