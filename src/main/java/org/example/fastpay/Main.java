package org.example.fastpay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Pointing to the new views directory
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/auth-view.fxml"));

        // Setting the initial size to match our HBox design
        Scene scene = new Scene(fxmlLoader.load(), 900, 650);

        // Load the CSS stylesheet for the modern styling
        String cssPath = getClass().getResource("styles/application.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        // Prevent the window from being squished and ruining the UI
        stage.setMinWidth(850);
        stage.setMinHeight(600);

        stage.setTitle("FastPay - Digital Wallet");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}