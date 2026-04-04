package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.User;

import java.sql.*;

public class UserDAO {

	public void register(User user) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "INSERT INTO User(username, password, role) VALUES(?,?,?)";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setString(1, user.getUsername());
		stmt.setString(2, user.getPassword());
		stmt.setString(3, user.getRole());

		stmt.executeUpdate();
		conn.close();
	}

	public User login(String username, String password) throws SQLException {
		Connection conn = DatabaseConnection.connect();

		String sql = "SELECT * FROM User WHERE username=? AND password=?";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setString(1, username);
		stmt.setString(2, password);

		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			User user = new User(
					rs.getInt("id"),
					rs.getString("username"),
					rs.getString("password"),
					rs.getString("role"));
			conn.close();
			return user;
		}

		conn.close();
		return null;
	}
}