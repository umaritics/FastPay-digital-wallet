package org.example.fastpay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField nameInput;
    @FXML private TextField cnicInput;
    @FXML private TextField phoneInput;
    @FXML private PasswordField passwordInput;
    @FXML private Label errorLabel;

    @FXML private Label nameAsterisk;
    @FXML private Label cnicAsterisk;
    @FXML private Label phoneAsterisk;
    @FXML private Label passAsterisk;

    @FXML
    protected void handleRegisterSubmit() {
        boolean isNameEmpty = nameInput.getText().trim().isEmpty();
        boolean isCnicEmpty = cnicInput.getText().trim().isEmpty();
        boolean isPhoneEmpty = phoneInput.getText().trim().isEmpty();
        boolean isPassEmpty = passwordInput.getText().trim().isEmpty();

        nameAsterisk.setVisible(isNameEmpty);
        cnicAsterisk.setVisible(isCnicEmpty);
        phoneAsterisk.setVisible(isPhoneEmpty);
        passAsterisk.setVisible(isPassEmpty);

        if (isNameEmpty || isCnicEmpty || isPhoneEmpty || isPassEmpty) {
            errorLabel.setText("Please fill in all required fields.");
            errorLabel.setStyle("-fx-text-fill: red;");
        } else {
            errorLabel.setText("Registration valid! Ready for Supabase.");
            errorLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    protected void goToLogin() throws IOException {
        // Load the Login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Stage stage = (Stage) errorLabel.getScene().getWindow();

        // Keep the current window size during the switch
        Scene scene = new Scene(fxmlLoader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(scene);
    }
}