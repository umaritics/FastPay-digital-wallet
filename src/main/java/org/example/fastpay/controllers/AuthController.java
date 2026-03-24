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

        // 1. Call the Database
        String result = DatabaseService.loginUser(email, password);

        // Print the raw result to your IDE console so we can see exactly what Supabase returned
        System.out.println("DEBUG - Supabase Response: " + result);

        // 2. Check for the new Pipe format OR the old Colon format just in case
        if (result.startsWith("Success|")) {

            try {
                String[] parts = result.split("\\|");
                String token = parts[1];
                String id = parts[2];
                String userEmail = parts[3];
                String role = parts[4];

                // Save to SessionManager
                org.example.fastpay.models.User loggedInUser = new org.example.fastpay.models.User(id, userEmail, role);
                org.example.fastpay.utils.SessionManager.getInstance().loginUser(loggedInUser, token);

                loginErrorLabel.setStyle("-fx-text-fill: #4caf50;");
                loginErrorLabel.setText("Login Successful! Loading dashboard...");

                // Bulletproof Absolute Pathing
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/fastpay/views/dashboard-view.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1050, 700);

                String cssPath = getClass().getResource("/org/example/fastpay/styles/application.css").toExternalForm();
                scene.getStylesheets().add(cssPath);

                javafx.stage.Stage stage = (javafx.stage.Stage) loginEmailInput.getScene().getWindow();
                stage.setScene(scene);
                stage.centerOnScreen();

            } catch (Exception e) {
                // If the screen fails to load, print the EXACT reason to the UI and the Console
                loginErrorLabel.setStyle("-fx-text-fill: #ff6b6b;");
                loginErrorLabel.setText("System Error: Could not load Dashboard file.");
                System.out.println("CRITICAL UI ERROR: " + e.getMessage());
                e.printStackTrace();
            }

        } else if (result.startsWith("Success:")) {
            // If we hit this block, it means DatabaseService wasn't updated to return the Session tokens!
            loginErrorLabel.setStyle("-fx-text-fill: #ff6b6b;");
            loginErrorLabel.setText("Configuration Error: DatabaseService is using old return format.");
        } else {
            loginErrorLabel.setStyle("-fx-text-fill: #ff6b6b;");
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