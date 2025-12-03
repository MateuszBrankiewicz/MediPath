FROM maven:4.0.0-rc-5-ibm-semeru-25-noble
COPY backend/ /usr/src/backend
WORKDIR /usr/src/backend
RUN ./mvnw package
CMD ["java", "-jar", "target/medipathbackend-0.0.1.jar"]