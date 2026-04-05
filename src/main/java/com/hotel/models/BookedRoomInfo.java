package com.hotel.models;

public class BookedRoomInfo {
	private final String roomNumber;
	private final String type;
	private final String checkInDate;
	private final String checkOutDate;
	private final double pricePerNight;
	private final double totalPrice;
	private final String username;

	public BookedRoomInfo(
			String roomNumber,
			String type,
			String checkInDate,
			String checkOutDate,
			double pricePerNight,
			double totalPrice,
			String username) {
		this.roomNumber = roomNumber;
		this.type = type;
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.pricePerNight = pricePerNight;
		this.totalPrice = totalPrice;
		this.username = username;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public String getType() {
		return type;
	}

	public String getCheckInDate() {
		return checkInDate;
	}

	public String getCheckOutDate() {
		return checkOutDate;
	}

	public double getPricePerNight() {
		return pricePerNight;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public String getUsername() {
		return username;
	}
}