package com.hotel.models;

public class Room {
	private int id;
	private String roomNumber;
	private int floor;
	private String type;
	private double price;
	private boolean available;

	public Room(int id, String roomNumber, int floor, String type, double price, boolean available) {
		this.id = id;
		this.roomNumber = roomNumber;
		this.floor = floor;
		this.type = type;
		this.price = price;
		this.available = available;
	}

	public Room(int id, String type, double price, boolean available) {
		this(id, "", 0, type, price, available);
	}

	public int getId() {
		return id;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public int getFloor() {
		return floor;
	}

	public String getType() {
		return type;
	}

	public double getPrice() {
		return price;
	}

	public boolean isAvailable() {
		return available;
	}
}