package com.hotel.controllers;

import com.hotel.dao.RoomDAO;
import com.hotel.models.Room;
import com.hotel.ui.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdminController {

	private static final Map<String, Double> ROOM_PRICES = new LinkedHashMap<>();

	static {
		ROOM_PRICES.put("Single", 1000.0);
		ROOM_PRICES.put("Double", 2000.0);
		ROOM_PRICES.put("Deluxe", 3000.0);
		ROOM_PRICES.put("Suite", 4500.0);
		ROOM_PRICES.put("Presidential Suite", 8000.0);
	}

	@FXML
	private Label welcomeLabel;

	@FXML
	public void initialize() {
		if (LoginController.currentUser != null) {
			welcomeLabel.setText("Welcome, " + LoginController.currentUser.getUsername());
		}
	}

	@FXML
	public void handleAddRoom(javafx.event.ActionEvent event) {
		Dialog<Room> dialog = new Dialog<>();
		dialog.setTitle("Add New Room");
		dialog.setHeaderText("Enter room details");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));

		Label typeLabel = new Label("Room Type:");
		typeLabel.getStyleClass().add("info-label");
		Label priceLabel = new Label("Price:");
		priceLabel.getStyleClass().add("info-label");

		ComboBox<String> typeField = new ComboBox<>();
		typeField.getItems().addAll(ROOM_PRICES.keySet());
		typeField.setPromptText("Select a room type");

		TextField priceField = new TextField();
		priceField.setEditable(false);
		priceField.setFocusTraversable(false);
		priceField.setPromptText("Select a room type");

		typeField.valueProperty().addListener((observable, oldValue, newValue) -> {
			Double price = ROOM_PRICES.get(newValue);
			priceField.setText(price == null ? "" : String.valueOf(price.intValue()));
		});

		grid.add(typeLabel, 0, 0);
		grid.add(typeField, 1, 0);
		grid.add(priceLabel, 0, 1);
		grid.add(priceField, 1, 1);

		DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStyleClass().add("themed-dialog");
		dialogPane.setContent(grid);
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		// Style the dialog buttons with theme classes
		dialogPane.lookupButton(ButtonType.OK).getStyleClass().add("primary-button");
		dialogPane.lookupButton(ButtonType.CANCEL).getStyleClass().add("secondary-button");

		// Apply theme to the dialog
		dialog.setOnShowing(e -> {
			Scene dialogScene = dialogPane.getScene();
			if (dialogScene != null) {
				ThemeManager.applyTheme(dialogScene);
			}
		});

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				try {
					String type = typeField.getValue();
					String priceText = priceField.getText().trim();

					if (type == null || type.isEmpty()) {
						showAlert("Error", "Please select a room type");
						return null;
					}

					if (priceText.isEmpty()) {
						showAlert("Error", "Please select a room type");
						return null;
					}

					double price = Double.parseDouble(priceText);

					Room room = new Room(0, type, price, true);
					new RoomDAO().addRoom(room);
					showAlert("Success", "Room added successfully!");
					return room;
				} catch (NumberFormatException e) {
					showAlert("Error", "Invalid price. Please enter a valid number.");
				} catch (Exception e) {
					showAlert("Error", "Failed to add room: " + e.getMessage());
				}
			}
			return null;
		});

		dialog.showAndWait();
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	@FXML
	public void handleViewRooms(javafx.event.ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/views/ViewRooms.fxml"));

			// Get stage from button click event (clean way)
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
	public void handleLogout(javafx.event.ActionEvent event) {
		try {
			LoginController.currentUser = null;

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/views/LoginView.fxml"));

			Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
					.getScene()
					.getWindow();

			Scene scene = ThemeManager.createThemedScene(loader.load(), stage.getScene());
			stage.setScene(scene);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}