package org.example.fastpay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField phoneInput;
    @FXML private PasswordField passwordInput;
    @FXML private Label errorLabel;

    // Injecting the new asterisks from the FXML
    @FXML private Label phoneAsterisk;
    @FXML private Label passAsterisk;

    @FXML
    protected void handleLogin() {
        // Check if fields are empty (ignoring accidental spaces)
        boolean isPhoneEmpty = phoneInput.getText().trim().isEmpty();
        boolean isPassEmpty = passwordInput.getText().trim().isEmpty();

        // Show the red asterisk only if the specific field is empty
        phoneAsterisk.setVisible(isPhoneEmpty);
        passAsterisk.setVisible(isPassEmpty);

        if (isPhoneEmpty || isPassEmpty) {
            // Show red error message
            errorLabel.setText("Please fill in all required fields.");
            errorLabel.setStyle("-fx-text-fill: red;");
        } else {
            // Success scenario (Asterisks stay hidden)
            errorLabel.setText("Login clicked! (Backend integration pending)");
            errorLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    protected void handleRegister() throws IOException {
        // Load the Registration screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register-view.fxml"));
        Stage stage = (Stage) errorLabel.getScene().getWindow();

        // Keep the current window size during the switch
        Scene scene = new Scene(fxmlLoader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(scene);
    }
}