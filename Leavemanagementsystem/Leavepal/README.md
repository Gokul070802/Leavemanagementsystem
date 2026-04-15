# Leave Management System

This module now keeps only the core application files for the Spring Boot backend and the source frontend.

## Structure

- `../frontend` is the editable source frontend.
- `src/main/resources/static` is the backend-served frontend copy packaged into the JAR.
- `src/main/java` and `src/test/java` contain the Spring Boot application code.
- `pom.xml` is the backend build file.

## Local Run

From the `Leavepal` folder:

```bash
mvn clean install
mvn spring-boot:run
```

The application starts on `http://localhost:8081`.

## Frontend Sync

If you edit files in the root `frontend` folder, copy them into Spring static assets with the workspace task `Copy Frontend to Backend`.