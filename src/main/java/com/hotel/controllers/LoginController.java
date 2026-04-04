package com.hotel.controllers;

import com.hotel.dao.UserDAO;
import com.hotel.models.User;
import com.hotel.ui.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

	public static User currentUser;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label messageLabel;

	private UserDAO userDAO = new UserDAO();

	@FXML
	public void handleLogin() {
		String username = usernameField.getText();
		String password = passwordField.getText();

		try {
			User user = userDAO.login(username, password);

			if (user != null) {
				currentUser = user;

				messageLabel.setText("Login successful!");

				String view;

				if (user.getRole().equalsIgnoreCase("admin")) {
					view = "/views/AdminDashboard.fxml";
				} else {
					view = "/views/GuestDashboard.fxml";
				}

				System.out.println("Loading: " + view);

				FXMLLoader loader = new FXMLLoader(
						LoginController.class.getResource(view));

				Scene scene = new Scene(loader.load());
				ThemeManager.applyTheme(scene);

				Stage stage = (Stage) usernameField.getScene().getWindow();
				stage.setScene(scene);

			} else {
				messageLabel.setText("Invalid credentials");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}