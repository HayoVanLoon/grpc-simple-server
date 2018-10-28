FROM openjdk:8-alpine
COPY ./target/simpleserver-0.0.1-jar-with-dependencies.jar /app/
WORKDIR /app
#RUN javac Main.java
CMD ["java", "-jar", "simpleserver-0.0.1-jar-with-dependencies.jar"]