FROM maven:3-jdk-8-alpine

COPY . /

RUN mvn clean install -DskipTests
RUN ls target

CMD ["/usr/bin/java", "-jar", "target/fiware-demo-coordinator-1.0.0.jar"]