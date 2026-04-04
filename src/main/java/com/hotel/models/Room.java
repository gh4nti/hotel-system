package com.hotel.models;

public class Room {
	private int id;
	private String type;
	private double price;
	private boolean available;

	public Room(int id, String type, double price, boolean available) {
		this.id = id;
		this.type = type;
		this.price = price;
		this.available = available;
	}

	public int getId() {
		return id;
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