package com.hotel.models;

public class UpgradeOption {
	private final String type;
	private final double price;

	public UpgradeOption(String type, double price) {
		this.type = type;
		this.price = price;
	}

	public String getType() {
		return type;
	}

	public double getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return type + " (Rs " + String.format("%.0f", price) + ")";
	}
}