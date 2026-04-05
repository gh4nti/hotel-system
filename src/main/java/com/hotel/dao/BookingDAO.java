package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.Booking;
import com.hotel.models.Room;
import com.hotel.models.UserBookingInfo;

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

	public Room bookRandomRoomByType(int userId, String roomType, String checkInDate, String checkOutDate)
			throws SQLException {
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

			String insertSql = "INSERT INTO Booking(user_id, room_id, date, check_in_date, check_out_date) VALUES(?,?,?,?,?)";
			PreparedStatement insertStmt = conn.prepareStatement(insertSql);
			insertStmt.setInt(1, userId);
			insertStmt.setInt(2, room.getId());
			insertStmt.setString(3, checkInDate);
			insertStmt.setString(4, checkInDate);
			insertStmt.setString(5, checkOutDate);
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

	public List<UserBookingInfo> getBookingsForUser(int userId) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = """
				SELECT b.id AS booking_id,
				       r.id AS room_id,
				       r.room_number,
				       r.type,
				       r.price
				FROM Booking b
				JOIN Room r ON r.id = b.room_id
				WHERE b.user_id = ?
				ORDER BY b.id
				""";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, userId);
		ResultSet rs = stmt.executeQuery();

		List<UserBookingInfo> bookings = new ArrayList<>();
		while (rs.next()) {
			bookings.add(new UserBookingInfo(
					rs.getInt("booking_id"),
					rs.getInt("room_id"),
					rs.getString("room_number"),
					rs.getString("type"),
					rs.getDouble("price")));
		}

		conn.close();
		return bookings;
	}

	public void upgradeBookingRoom(int userId, int bookingId, int oldRoomId, int newRoomId) throws SQLException {
		Connection conn = DatabaseConnection.connect();
		conn.setAutoCommit(false);

		try {
			String validateSql = "SELECT room_id FROM Booking WHERE id = ? AND user_id = ?";
			PreparedStatement validateStmt = conn.prepareStatement(validateSql);
			validateStmt.setInt(1, bookingId);
			validateStmt.setInt(2, userId);
			ResultSet rs = validateStmt.executeQuery();

			if (!rs.next() || rs.getInt("room_id") != oldRoomId) {
				conn.rollback();
				throw new SQLException("Invalid booking selected for upgrade.");
			}

			String occupyNewSql = "UPDATE Room SET available = 0 WHERE id = ? AND available = 1";
			PreparedStatement occupyStmt = conn.prepareStatement(occupyNewSql);
			occupyStmt.setInt(1, newRoomId);
			int occupiedRows = occupyStmt.executeUpdate();

			if (occupiedRows == 0) {
				conn.rollback();
				throw new SQLException("Selected upgrade room is no longer available.");
			}

			String updateBookingSql = "UPDATE Booking SET room_id = ? WHERE id = ?";
			PreparedStatement bookingStmt = conn.prepareStatement(updateBookingSql);
			bookingStmt.setInt(1, newRoomId);
			bookingStmt.setInt(2, bookingId);
			bookingStmt.executeUpdate();

			String freeOldSql = "UPDATE Room SET available = 1 WHERE id = ?";
			PreparedStatement freeStmt = conn.prepareStatement(freeOldSql);
			freeStmt.setInt(1, oldRoomId);
			freeStmt.executeUpdate();

			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.close();
		}
	}

	public void checkoutBookingRoom(int userId, int bookingId, int roomId) throws SQLException {
		Connection conn = DatabaseConnection.connect();
		conn.setAutoCommit(false);

		try {
			String validateSql = "SELECT room_id FROM Booking WHERE id = ? AND user_id = ?";
			PreparedStatement validateStmt = conn.prepareStatement(validateSql);
			validateStmt.setInt(1, bookingId);
			validateStmt.setInt(2, userId);
			ResultSet rs = validateStmt.executeQuery();

			if (!rs.next() || rs.getInt("room_id") != roomId) {
				conn.rollback();
				throw new SQLException("Invalid booking selected for checkout.");
			}

			String deleteSql = "DELETE FROM Booking WHERE id = ? AND user_id = ?";
			PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
			deleteStmt.setInt(1, bookingId);
			deleteStmt.setInt(2, userId);
			int deletedRows = deleteStmt.executeUpdate();

			if (deletedRows == 0) {
				conn.rollback();
				throw new SQLException("Booking could not be checked out.");
			}

			String freeRoomSql = "UPDATE Room SET available = 1 WHERE id = ?";
			PreparedStatement freeRoomStmt = conn.prepareStatement(freeRoomSql);
			freeRoomStmt.setInt(1, roomId);
			freeRoomStmt.executeUpdate();

			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.close();
		}
	}
}