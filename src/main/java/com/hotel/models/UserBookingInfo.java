package com.hotel.models;

public class UserBookingInfo {
	private final int bookingId;
	private final int roomId;
	private final String roomNumber;
	private final String type;
	private final double price;

	public UserBookingInfo(int bookingId, int roomId, String roomNumber, String type, double price) {
		this.bookingId = bookingId;
		this.roomId = roomId;
		this.roomNumber = roomNumber;
		this.type = type;
		this.price = price;
	}

	public int getBookingId() {
		return bookingId;
	}

	public int getRoomId() {
		return roomId;
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

	@Override
	public String toString() {
		return roomNumber + " - " + type + " (Rs " + String.format("%.0f", price) + ")";
	}
}