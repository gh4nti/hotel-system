# Hotel System

Desktop hotel booking and management system built with JavaFX, Maven, and SQLite.

## Overview

This project supports two user roles:

- Admin: view currently booked rooms and modify room type/price tiers.
- Guest: register/login, book rooms with date range selection, upgrade rooms, and check out.

The database is initialized automatically on app startup, including room seeding and default admin provisioning.

## Tech Stack

- Java 17
- JavaFX 21 (`javafx-controls`, `javafx-fxml`)
- Maven
- SQLite (`sqlite-jdbc`)

## Final Features

- Progressive auth flow:
    - Enter username first
    - Existing user -> login
    - New user -> register as guest
- Role-based dashboard routing after login:
    - Admin -> `AdminDashboard.fxml`
    - Guest -> `GuestDashboard.fxml`
- Automatic DB initialization in `Main` via `InitDB.initializeDatabase()`
- Auto-seeded default admin account (always ensured):
    - Username: `admin`
    - Password: `123`
    - Role: `admin`
- Room inventory seeding logic:
    - Floors: ground (`0`) through `10`
    - 15 rooms per floor
    - 165 total rooms
    - Tier distribution per floor:
        - Single x4
        - Double x4
        - Deluxe x4
        - Suite x2
        - Presidential Suite x1
- Guest booking flow:
    - Select room type
    - Select check-in and check-out dates
    - Auto-calculated nights and total price
    - System assigns a random available room from selected tier
- Guest post-booking actions:
    - Upgrade to higher-tier room (pay price difference)
    - Checkout (booking removed and room marked available)
- Admin capabilities:
    - View all booked rooms with username, dates, price/night, and total
    - Modify room type names and prices globally
- Theme behavior:
    - Base stylesheet + light/dark theme stylesheet
    - Theme auto-detected from OS settings on startup

## Project Structure

```text
hotel-system/
  pom.xml
  README.md
  src/
    main/
      java/com/hotel/
        InitDB.java
        Main.java
        controllers/
          AdminController.java
          GuestController.java
          LoginController.java
          ViewRoomsController.java
        dao/
          BookingDAO.java
          RoomDAO.java
          UserDAO.java
        database/
          DatabaseConnection.java
        models/
          BookedRoomInfo.java
          Booking.java
          Room.java
          UpgradeOption.java
          User.java
          UserBookingInfo.java
        ui/
          ThemeManager.java
      resources/
        style.css
        theme-dark.css
        theme-light.css
        views/
          AdminDashboard.fxml
          GuestDashboard.fxml
          LoginView.fxml
          ViewRooms.fxml
```

## Prerequisites

- JDK 17+
- Maven 3.8+

Optional checks:

```bash
java -version
mvn -version
```

## Build

```bash
mvn clean package
```

## Run

```bash
mvn javafx:run
```

Entry point: `com.hotel.Main`

## Database Notes

- SQLite file: `hotel.db` (created in project root)
- Tables are auto-created/updated at startup:
    - `User`
    - `Room`
    - `Booking`
- Booking schema migration is handled in initializer (`check_in_date`, `check_out_date` are ensured)
- Room data may be reset/reseeded when schema/inventory is not in expected final shape

You do not need to run `InitDB` manually for normal app usage.

## Default Access

- Admin login:
    - Username: `admin`
    - Password: `123`

Guest accounts are created through the UI registration flow.

## Known Limitations

- Passwords are stored in plain text (no hashing yet).
- `currentUser` is static in controller state (single-session desktop assumption).
- No automated tests are currently included.

## Troubleshooting

- If login/booking behaves unexpectedly:
    - Delete `hotel.db` and rerun app to rebuild fresh schema/data.
- If JavaFX app does not launch:
    - Ensure `pom.xml` has JavaFX plugin main class set to `com.hotel.Main`.
- If styles are missing:
    - Confirm resources exist under `src/main/resources` and run from Maven (`mvn javafx:run`).
