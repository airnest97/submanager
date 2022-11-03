FROM openjdk:19
MAINTAINER submanager
EXPOSE 8080
ADD target/submanager.jar submanager.jar
ENTRYPOINT ["java", "-jar", "/submanager.jar"]