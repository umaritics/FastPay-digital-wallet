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
    @FXML private TextField usernameInput;
    @FXML private TextField nameInput;
    @FXML private TextField cnicInput;
    @FXML private TextField phoneInput;
    @FXML private PasswordField passwordInput;
    @FXML private Label errorLabel;

    @FXML private Label userAsterisk;
    @FXML private Label nameAsterisk;
    @FXML private Label cnicAsterisk;
    @FXML private Label phoneAsterisk;
    @FXML private Label passAsterisk;

    @FXML
    protected void handleRegisterSubmit() {
        String username = usernameInput.getText().trim();
        String name = nameInput.getText().trim();
        String cnic = cnicInput.getText().trim();
        String phone = phoneInput.getText().trim();
        String pass = passwordInput.getText().trim();

        userAsterisk.setVisible(username.isEmpty());
        nameAsterisk.setVisible(name.isEmpty());
        cnicAsterisk.setVisible(cnic.isEmpty());
        phoneAsterisk.setVisible(phone.isEmpty());
        passAsterisk.setVisible(pass.isEmpty());

        if (username.isEmpty() || name.isEmpty() || cnic.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please fill in all required fields.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        errorLabel.setText("Creating account...");
        errorLabel.setStyle("-fx-text-fill: #002244;");

        // Send to Live Database
        String response = SupabaseAuth.registerUser(username, name, cnic, phone, pass);

        if (response.equals("Success")) {
            errorLabel.setText("Account Created! You can now log in.");
            errorLabel.setStyle("-fx-text-fill: green;");
            // Clear fields after success
            usernameInput.clear(); nameInput.clear(); cnicInput.clear(); phoneInput.clear(); passwordInput.clear();
        } else {
            errorLabel.setText(response);
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void goToLogin() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Stage stage = (Stage) errorLabel.getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(scene);
    }
}