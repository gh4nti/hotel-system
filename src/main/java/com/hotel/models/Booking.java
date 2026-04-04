package com.hotel.models;

public class Booking {
	private int id;
	private int userId;
	private int roomId;
	private String date;

	public Booking(int id, int userId, int roomId, String date) {
		this.id = id;
		this.userId = userId;
		this.roomId = roomId;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getRoomId() {
		return roomId;
	}

	public String getDate() {
		return date;
	}
}