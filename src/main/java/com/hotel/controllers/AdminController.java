package com.hotel.controllers;

import com.hotel.dao.RoomDAO;
import com.hotel.ui.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AdminController {

	@FXML
	private Label welcomeLabel;

	@FXML
	public void initialize() {
		if (LoginController.currentUser != null) {
			welcomeLabel.setText("Welcome, " + LoginController.currentUser.getUsername());
		}
	}

	@FXML
	public void handleModifyRoom() {
		try {
			RoomDAO roomDAO = new RoomDAO();

			Dialog<Void> dialog = new Dialog<>();
			dialog.setTitle("Modify Room Type");
			dialog.setHeaderText("Update room type name and price");

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20));

			Label currentTypeLabel = new Label("Current Type:");
			currentTypeLabel.getStyleClass().add("info-label");
			Label newTypeLabel = new Label("New Type Name:");
			newTypeLabel.getStyleClass().add("info-label");
			Label priceLabel = new Label("New Price:");
			priceLabel.getStyleClass().add("info-label");

			ComboBox<String> currentTypeCombo = new ComboBox<>();
			currentTypeCombo.getItems().setAll(roomDAO.getRoomTypes());
			currentTypeCombo.setPromptText("Select current type");

			TextField newTypeField = new TextField();
			newTypeField.setPromptText("Enter new room type name");

			TextField priceField = new TextField();
			priceField.setPromptText("Enter new price");

			currentTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
				if (newValue == null || newValue.isBlank()) {
					newTypeField.clear();
					priceField.clear();
					return;
				}

				newTypeField.setText(newValue);
				try {
					double currentPrice = roomDAO.getPriceForType(newValue);
					priceField.setText(String.valueOf((int) currentPrice));
				} catch (Exception ex) {
					priceField.clear();
				}
			});

			grid.add(currentTypeLabel, 0, 0);
			grid.add(currentTypeCombo, 1, 0);
			grid.add(newTypeLabel, 0, 1);
			grid.add(newTypeField, 1, 1);
			grid.add(priceLabel, 0, 2);
			grid.add(priceField, 1, 2);

			DialogPane pane = dialog.getDialogPane();
			pane.getStyleClass().add("themed-dialog");
			pane.setContent(grid);
			pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			pane.lookupButton(ButtonType.OK).getStyleClass().add("primary-button");
			pane.lookupButton(ButtonType.CANCEL).getStyleClass().add("secondary-button");

			dialog.setOnShowing(e -> {
				Scene dialogScene = pane.getScene();
				if (dialogScene != null) {
					ThemeManager.applyTheme(dialogScene);
				}
			});

			dialog.setResultConverter(button -> {
				if (button == ButtonType.OK) {
					try {
						String currentType = currentTypeCombo.getValue();
						String newType = newTypeField.getText().trim();
						double newPrice = Double.parseDouble(priceField.getText().trim());

						if (currentType == null || currentType.isBlank()) {
							showAlert("Error", "Please select a current room type.");
							return null;
						}

						if (newType.isBlank()) {
							showAlert("Error", "New room type name cannot be empty.");
							return null;
						}

						if (newPrice <= 0) {
							showAlert("Error", "Price must be greater than 0.");
							return null;
						}

						roomDAO.updateRoomTypeAndPrice(currentType, newType, newPrice);
						showAlert("Success", "Room type updated successfully.");
					} catch (NumberFormatException ex) {
						showAlert("Error", "Please enter a valid price.");
						return null;
					} catch (Exception ex) {
						showAlert("Error", "Failed to modify room type: " + ex.getMessage());
						return null;
					}
				}
				return null;
			});

			dialog.showAndWait();
		} catch (Exception e) {
			showAlert("Error", "Failed to load room types: " + e.getMessage());
		}
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