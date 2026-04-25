package org.example.fastpay.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class MTagController {

    @FXML private TextField mtagIdField;
    @FXML private TextField amountField;

    @FXML
    public void initialize() {
        setupValidations();
    }

    private void setupValidations() {
        // EXACTLY 8 DIGITS (Real-time keyboard block)
        mtagIdField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            // Allows an empty string, or exactly up to 8 numeric digits
            if (newText.matches("^$|^\\d{1,8}$")) {
                return change;
            }
            return null; // Reject the keystroke
        }));

        // AMOUNT VALIDATION (Numbers and decimals only)
        amountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
    }

    @FXML
    protected void handleTopup() {
        String mtagId = mtagIdField.getText().trim();
        String amountStr = amountField.getText().trim();

        // 1. Validation Checks
        if (mtagId.length() != 8) {
            new Alert(Alert.AlertType.WARNING, "Invalid M-Tag ID. It must be exactly 8 digits.").show();
            return;
        }

        if (amountStr.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter a recharge amount.").show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount < 500) {
            new Alert(Alert.AlertType.WARNING, "The minimum M-Tag recharge amount is Rs. 500.").show();
            return;
        }

        // 2. Process Transaction (Placeholder for future DB logic)
        System.out.println("Processing M-Tag Top-up. ID: " + mtagId + ", Amount: Rs. " + amount);

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Success");
        success.setHeaderText("Transaction Successful");
        success.setContentText("Successfully recharged Rs. " + amount + " to M-Tag ID " + mtagId);
        success.showAndWait();

        // 3. Clear form on success
        mtagIdField.clear();
        amountField.clear();
    }

    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());

            javafx.stage.Stage stage = (javafx.stage.Stage) mtagIdField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CRITICAL: Could not return to dashboard.");
        }
    }
}