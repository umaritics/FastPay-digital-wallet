package org.example.fastpay.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.fastpay.services.DatabaseService;

public class AuthController {

    @FXML private TabPane authTabPane;
    @FXML private Tab loginTab;
    @FXML private Tab registerTab;

    // Login Fields
    @FXML private TextField loginEmailInput;
    @FXML private PasswordField loginPasswordInput;
    @FXML private Label loginErrorLabel;

    // Register Fields
    @FXML private TextField regNameInput;
    @FXML private TextField regEmailInput;
    @FXML private TextField regCnicInput;
    @FXML private TextField regPhoneInput;
    @FXML private PasswordField regPasswordInput;
    @FXML private Label regErrorLabel;

    @FXML
    protected void handleLogin() {
        String email = loginEmailInput.getText().trim();
        String password = loginPasswordInput.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Please fill in all fields.");
            return;
        }

        loginErrorLabel.setText("Authenticating...");
        loginErrorLabel.setStyle("-fx-text-fill: #4da6ff;");

        String result = DatabaseService.loginUser(email, password);

        if (result.startsWith("Success:")) {
            loginErrorLabel.setStyle("-fx-text-fill: #4caf50;"); // Green
            loginErrorLabel.setText("Login Successful! Loading dashboard...");
            // TODO: Route to Dashboard View here
        } else {
            loginErrorLabel.setStyle("-fx-text-fill: #ff6b6b;"); // Red
            loginErrorLabel.setText(result);
        }
    }

    @FXML
    protected void handleRegister() {
        String name = regNameInput.getText().trim();
        String email = regEmailInput.getText().trim();
        String cnic = regCnicInput.getText().trim();
        String phone = regPhoneInput.getText().trim();
        String password = regPasswordInput.getText().trim();

        if (name.isEmpty() || email.isEmpty() || cnic.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            regErrorLabel.setText("Please fill in all required fields.");
            return;
        }

        regErrorLabel.setText("Creating account...");
        regErrorLabel.setStyle("-fx-text-fill: #4da6ff;");

        String response = DatabaseService.registerUser(email, password, name, cnic, phone);

        if (response.equals("Success")) {
            regErrorLabel.setStyle("-fx-text-fill: #4caf50;");
            regErrorLabel.setText("Account created! You can now log in.");
            clearRegisterFields();
            switchToLoginTab();
        } else {
            regErrorLabel.setStyle("-fx-text-fill: #ff6b6b;");
            regErrorLabel.setText(response);
        }
    }

    @FXML
    protected void switchToRegisterTab() {
        authTabPane.getSelectionModel().select(registerTab);
        loginErrorLabel.setText("");
    }

    @FXML
    protected void switchToLoginTab() {
        authTabPane.getSelectionModel().select(loginTab);
        regErrorLabel.setText("");
    }

    private void clearRegisterFields() {
        regNameInput.clear();
        regEmailInput.clear();
        regCnicInput.clear();
        regPhoneInput.clear();
        regPasswordInput.clear();
    }
}