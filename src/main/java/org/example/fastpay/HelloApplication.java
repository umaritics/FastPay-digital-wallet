package org.example.fastpay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        // Start at 800x500
        Scene scene = new Scene(fxmlLoader.load(), 800, 650);

        // Prevent the window from being squished too small
        stage.setMinWidth(700);
        stage.setMinHeight(650);

        stage.setTitle("FastPay - Digital Wallet"); // Updated Name
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}