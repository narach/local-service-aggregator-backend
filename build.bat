./gradlew build
docker build --build-arg JAR_FILE=./build/libs/aggregator-0.0.1-SNAPSHOT.jar -t aggregator-docker .
