package com.hotel;

import com.hotel.ui.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        InitDB.initializeDatabase();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
        Scene scene = ThemeManager.createThemedScene(loader.load(), null);

        stage.setTitle("Hotel System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}