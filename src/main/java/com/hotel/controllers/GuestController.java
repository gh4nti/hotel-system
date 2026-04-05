package com.hotel.controllers;

import com.hotel.ui.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class GuestController {

	@FXML
	private Label welcomeLabel;

	@FXML
	public void initialize() {
		if (LoginController.currentUser != null) {
			welcomeLabel
					.setText("Welcome, " + LoginController.currentUser.getUsername() + ". You can book rooms here.");
		} else {
			welcomeLabel.setText("Book your stay quickly and easily.");
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
}
