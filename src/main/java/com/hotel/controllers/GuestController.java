package com.hotel.controllers;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.models.Room;
import com.hotel.models.UpgradeOption;
import com.hotel.models.UserBookingInfo;
import com.hotel.ui.ThemeManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class GuestController {

	@FXML
	private Label welcomeLabel;
	@FXML
	private Label bookingStatusLabel;
	@FXML
	private Button bookRoomButton;
	@FXML
	private Button upgradeRoomButton;
	@FXML
	private Button checkoutRoomButton;

	private final BookingDAO bookingDAO = new BookingDAO();
	private final RoomDAO roomDAO = new RoomDAO();

	@FXML
	public void initialize() {
		refreshDashboard();
	}

	private void refreshDashboard() {
		if (LoginController.currentUser != null) {
			welcomeLabel.setText("Hello, " + LoginController.currentUser.getUsername());

			try {
				List<UserBookingInfo> bookings = bookingDAO.getBookingsForUser(LoginController.currentUser.getId());
				boolean hasBookings = !bookings.isEmpty();

				if (hasBookings) {
					bookingStatusLabel.setText("You have " + bookings.size() + " booked room(s).");
					bookRoomButton.setText("Book Another Room");
					upgradeRoomButton.setVisible(true);
					upgradeRoomButton.setManaged(true);
					checkoutRoomButton.setVisible(true);
					checkoutRoomButton.setManaged(true);
				} else {
					bookingStatusLabel.setText("You have not booked a room yet.");
					bookRoomButton.setText("Book Room");
					upgradeRoomButton.setVisible(false);
					upgradeRoomButton.setManaged(false);
					checkoutRoomButton.setVisible(false);
					checkoutRoomButton.setManaged(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				bookingStatusLabel.setText("Could not load your bookings right now.");
				upgradeRoomButton.setVisible(false);
				upgradeRoomButton.setManaged(false);
				checkoutRoomButton.setVisible(false);
				checkoutRoomButton.setManaged(false);
			}
		} else {
			welcomeLabel.setText("Welcome, guest");
			bookingStatusLabel.setText("Book your stay quickly and easily.");
			bookRoomButton.setText("Book Room");
			upgradeRoomButton.setVisible(false);
			upgradeRoomButton.setManaged(false);
			checkoutRoomButton.setVisible(false);
			checkoutRoomButton.setManaged(false);
		}
	}

	@FXML
	public void handleBookRoom(javafx.event.ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ViewRooms.fxml"));
			Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
					.getScene()
					.getWindow();
			Scene scene = ThemeManager.createThemedScene(loader.load(), stage.getScene());
			stage.setScene(scene);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void handleUpgradeRoom() {
		if (LoginController.currentUser == null) {
			showAlert("Please login to upgrade a room.");
			return;
		}

		try {
			List<UserBookingInfo> bookings = bookingDAO.getBookingsForUser(LoginController.currentUser.getId());
			if (bookings.isEmpty()) {
				showAlert("You do not have any bookings to upgrade.");
				refreshDashboard();
				return;
			}

			UserBookingInfo selectedBooking;
			if (bookings.size() == 1) {
				selectedBooking = bookings.get(0);
			} else {
				ChoiceDialog<UserBookingInfo> bookingDialog = new ChoiceDialog<>(bookings.get(0), bookings);
				bookingDialog.setTitle("Select Room");
				bookingDialog.setHeaderText("Choose a booked room to upgrade");
				bookingDialog.setContentText("Your rooms:");

				Optional<UserBookingInfo> selectedBookingOpt = bookingDialog.showAndWait();
				if (selectedBookingOpt.isEmpty()) {
					return;
				}
				selectedBooking = selectedBookingOpt.get();
			}

			List<UpgradeOption> options = roomDAO.getHigherTierOptions(selectedBooking.getPrice());
			if (options.isEmpty()) {
				showAlert("No higher-tier upgrade options are available for this room.");
				return;
			}

			ChoiceDialog<UpgradeOption> upgradeDialog = new ChoiceDialog<>(options.get(0),
					FXCollections.observableArrayList(options));
			upgradeDialog.setTitle("Upgrade Room");
			upgradeDialog.setHeaderText("Choose a higher tier");
			upgradeDialog.setContentText("Upgrade to:");

			Optional<UpgradeOption> selectedUpgradeOpt = upgradeDialog.showAndWait();
			if (selectedUpgradeOpt.isEmpty()) {
				return;
			}

			UpgradeOption selectedUpgrade = selectedUpgradeOpt.get();
			double remainingPrice = selectedUpgrade.getPrice() - selectedBooking.getPrice();

			Room newRoom = roomDAO.getRandomAvailableRoomByType(selectedUpgrade.getType());
			if (newRoom == null) {
				showAlert("No available rooms found in the selected upgrade tier.");
				return;
			}

			Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
			confirmAlert.setTitle("Confirm Upgrade");
			confirmAlert
					.setHeaderText("Upgrade " + selectedBooking.getRoomNumber() + " to " + selectedUpgrade.getType());
			confirmAlert.setContentText("Additional amount to pay: Rs " + String.format("%.0f", remainingPrice));

			Optional<ButtonType> confirm = confirmAlert.showAndWait();
			if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
				return;
			}

			bookingDAO.upgradeBookingRoom(
					LoginController.currentUser.getId(),
					selectedBooking.getBookingId(),
					selectedBooking.getRoomId(),
					newRoom.getId());

			showAlert("Upgrade successful. New room: " + newRoom.getRoomNumber() + ". Paid: Rs "
					+ String.format("%.0f", remainingPrice));
			refreshDashboard();
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Room upgrade failed. Please try again.");
		}
	}

	@FXML
	public void handleCheckoutRoom() {
		if (LoginController.currentUser == null) {
			showAlert("Please login to check out a room.");
			return;
		}

		try {
			List<UserBookingInfo> bookings = bookingDAO.getBookingsForUser(LoginController.currentUser.getId());
			if (bookings.isEmpty()) {
				showAlert("You do not have any booked rooms to check out.");
				refreshDashboard();
				return;
			}

			UserBookingInfo selectedBooking;
			if (bookings.size() == 1) {
				selectedBooking = bookings.get(0);
			} else {
				ChoiceDialog<UserBookingInfo> bookingDialog = new ChoiceDialog<>(bookings.get(0), bookings);
				bookingDialog.setTitle("Select Room");
				bookingDialog.setHeaderText("Choose a room to check out");
				bookingDialog.setContentText("Your rooms:");

				Optional<UserBookingInfo> selectedBookingOpt = bookingDialog.showAndWait();
				if (selectedBookingOpt.isEmpty()) {
					return;
				}
				selectedBooking = selectedBookingOpt.get();
			}

			Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
			confirmAlert.setTitle("Confirm Checkout");
			confirmAlert.setHeaderText("Check out room " + selectedBooking.getRoomNumber() + "?");
			confirmAlert.setContentText("This will free up the room and remove it from your bookings.");

			Optional<ButtonType> confirm = confirmAlert.showAndWait();
			if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
				return;
			}

			bookingDAO.checkoutBookingRoom(
					LoginController.currentUser.getId(),
					selectedBooking.getBookingId(),
					selectedBooking.getRoomId());

			showAlert("Checked out successfully from room " + selectedBooking.getRoomNumber() + ".");
			refreshDashboard();
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Checkout failed. Please try again.");
		}
	}

	@FXML
	public void handleLogout(javafx.event.ActionEvent event) {
		try {
			LoginController.currentUser = null;

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
			Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
					.getScene()
					.getWindow();
			Scene scene = ThemeManager.createThemedScene(loader.load(), stage.getScene());
			stage.setScene(scene);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
