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
    @FXML private TextField usernameInput;
    @FXML private PasswordField passwordInput;
    @FXML private Label errorLabel;
    @FXML private Label userAsterisk;
    @FXML private Label passAsterisk;

    @FXML
    protected void handleLogin() {
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText().trim();

        userAsterisk.setVisible(username.isEmpty());
        passAsterisk.setVisible(password.isEmpty());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all required fields.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Call live database
        errorLabel.setText("Authenticating...");
        errorLabel.setStyle("-fx-text-fill: #002244;");

        String result = SupabaseAuth.loginUser(username, password);

        if (result.startsWith("Success:")) {
            String role = result.split(":")[1];
            errorLabel.setStyle("-fx-text-fill: green;");

            try {
                if (role.equals("admin")) {
                    errorLabel.setText("Welcome Admin! Loading graphs...");
                    // TODO: Load Admin Dashboard
                } else if (role.equals("accountant")) {
                    errorLabel.setText("Welcome Accountant! Loading ledgers...");
                    // TODO: Load Accountant Dashboard
                } else {
                    // SWITCH TO MAIN DASHBOARD FOR NORMAL USERS
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dashboard-view.fxml"));
                    Stage stage = (Stage) errorLabel.getScene().getWindow();
                    Scene scene = new Scene(fxmlLoader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
                    stage.setScene(scene);
                }
            } catch (Exception e) {
                errorLabel.setText("Error loading dashboard UI.");
            }
        }
    }

    @FXML
    protected void handleRegister() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register-view.fxml"));
        Stage stage = (Stage) errorLabel.getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load(), stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(scene);
    }
}