FROM openjdk:17
LABEL maintainer="Kirtesh Wani wanikirtesh@gmail.com"
EXPOSE 9191
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir "requests"
RUN mkdir "processes"
RUN mkdir "newFixtures"
ENTRYPOINT ["java","-jar","/app.jar"]