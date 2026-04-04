package com.hotel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
        Scene scene = new Scene(loader.load(), 400, 300);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Hotel System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}