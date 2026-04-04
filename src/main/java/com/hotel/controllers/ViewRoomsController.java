package com.hotel.controllers;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.models.Booking;
import com.hotel.models.Room;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;

public class ViewRoomsController {

	@FXML
	private TableView<Room> table;
	@FXML
	private TableColumn<Room, Integer> idCol;
	@FXML
	private TableColumn<Room, String> typeCol;
	@FXML
	private TableColumn<Room, Double> priceCol;
	@FXML
	private TableColumn<Room, Boolean> availCol;

	private RoomDAO dao = new RoomDAO();

	@FXML
	public void initialize() {
		try {
			idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
			typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
			priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
			availCol.setCellValueFactory(new PropertyValueFactory<>("available"));

			ObservableList<Room> list = FXCollections.observableArrayList(dao.getAllRooms());
			table.setItems(list);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void handleBook() {
		Room selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showAlert("Please select a room");
			return;
		}

		if (!selected.isAvailable()) {
			showAlert("Room already booked");
			return;
		}

		try {
			BookingDAO dao = new BookingDAO();

			// TEMP: userId = 1 (we'll improve later)
			dao.bookRoom(new Booking(0, 1, selected.getId(), "2026-04-05"));

			showAlert("Room booked successfully!");

			// refresh table
			table.setItems(FXCollections.observableArrayList(new RoomDAO().getAllRooms()));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setContentText(msg);
		alert.show();
	}
}