FROM openjdk:19
LABEL maintainer="Kirtesh Wani wanikirtesh@gmail.com"
MAINTAINER Kirtesh Wani wanikirtesh@gmail.com
EXPOSE 9191
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} anyMocker.jar
RUN mkdir "requests"
RUN mkdir "processes"
RUN mkdir "specs"
ENTRYPOINT ["java","-jar","/anyMocker.jar"]