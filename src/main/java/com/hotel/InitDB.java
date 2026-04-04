package com.hotel;

import com.hotel.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class InitDB {
	public static void main(String[] args) throws Exception {
		Connection conn = DatabaseConnection.connect();
		Statement stmt = conn.createStatement();

		stmt.execute(
				"CREATE TABLE IF NOT EXISTS User (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, role TEXT)");
		stmt.execute(
				"CREATE TABLE IF NOT EXISTS Room (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT, price REAL, available INTEGER)");
		stmt.execute(
				"CREATE TABLE IF NOT EXISTS Booking (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, room_id INTEGER, date TEXT)");

		System.out.println("Tables created!");
	}
}