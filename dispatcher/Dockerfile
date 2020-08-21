FROM java

COPY target/paillasse-1.0-SNAPSHOT-jar-with-dependencies.jar /app/server.jar

COPY resources /app/resources

WORKDIR /app

CMD ["java", "-jar", "/app/server.jar", "-d", "/data/data_example.csv", "-e", "/data/config_example.json", "-p", "8090"]
