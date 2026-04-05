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
	private enum AuthStep {
		IDENTIFY,
		LOGIN,
		REGISTER
	}

	public static User currentUser;
	private AuthStep currentStep = AuthStep.IDENTIFY;
	private String pendingUsername;

	@FXML
	private Label titleLabel;

	@FXML
	private Label subtitleLabel;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private PasswordField confirmPasswordField;

	@FXML
	private Button primaryButton;

	@FXML
	private Button secondaryButton;

	@FXML
	private Label messageLabel;

	private UserDAO userDAO = new UserDAO();

	@FXML
	public void initialize() {
		showIdentifyStep();
	}

	@FXML
	public void handlePrimaryAction() {
		switch (currentStep) {
			case IDENTIFY:
				handleContinueWithUsername();
				break;
			case LOGIN:
				handleLoginWithPassword();
				break;
			case REGISTER:
				handleRegisterWithPassword();
				break;
		}
	}

	@FXML
	public void handleSecondaryAction() {
		showIdentifyStep();
	}

	private void handleContinueWithUsername() {
		String username = usernameField.getText().trim();
		if (username.isEmpty()) {
			messageLabel.setText("Enter username");
			return;
		}

		try {
			User existingUser = userDAO.findByUsername(username);
			if (existingUser != null) {
				showLoginStep(username);
			} else {
				showRegisterStep(username);
			}
		} catch (Exception e) {
			e.printStackTrace();
			messageLabel.setText("Something went wrong. Try again.");
		}
	}

	private void handleLoginWithPassword() {
		String password = passwordField.getText();
		if (password.isEmpty()) {
			messageLabel.setText("Enter password");
			return;
		}

		try {
			User user = userDAO.login(pendingUsername, password);
			if (user == null) {
				messageLabel.setText("Invalid credentials");
				return;
			}

			currentUser = user;
			messageLabel.setText("Login successful!");

			String view;
			if (user.getRole().equalsIgnoreCase("admin")) {
				view = "/views/AdminDashboard.fxml";
			} else {
				view = "/views/GuestDashboard.fxml";
			}

			FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(view));
			Stage stage = (Stage) usernameField.getScene().getWindow();
			Scene scene = ThemeManager.createThemedScene(loader.load(), stage.getScene());
			stage.setScene(scene);
		} catch (Exception e) {
			e.printStackTrace();
			messageLabel.setText("Something went wrong. Try again.");
		}
	}

	private void handleRegisterWithPassword() {
		String password = passwordField.getText();
		String confirmPassword = confirmPasswordField.getText();

		if (password.isEmpty() || confirmPassword.isEmpty()) {
			messageLabel.setText("Enter and confirm password");
			return;
		}

		if (!password.equals(confirmPassword)) {
			messageLabel.setText("Passwords do not match");
			return;
		}

		try {
			User existingUser = userDAO.findByUsername(pendingUsername);
			if (existingUser != null) {
				messageLabel.setText("User already registered. Please login.");
				showLoginStep(pendingUsername);
				return;
			}

			userDAO.register(new User(0, pendingUsername, password, "guest"));
			showLoginStep(pendingUsername);
			messageLabel.setText("Registration successful. Please login.");
		} catch (Exception e) {
			e.printStackTrace();
			messageLabel.setText("Registration failed. Try again.");
		}
	}

	private void showIdentifyStep() {
		currentStep = AuthStep.IDENTIFY;
		pendingUsername = null;

		titleLabel.setText("Find Your Account");
		subtitleLabel.setText("Enter your username to continue.");

		usernameField.setVisible(true);
		usernameField.setManaged(true);
		usernameField.setEditable(true);

		passwordField.clear();
		passwordField.setVisible(false);
		passwordField.setManaged(false);

		confirmPasswordField.clear();
		confirmPasswordField.setVisible(false);
		confirmPasswordField.setManaged(false);

		primaryButton.setText("Continue");
		secondaryButton.setVisible(false);
		secondaryButton.setManaged(false);

		messageLabel.setText("");
		usernameField.requestFocus();
	}

	private void showLoginStep(String username) {
		currentStep = AuthStep.LOGIN;
		pendingUsername = username;

		titleLabel.setText("Welcome Back");
		subtitleLabel.setText("Username '" + username + "' found. Enter your password.");

		usernameField.setVisible(false);
		usernameField.setManaged(false);

		passwordField.clear();
		passwordField.setPromptText("Password");
		passwordField.setVisible(true);
		passwordField.setManaged(true);

		confirmPasswordField.clear();
		confirmPasswordField.setVisible(false);
		confirmPasswordField.setManaged(false);

		primaryButton.setText("Login");
		secondaryButton.setText("Use Different Username");
		secondaryButton.setVisible(true);
		secondaryButton.setManaged(true);

		messageLabel.setText("Account found. Please login.");
		passwordField.requestFocus();
	}

	private void showRegisterStep(String username) {
		currentStep = AuthStep.REGISTER;
		pendingUsername = username;

		titleLabel.setText("Create Account");
		subtitleLabel.setText("No account for '" + username + "'. Set a password to register.");

		usernameField.setVisible(false);
		usernameField.setManaged(false);

		passwordField.clear();
		passwordField.setPromptText("Create Password");
		passwordField.setVisible(true);
		passwordField.setManaged(true);

		confirmPasswordField.clear();
		confirmPasswordField.setPromptText("Confirm Password");
		confirmPasswordField.setVisible(true);
		confirmPasswordField.setManaged(true);

		primaryButton.setText("Register");
		secondaryButton.setText("Use Different Username");
		secondaryButton.setVisible(true);
		secondaryButton.setManaged(true);

		messageLabel.setText("Username available. Please register.");
		passwordField.requestFocus();
	}
}