package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

	public void addRoom(Room room) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "INSERT INTO Room(type, price, available) VALUES(?,?,?)";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setString(1, room.getType());
		stmt.setDouble(2, room.getPrice());
		stmt.setInt(3, room.isAvailable() ? 1 : 0);

		stmt.executeUpdate();
		conn.close();
	}

	public List<Room> getAllRooms() throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT * FROM Room";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		List<Room> rooms = new ArrayList<>();

		while (rs.next()) {
			Room room = new Room(
					rs.getInt("id"),
					rs.getString("type"),
					rs.getDouble("price"),
					rs.getInt("available") == 1);
			rooms.add(room);
		}

		conn.close();
		return rooms;
	}
}