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
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
	private TableColumn<BookedRoomInfo, String> checkInCol;
	@FXML
	private TableColumn<BookedRoomInfo, String> checkOutCol;
	@FXML
	private TableColumn<BookedRoomInfo, Double> pricePerNightCol;
	@FXML
	private TableColumn<BookedRoomInfo, Double> totalPriceCol;
	@FXML
	private TableColumn<BookedRoomInfo, String> usernameCol;
	@FXML
	private ComboBox<String> typeCombo;
	@FXML
	private VBox bookingControls;
	@FXML
	private Label summaryLabel;
	@FXML
	private DatePicker checkInDatePicker;
	@FXML
	private DatePicker checkOutDatePicker;
	@FXML
	private Label nightsLabel;
	@FXML
	private Label pricePerNightLabel;
	@FXML
	private Label totalPriceLabel;

	private RoomDAO dao = new RoomDAO();
	private List<String> roomTypes;

	@FXML
	public void initialize() {
		try {
			boolean isAdmin = LoginController.currentUser != null
					&& LoginController.currentUser.getRole().equalsIgnoreCase("admin");

			roomNumberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
			typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
			checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
			checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
			pricePerNightCol.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
			totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
			usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
			roomTypes = dao.getRoomTypes();
			typeCombo.getItems().setAll(roomTypes);
			typeCombo.setPromptText("Select room type");
			typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshBookingQuote());

			checkInDatePicker.setValue(LocalDate.now());
			checkInDatePicker.setDayCellFactory(dp -> new DateCell() {
				@Override
				public void updateItem(LocalDate item, boolean empty) {
					super.updateItem(item, empty);
					setDisable(empty || item.isBefore(LocalDate.now()));
				}
			});

			checkOutDatePicker.setDayCellFactory(dp -> new DateCell() {
				@Override
				public void updateItem(LocalDate item, boolean empty) {
					super.updateItem(item, empty);
					LocalDate checkIn = checkInDatePicker.getValue();
					if (checkIn != null) {
						setDisable(empty || !item.isAfter(checkIn));
					} else {
						setDisable(empty || item.isBefore(LocalDate.now().plusDays(1)));
					}
				}
			});

			checkInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal != null && checkOutDatePicker.getValue() != null
						&& !checkOutDatePicker.getValue().isAfter(newVal)) {
					checkOutDatePicker.setValue(null);
				}
				refreshBookingQuote();
			});
			checkOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshBookingQuote());

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
				refreshBookingQuote();
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
		LocalDate checkInDate = checkInDatePicker.getValue();
		LocalDate checkOutDate = checkOutDatePicker.getValue();

		if (selectedType == null || selectedType.isBlank()) {
			showAlert("Please select a room type");
			return;
		}

		if (checkInDate == null) {
			showAlert("Please select a check-in date");
			return;
		}

		if (checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
			showAlert("Check-out date must be after check-in date");
			return;
		}

		long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
		double pricePerNight;

		try {
			BookingDAO dao = new BookingDAO();
			pricePerNight = this.dao.getPriceForType(selectedType);

			int userId = LoginController.currentUser.getId();
			Room bookedRoom = dao.bookRandomRoomByType(
					userId,
					selectedType,
					checkInDate.toString(),
					checkOutDate.toString());

			if (bookedRoom == null) {
				showAlert("No available rooms for that type");
				return;
			}

			double totalPrice = pricePerNight * nights;

			showAlert("Room booked successfully: " + bookedRoom.getRoomNumber()
					+ "\nCheck-in: " + checkInDate
					+ "\nCheck-out: " + checkOutDate
					+ "\nNights: " + nights
					+ "\nTotal: Rs " + String.format("%.0f", totalPrice));
			refreshSummary();
			refreshBookingQuote();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void refreshBookingQuote() {
		String selectedType = typeCombo.getValue();
		LocalDate checkInDate = checkInDatePicker.getValue();
		LocalDate checkOutDate = checkOutDatePicker.getValue();

		if (selectedType == null || selectedType.isBlank()) {
			nightsLabel.setText("Nights: --");
			pricePerNightLabel.setText("Price/night: --");
			totalPriceLabel.setText("Total: --");
			return;
		}

		try {
			double pricePerNight = dao.getPriceForType(selectedType);
			pricePerNightLabel.setText("Price/night: Rs " + String.format("%.0f", pricePerNight));

			if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
				nightsLabel.setText("Nights: --");
				totalPriceLabel.setText("Total: --");
				return;
			}

			long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
			double total = nights * pricePerNight;

			nightsLabel.setText("Nights: " + nights);
			totalPriceLabel.setText("Total: Rs " + String.format("%.0f", total));
		} catch (Exception e) {
			nightsLabel.setText("Nights: --");
			pricePerNightLabel.setText("Price/night: --");
			totalPriceLabel.setText("Total: --");
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