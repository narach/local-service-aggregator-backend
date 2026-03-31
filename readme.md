### How to run the project
Java 21 is required.

1. Configure local DB credentials in `src/main/resources/application-local.properties`.
2. Start the app with local profile:

```bash
./gradlew bootRun --args="--spring.profiles.active=local --spring.config.import= --spring.cloud.aws.parameterstore.enabled=false"
```

### How to run DB migrations after schema changes
Liquibase runs on app startup.

1. Add a new changelog file under `src/main/resources/db/changelog`.
2. Include it in `src/main/resources/db/changelog/db.changelog-master.yaml`.
3. Run migrations against local profile:

```bash
./gradlew bootRun --args="--spring.profiles.active=local --spring.config.import= --spring.cloud.aws.parameterstore.enabled=false"
```

4. Wait for a log line similar to:
   `Liquibase: Update has been successful`
5. Stop the app with `Ctrl + C`.

