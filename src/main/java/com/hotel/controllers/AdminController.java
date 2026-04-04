package com.hotel.controllers;

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

			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(
					getClass().getResource("/style.css").toExternalForm());

			// Get stage from button click event (clean way)
			Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
					.getScene()
					.getWindow();

			stage.setScene(scene);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}