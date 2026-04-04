package com.hotel.controllers;

import com.hotel.ui.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
}