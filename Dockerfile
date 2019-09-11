FROM openjdk:11-jre-slim

WORKDIR /app

COPY example/build/libs /app

CMD java -jar example-0.3-SNAPSHOT.jar $token $ticker $interval $max_volume $use_sandbox