package com.hotel.controllers;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.models.Booking;
import com.hotel.models.Room;
import com.hotel.ui.ThemeManager;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
			availCol.setCellFactory(col -> new TableCell<>() {
				@Override
				protected void updateItem(Boolean item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
						setTextFill(Color.TRANSPARENT);
						return;
					}

					setText(item ? "Available" : "Booked");
					setTextFill(item ? Color.web("#44d17a") : Color.web("#ff6565"));
				}
			});

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

			int userId = LoginController.currentUser.getId();
			dao.bookRoom(new Booking(0, userId, selected.getId(), "2026-04-05"));

			showAlert("Room booked successfully!");

			// refresh table
			table.setItems(FXCollections.observableArrayList(new RoomDAO().getAllRooms()));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void handleBack(javafx.event.ActionEvent event) {
		try {
			String view;

			if (LoginController.currentUser != null
					&& LoginController.currentUser.getRole().equalsIgnoreCase("admin")) {
				view = "/views/AdminDashboard.fxml";
			} else {
				view = "/views/GuestDashboard.fxml";
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
			Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
					.getScene()
					.getWindow();
			Scene scene = ThemeManager.createThemedScene(loader.load(), stage.getScene());
			stage.setScene(scene);
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