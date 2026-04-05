package com.hotel.controllers;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.models.Room;
import com.hotel.ui.ThemeManager;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ViewRoomsController {
	@FXML
	private Label titleLabel;
	@FXML
	private Label subtitleLabel;

	@FXML
	private TableView<Room> table;
	@FXML
	private TableColumn<Room, String> roomNumberCol;
	@FXML
	private TableColumn<Room, Integer> floorCol;
	@FXML
	private TableColumn<Room, String> typeCol;
	@FXML
	private TableColumn<Room, Double> priceCol;
	@FXML
	private TableColumn<Room, Boolean> availCol;
	@FXML
	private ComboBox<String> typeCombo;
	@FXML
	private Label summaryLabel;

	private RoomDAO dao = new RoomDAO();
	private List<String> roomTypes;

	@FXML
	public void initialize() {
		try {
			boolean isAdmin = LoginController.currentUser != null
					&& LoginController.currentUser.getRole().equalsIgnoreCase("admin");

			roomNumberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
			floorCol.setCellValueFactory(new PropertyValueFactory<>("floor"));
			typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
			priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
			availCol.setCellValueFactory(new PropertyValueFactory<>("available"));
			roomTypes = dao.getRoomTypes();
			typeCombo.getItems().setAll(roomTypes);
			typeCombo.setPromptText("Select room type");
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

			if (!isAdmin) {
				titleLabel.setText("Book a Room");
				subtitleLabel.setText("Select a room type and the system will assign an available room automatically.");
				table.setVisible(false);
				table.setManaged(false);
			}

			refreshSummary();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void handleBook() {
		if (LoginController.currentUser == null) {
			showAlert("Please login to book a room");
			return;
		}

		String selectedType = typeCombo.getValue();

		if (selectedType == null || selectedType.isBlank()) {
			showAlert("Please select a room type");
			return;
		}

		try {
			BookingDAO dao = new BookingDAO();

			int userId = LoginController.currentUser.getId();
			Room bookedRoom = dao.bookRandomRoomByType(userId, selectedType, LocalDate.now().toString());

			if (bookedRoom == null) {
				showAlert("No available rooms for that type");
				return;
			}

			showAlert("Room booked successfully: " + bookedRoom.getRoomNumber());

			// refresh table
			table.setItems(FXCollections.observableArrayList(new RoomDAO().getAllRooms()));
			refreshSummary();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void refreshSummary() throws Exception {
		Map<String, Integer> counts = dao.getAvailableCountsByType();
		StringBuilder builder = new StringBuilder("Available rooms: ");
		for (int i = 0; i < roomTypes.size(); i++) {
			String type = roomTypes.get(i);
			if (i > 0) {
				builder.append(" | ");
			}
			builder.append(type).append(" ").append(counts.getOrDefault(type, 0));
		}
		summaryLabel.setText(builder.toString());
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