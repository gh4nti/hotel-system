# Hotel System

A desktop hotel management app built with JavaFX, Maven, and SQLite.

The application supports role-based login, room management for admins, and room browsing/booking for guests.

## Tech Stack

- Java 17
- JavaFX 21 (`javafx-controls`, `javafx-fxml`)
- Maven
- SQLite (`sqlite-jdbc`)

## Implemented Features

- Login flow using username/password
- Role-based navigation:
    - Admin -> Admin Dashboard
    - Guest -> Guest Dashboard
- View all pre-seeded rooms in a table (room number, floor, type, price, availability)
- Book a room by type, with the system randomly assigning an available room of that type
- Room inventory is automatically seeded at startup: 10 floors plus ground, 15 rooms per level
- Light/Dark theme auto-selection based on OS settings

## Project Structure

```text
hotel-system/
  pom.xml
  src/main/java/com/hotel/
    Main.java
    InitDB.java
    controllers/
    dao/
    database/
    models/
    ui/
  src/main/resources/
    style.css
    theme-light.css
    theme-dark.css
    views/
```

## Prerequisites

- JDK 17+
- Maven 3.8+

Verify installation:

```bash
java -version
mvn -version
```

## Build

```bash
mvn clean package
```

## Database Setup (Required Before Login)

The app uses a local SQLite file database named `hotel.db` (created in the project root when first used).

### 1. Create tables

Run the DB initializer class once:

```bash
mvn -Dexec.mainClass=com.hotel.InitDB -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

### 2. Insert at least one user

There are no default users seeded by the project. Add users manually using SQLite.

Example SQL:

```sql
INSERT INTO User(username, password, role) VALUES ('admin', 'admin123', 'admin');
INSERT INTO User(username, password, role) VALUES ('guest', 'guest123', 'guest');
```

Optional example with `sqlite3` CLI:

```bash
sqlite3 hotel.db
INSERT INTO User(username, password, role) VALUES ('admin', 'admin123', 'admin');
INSERT INTO User(username, password, role) VALUES ('guest', 'guest123', 'guest');
.quit
```

## Run the Application

```bash
mvn javafx:run
```

Entry point:

- `com.hotel.Main`

## How It Works

- `LoginView.fxml` is loaded on startup.
- On successful login, users are routed by role.
- Room inventory is recreated/seeded from `InitDB` when needed, so the hotel always starts with the expected room layout.
- Room and booking operations are handled through DAO classes:
    - `UserDAO`
    - `RoomDAO`
    - `BookingDAO`
- DB connection is managed by `DatabaseConnection` with URL `jdbc:sqlite:hotel.db`.

## Known Limitations

- Passwords are stored in plain text (not recommended for production).
- Booking date is currently hardcoded in `ViewRoomsController`.
- No automated tests are included yet.

## Troubleshooting

- If login always fails, verify that:
    - `hotel.db` exists
    - tables were created
    - at least one user row exists in `User`
- If JavaFX launch fails, confirm `pom.xml` still points to:

```xml
<mainClass>com.hotel.Main</mainClass>
```
