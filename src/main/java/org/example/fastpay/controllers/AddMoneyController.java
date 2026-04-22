package org.example.fastpay.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.fastpay.services.DatabaseService;
import org.example.fastpay.utils.SessionManager;

public class AddMoneyController {

    @FXML private TextField amountInput;
    @FXML private TextField nameInput;
    @FXML private TextField cardNumberInput;
    @FXML private TextField expiryInput;
    @FXML private PasswordField cvvInput;
    @FXML private ImageView networkIcon;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        // Real-time listener for formatting and network detection
        cardNumberInput.textProperty().addListener((observable, oldValue, newValue) -> {
            // Remove all non-digits
            String cleanStr = newValue.replaceAll("[^\\d]", "");

            // Limit to 16 digits
            if (cleanStr.length() > 16) {
                cleanStr = cleanStr.substring(0, 16);
            }

            // Auto-format with spaces
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < cleanStr.length(); i++) {
                if (i > 0 && i % 4 == 0) formatted.append(" ");
                formatted.append(cleanStr.charAt(i));
            }

            // Prevent infinite loop from self-updating
            if (!newValue.equals(formatted.toString())) {
                cardNumberInput.setText(formatted.toString());
                cardNumberInput.positionCaret(formatted.length()); // Keep cursor at end
            }

            // Detect Network based on BIN (First digit)
            detectNetwork(cleanStr);
        });
    }

    private void detectNetwork(String cleanCardNumber) {
        if (cleanCardNumber.isEmpty()) {
            networkIcon.setImage(null);
            return;
        }

        String iconName = "icon-placeholder.png"; // Default

        if (cleanCardNumber.startsWith("4")) {
            iconName = "icon-visa.png";
        } else if (cleanCardNumber.startsWith("5")) {
            iconName = "icon-mastercard.png";
        } else {
            // Assume PayPak for domestic BINs
            iconName = "icon-paypak.png";
        }

        try {
            networkIcon.setImage(new Image(getClass().getResourceAsStream("/org/example/fastpay/assets/" + iconName)));
        } catch (Exception ignored) {}
    }

    // --- THE LUHN ALGORITHM ---
    private boolean isValidLuhn(String cardNumber) {
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        if (cleanNumber.length() < 15) return false;

        int sum = 0;
        boolean alternate = false;

        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cleanNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @FXML
    protected void handlePayment() {
        String amountStr = amountInput.getText().trim();
        String name = nameInput.getText().trim();
        String card = cardNumberInput.getText().trim();
        String expiry = expiryInput.getText().trim();
        String cvv = cvvInput.getText().trim();

        if (amountStr.isEmpty() || name.isEmpty() || card.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            return;
        }

        // Validate Mathematics
        if (!isValidLuhn(card)) {
            statusLabel.setText("Invalid Card Number. Please check your digits.");
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 100) {
                statusLabel.setText("Minimum top-up is Rs. 100.");
                return;
            }

            statusLabel.setText("Authenticating with Bank...");
            statusLabel.setStyle("-fx-text-fill: #4da6ff;");

            // Execute the Database RPC Call
            String token = SessionManager.getInstance().getAccessToken();
            String userId = SessionManager.getInstance().getCurrentUser().getId();

            boolean success = DatabaseService.processDeposit(userId, amount, token);

            if (success) {
                statusLabel.setText("Success! Rs. " + amount + " added to General Partition.");
                statusLabel.setStyle("-fx-text-fill: #4caf50;");
                amountInput.clear();
                cardNumberInput.clear();
                cvvInput.clear();
            } else {
                statusLabel.setText("Bank processing failed. Try again.");
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid amount.");
        }
    }

    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            javafx.stage.Stage stage = (javafx.stage.Stage) amountInput.getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            stage.setScene(scene);

            if (isMax) stage.setMaximized(true);
            else { stage.setWidth(width); stage.setHeight(height); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}