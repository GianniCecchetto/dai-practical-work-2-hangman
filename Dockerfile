FROM eclipse-temurin:21-jre
WORKDIR /app
COPY /hangman/target/hangman-1.0-SNAPSHOT.jar /app/hangman.jar
EXPOSE 1902
ENTRYPOINT [ "java", "-jar", "hangman.jar" ]
