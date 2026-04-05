package com.hotel;

import com.hotel.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class InitDB {
	private static final String[] ROOM_TYPES = {
			"Single",
			"Double",
			"Deluxe",
			"Suite",
			"Presidential Suite"
	};
	private static final double[] ROOM_PRICES = { 1000.0, 2000.0, 3000.0, 4500.0, 8000.0 };
	private static final int[] ROOM_COUNTS = { 4, 4, 4, 2, 1 };

	public static void initializeDatabase() throws Exception {
		try (Connection conn = DatabaseConnection.connect(); Statement stmt = conn.createStatement()) {
			stmt.execute(
					"CREATE TABLE IF NOT EXISTS User (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, role TEXT)");

			ensureDefaultAdmin(conn);

			boolean needsRoomMigration = !roomTableHasNewSchema(conn);
			if (needsRoomMigration) {
				stmt.execute("DROP TABLE IF EXISTS Booking");
				stmt.execute("DROP TABLE IF EXISTS Room");
			}

			stmt.execute(
					"CREATE TABLE IF NOT EXISTS Room (id INTEGER PRIMARY KEY AUTOINCREMENT, room_number TEXT UNIQUE, floor INTEGER, type TEXT, price REAL, available INTEGER)");
			stmt.execute(
					"CREATE TABLE IF NOT EXISTS Booking (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, room_id INTEGER, date TEXT, check_in_date TEXT, check_out_date TEXT)");

			ensureBookingSchema(conn);

			seedRoomsIfNeeded(conn);
		}
	}

	private static void ensureBookingSchema(Connection conn) throws Exception {
		boolean hasCheckIn = false;
		boolean hasCheckOut = false;

		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(Booking)")) {
			while (rs.next()) {
				String columnName = rs.getString("name");
				if ("check_in_date".equalsIgnoreCase(columnName)) {
					hasCheckIn = true;
				}
				if ("check_out_date".equalsIgnoreCase(columnName)) {
					hasCheckOut = true;
				}
			}
		}

		try (Statement stmt = conn.createStatement()) {
			if (!hasCheckIn) {
				stmt.execute("ALTER TABLE Booking ADD COLUMN check_in_date TEXT");
			}
			if (!hasCheckOut) {
				stmt.execute("ALTER TABLE Booking ADD COLUMN check_out_date TEXT");
			}
			stmt.execute("UPDATE Booking SET check_in_date = date WHERE check_in_date IS NULL AND date IS NOT NULL");
		}
	}

	private static void ensureDefaultAdmin(Connection conn) throws Exception {
		String updateSql = "UPDATE User SET username=?, password=? WHERE role='admin'";
		try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
			stmt.setString(1, "admin");
			stmt.setString(2, "123");
			int updatedRows = stmt.executeUpdate();

			if (updatedRows == 0) {
				String insertSql = "INSERT INTO User(username, password, role) VALUES(?,?,?)";
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
					insertStmt.setString(1, "admin");
					insertStmt.setString(2, "123");
					insertStmt.setString(3, "admin");
					insertStmt.executeUpdate();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		initializeDatabase();

		System.out.println("Tables created!");
	}

	private static boolean roomTableHasNewSchema(Connection conn) throws Exception {
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(Room)")) {
			boolean hasRoomNumber = false;
			boolean hasFloor = false;

			while (rs.next()) {
				String columnName = rs.getString("name");
				if ("room_number".equalsIgnoreCase(columnName)) {
					hasRoomNumber = true;
				}
				if ("floor".equalsIgnoreCase(columnName)) {
					hasFloor = true;
				}
			}

			return hasRoomNumber && hasFloor;
		}
	}

	private static void seedRoomsIfNeeded(Connection conn) throws Exception {
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Room")) {
			int roomCount = rs.next() ? rs.getInt("total") : 0;
			if (roomCount == 165) {
				return;
			}
		}

		try (Statement stmt = conn.createStatement()) {
			stmt.execute("DELETE FROM Booking");
			stmt.execute("DELETE FROM Room");
		}

		String insertSql = "INSERT INTO Room(room_number, floor, type, price, available) VALUES(?,?,?,?,?)";
		try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
			for (int floor = 0; floor <= 10; floor++) {
				int roomIndex = 1;
				for (int typeIndex = 0; typeIndex < ROOM_TYPES.length; typeIndex++) {
					for (int count = 0; count < ROOM_COUNTS[typeIndex]; count++) {
						String roomNumber = formatRoomNumber(floor, roomIndex++);
						stmt.setString(1, roomNumber);
						stmt.setInt(2, floor);
						stmt.setString(3, ROOM_TYPES[typeIndex]);
						stmt.setDouble(4, ROOM_PRICES[typeIndex]);
						stmt.setInt(5, 1);
						stmt.addBatch();
					}
				}
				stmt.executeBatch();
			}
		}
	}

	private static String formatRoomNumber(int floor, int roomIndex) {
		String prefix = floor == 0 ? "0" : String.valueOf(floor);
		return prefix + String.format("%02d", roomIndex);
	}
}