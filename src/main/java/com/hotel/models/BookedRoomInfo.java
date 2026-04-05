package com.hotel.models;

public class BookedRoomInfo {
	private final String roomNumber;
	private final String type;
	private final double price;
	private final String username;

	public BookedRoomInfo(String roomNumber, String type, double price, String username) {
		this.roomNumber = roomNumber;
		this.type = type;
		this.price = price;
		this.username = username;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public String getType() {
		return type;
	}

	public double getPrice() {
		return price;
	}

	public String getUsername() {
		return username;
	}
}