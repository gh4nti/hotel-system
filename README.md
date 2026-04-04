# Hotel Management System

A JavaFX-based desktop application for hotel management, built with Maven and Java 17.

## Tech Stack

- Java 17
- JavaFX 21
- Maven
- SQLite (via `sqlite-jdbc`)

## Project Structure

```text
hotel-system/
  pom.xml
  src/
    main/
      java/
        com/
          hotel/
            Main.java
      resources/
    test/
      java/
```

## Prerequisites

- JDK 17 installed and available on `PATH`
- Maven installed and available on `PATH`

Check versions:

```bash
java -version
mvn -version
```

## Build

From the `hotel-system` directory:

```bash
mvn clean package
```

## Run

Use the JavaFX Maven plugin:

```bash
mvn javafx:run
```

## Current Application Behavior

The app currently launches a simple JavaFX window showing:

- Title: `Hotel System`
- Label text: `Hotel Management System`

## Troubleshooting

If `mvn javafx:run` fails with a main class error, verify the `mainClass` in `pom.xml` matches your package and class.

Current source entry point is:

- `com.hotel.Main`

If needed, update the JavaFX plugin configuration:

```xml
<mainClass>com.hotel.Main</mainClass>
```

## Next Steps

Suggested enhancements:

- Add booking, room, and customer modules
- Connect UI actions to SQLite persistence
- Add unit tests under `src/test/java`
- Add FXML layouts and controllers for richer UI
