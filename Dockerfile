FROM openjdk:11-jre-slim

WORKDIR /app

COPY example/build/libs /app

CMD java -jar example.jar $token $ticker $interval $max_volume $use_sandbox