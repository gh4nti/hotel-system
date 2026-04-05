package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.BookedRoomInfo;
import com.hotel.models.Room;
import com.hotel.models.UpgradeOption;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoomDAO {

	public List<Room> getAllRooms() throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT * FROM Room ORDER BY floor, CAST(room_number AS INTEGER)";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		List<Room> rooms = new ArrayList<>();

		while (rs.next()) {
			Room room = new Room(
					rs.getInt("id"),
					rs.getString("room_number"),
					rs.getInt("floor"),
					rs.getString("type"),
					rs.getDouble("price"),
					rs.getInt("available") == 1);
			rooms.add(room);
		}

		conn.close();
		return rooms;
	}

	public Map<String, Integer> getAvailableCountsByType() throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT type, SUM(CASE WHEN available = 1 THEN 1 ELSE 0 END) AS available_count, MIN(id) AS sort_id FROM Room GROUP BY type ORDER BY sort_id";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		Map<String, Integer> counts = new LinkedHashMap<>();
		while (rs.next()) {
			counts.put(rs.getString("type"), rs.getInt("available_count"));
		}

		conn.close();
		return counts;
	}

	public List<String> getRoomTypes() throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT type, MIN(id) AS sort_id FROM Room GROUP BY type ORDER BY sort_id";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		List<String> types = new ArrayList<>();
		while (rs.next()) {
			types.add(rs.getString("type"));
		}

		conn.close();
		return types;
	}

	public double getPriceForType(String type) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT price FROM Room WHERE type = ? LIMIT 1";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, type);
		ResultSet rs = stmt.executeQuery();

		double price = rs.next() ? rs.getDouble("price") : 0.0;
		conn.close();
		return price;
	}

	public void updateRoomTypeAndPrice(String currentType, String newType, double newPrice) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "UPDATE Room SET type = ?, price = ? WHERE type = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, newType);
		stmt.setDouble(2, newPrice);
		stmt.setString(3, currentType);
		stmt.executeUpdate();

		conn.close();
	}

	public Room getRandomAvailableRoomByType(String type) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT * FROM Room WHERE type = ? AND available = 1 ORDER BY RANDOM() LIMIT 1";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, type);
		ResultSet rs = stmt.executeQuery();

		Room room = null;
		if (rs.next()) {
			room = new Room(
					rs.getInt("id"),
					rs.getString("room_number"),
					rs.getInt("floor"),
					rs.getString("type"),
					rs.getDouble("price"),
					rs.getInt("available") == 1);
		}

		conn.close();
		return room;
	}

	public void markRoomUnavailable(int roomId) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "UPDATE Room SET available = 0 WHERE id = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, roomId);
		stmt.executeUpdate();
		conn.close();
	}

	public List<BookedRoomInfo> getBookedRoomsWithUsername() throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = """
				SELECT r.room_number,
				       r.type,
				       COALESCE(b.check_in_date, b.date) AS check_in_date,
				       COALESCE(b.check_out_date, date(COALESCE(b.check_in_date, b.date), '+1 day')) AS check_out_date,
				       r.price AS price_per_night,
				       CASE
				         WHEN b.check_in_date IS NOT NULL
				              AND b.check_out_date IS NOT NULL
				              AND julianday(b.check_out_date) > julianday(b.check_in_date)
				         THEN (julianday(b.check_out_date) - julianday(b.check_in_date)) * r.price
				         ELSE r.price
				       END AS total_price,
				       u.username
				FROM Booking b
				JOIN Room r ON r.id = b.room_id
				JOIN User u ON u.id = b.user_id
				ORDER BY CAST(r.room_number AS INTEGER)
				""";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		List<BookedRoomInfo> bookedRooms = new ArrayList<>();
		while (rs.next()) {
			bookedRooms.add(new BookedRoomInfo(
					rs.getString("room_number"),
					rs.getString("type"),
					rs.getString("check_in_date"),
					rs.getString("check_out_date"),
					rs.getDouble("price_per_night"),
					rs.getDouble("total_price"),
					rs.getString("username")));
		}

		conn.close();
		return bookedRooms;
	}

	public List<UpgradeOption> getHigherTierOptions(double currentPrice) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT type, MIN(price) AS tier_price FROM Room GROUP BY type HAVING MIN(price) > ? ORDER BY tier_price";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setDouble(1, currentPrice);
		ResultSet rs = stmt.executeQuery();

		List<UpgradeOption> options = new ArrayList<>();
		while (rs.next()) {
			options.add(new UpgradeOption(
					rs.getString("type"),
					rs.getDouble("tier_price")));
		}

		conn.close();
		return options;
	}
}