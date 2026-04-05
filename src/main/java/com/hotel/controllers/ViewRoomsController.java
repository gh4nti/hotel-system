package com.hotel.controllers;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.models.BookedRoomInfo;
import com.hotel.models.Room;
import com.hotel.ui.ThemeManager;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
	private TableView<BookedRoomInfo> table;
	@FXML
	private TableColumn<BookedRoomInfo, String> roomNumberCol;
	@FXML
	private TableColumn<BookedRoomInfo, String> typeCol;
	@FXML
	private TableColumn<BookedRoomInfo, Double> priceCol;
	@FXML
	private TableColumn<BookedRoomInfo, String> usernameCol;
	@FXML
	private ComboBox<String> typeCombo;
	@FXML
	private HBox bookingControls;
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
			typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
			priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
			usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
			roomTypes = dao.getRoomTypes();
			typeCombo.getItems().setAll(roomTypes);
			typeCombo.setPromptText("Select room type");

			if (isAdmin) {
				titleLabel.setText("Booked Rooms");
				subtitleLabel.setText("Currently booked rooms with guest usernames.");
				bookingControls.setVisible(false);
				bookingControls.setManaged(false);
				table.setItems(FXCollections.observableArrayList(dao.getBookedRoomsWithUsername()));
			} else {
				titleLabel.setText("Book a Room");
				subtitleLabel.setText("Select a room type and the system will assign an available room automatically.");
				usernameCol.setVisible(false);
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

		if (LoginController.currentUser.getRole().equalsIgnoreCase("admin")) {
			showAlert("Booking is available only from the guest dashboard.");
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
			refreshSummary();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void refreshSummary() throws Exception {
		boolean isAdmin = LoginController.currentUser != null
				&& LoginController.currentUser.getRole().equalsIgnoreCase("admin");

		if (isAdmin) {
			summaryLabel.setText("Total booked rooms: " + dao.getBookedRoomsWithUsername().size());
			return;
		}

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