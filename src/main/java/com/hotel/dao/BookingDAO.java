package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.Booking;
import com.hotel.models.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

	public void bookRoom(Booking booking) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		// 1. Insert booking
		String sql = "INSERT INTO Booking(user_id, room_id, date) VALUES(?,?,?)";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setInt(1, booking.getUserId());
		stmt.setInt(2, booking.getRoomId());
		stmt.setString(3, booking.getDate());

		stmt.executeUpdate();

		// 2. Mark room unavailable
		String update = "UPDATE Room SET available=0 WHERE id=?";
		PreparedStatement stmt2 = conn.prepareStatement(update);
		stmt2.setInt(1, booking.getRoomId());
		stmt2.executeUpdate();

		conn.close();
	}

	public Room bookRandomRoomByType(int userId, String roomType, String date) throws SQLException {
		Connection conn = DatabaseConnection.connect();
		conn.setAutoCommit(false);

		try {
			String selectSql = "SELECT * FROM Room WHERE type = ? AND available = 1 ORDER BY RANDOM() LIMIT 1";
			PreparedStatement selectStmt = conn.prepareStatement(selectSql);
			selectStmt.setString(1, roomType);
			ResultSet rs = selectStmt.executeQuery();

			if (!rs.next()) {
				conn.rollback();
				return null;
			}

			Room room = new Room(
					rs.getInt("id"),
					rs.getString("room_number"),
					rs.getInt("floor"),
					rs.getString("type"),
					rs.getDouble("price"),
					true);

			String insertSql = "INSERT INTO Booking(user_id, room_id, date) VALUES(?,?,?)";
			PreparedStatement insertStmt = conn.prepareStatement(insertSql);
			insertStmt.setInt(1, userId);
			insertStmt.setInt(2, room.getId());
			insertStmt.setString(3, date);
			insertStmt.executeUpdate();

			String updateSql = "UPDATE Room SET available = 0 WHERE id = ?";
			PreparedStatement updateStmt = conn.prepareStatement(updateSql);
			updateStmt.setInt(1, room.getId());
			updateStmt.executeUpdate();

			conn.commit();
			return room;
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.close();
		}
	}

	public List<Booking> getBookings() throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT * FROM Booking";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		List<Booking> list = new ArrayList<>();

		while (rs.next()) {
			list.add(new Booking(
					rs.getInt("id"),
					rs.getInt("user_id"),
					rs.getInt("room_id"),
					rs.getString("date")));
		}

		conn.close();
		return list;
	}
}