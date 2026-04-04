package com.hotel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Hotel Management System");
        Scene scene = new Scene(label, 400, 300);

        stage.setTitle("Hotel System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}