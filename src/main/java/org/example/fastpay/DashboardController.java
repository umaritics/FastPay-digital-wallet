package org.example.fastpay;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    protected void handleLogout(ActionEvent event) throws IOException {
        // Find the button that was clicked to get the current window
        Button logoutBtn = (Button) event.getSource();
        Stage stage = (Stage) logoutBtn.getScene().getWindow();

        // Load the Login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(scene);
    }
}