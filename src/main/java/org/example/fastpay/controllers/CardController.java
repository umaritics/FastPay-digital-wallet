package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.fastpay.models.User;
import org.example.fastpay.services.DatabaseService;
import org.example.fastpay.utils.SessionManager;
import org.json.JSONObject;

public class CardController {

    @FXML private HBox orderCardView;
    @FXML private ComboBox<String> cardTierCombo;
    @FXML private TextArea addressInput;
    @FXML private Label orderErrorLabel;
    @FXML private Label previewNameLabel;

    @FXML private VBox activeCardView;
    @FXML private javafx.scene.image.ImageView cardBrandIcon;    @FXML private Label cardNumberLabel;
    @FXML private Label cardNameLabel;
    @FXML private Label cardExpiryLabel;
    @FXML private Label cardCvvLabel;
    @FXML private Label cardStatusLabel;

    @FXML private ToggleButton freezeToggleBtn;
    @FXML private ToggleButton revealToggleBtn;

    private User currentUser;
    private String token;
    private JSONObject currentCard;

    // Stored unmasked values
    private String unmaskedCardNumber;
    private String unmaskedCvv;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        token = SessionManager.getInstance().getAccessToken();

        // Setup Dropdown and Previews
        if (cardTierCombo != null) {
            cardTierCombo.getItems().addAll("Visa (Free)", "Mastercard (Rs. 600)", "PayPal (Rs. 1200)");
            cardTierCombo.getSelectionModel().selectFirst();
        }

        if (currentUser != null) {
            String fullName = currentUser.getEmail().split("@")[0].toUpperCase();
            if (previewNameLabel != null) previewNameLabel.setText(fullName);
            if (cardNameLabel != null) cardNameLabel.setText(fullName);
        }

        Platform.runLater(this::checkCardStatus);
    }

    private void checkCardStatus() {
        currentCard = DatabaseService.getIssuedCard(currentUser.getId(), token);

        if (currentCard == null) {
            orderCardView.setVisible(true);
            activeCardView.setVisible(false);
        } else {
            orderCardView.setVisible(false);
            activeCardView.setVisible(true);

            // Store raw values
            String rawCard = currentCard.getString("card_number");
            unmaskedCardNumber = rawCard.replaceAll(".{4}", "$0 ").trim();
            unmaskedCvv = currentCard.getString("cvv");

            // Load the dynamic Brand Icon (Visa, Mastercard, PayPak)
            String tier = currentCard.optString("card_tier", "Visa");
            try {
                String iconPath = "/org/example/fastpay/assets/icon-" + tier.toLowerCase() + ".png";
                java.io.InputStream stream = getClass().getResourceAsStream(iconPath);
                if (stream != null) {
                    cardBrandIcon.setImage(new javafx.scene.image.Image(stream));
                }
            } catch (Exception e) {
                System.out.println("Could not load brand icon for: " + tier);
            }

            cardExpiryLabel.setText(currentCard.getString("expiry_date"));

            // Initial Masked State
            maskCardDetails();
            revealToggleBtn.setSelected(false);

            String status = currentCard.getString("status");
            cardStatusLabel.setText(status);

            cardStatusLabel.getStyleClass().removeAll("text-status-active", "text-status-frozen");

            if (status.equals("FROZEN")) {
                cardStatusLabel.setTextFill(javafx.scene.paint.Color.web("#ff6b6b"));
                freezeToggleBtn.setSelected(true);
            } else {
                cardStatusLabel.setTextFill(javafx.scene.paint.Color.web("#4caf50"));
            }
        }
    }

    @FXML
    protected void handleOrderCard() {
        String address = addressInput.getText().trim();
        String dropdownSelection = cardTierCombo.getValue();

        // Extract just the tier name (e.g., "Mastercard (Rs. 600)" -> "Mastercard")
        String selectedTier = dropdownSelection.split(" ")[0];

        if (address.isEmpty()) {
            orderErrorLabel.setText("Please enter a valid delivery address.");
            return;
        }

        orderErrorLabel.setText("Processing order...");
        orderErrorLabel.setStyle("-fx-text-fill: #4da6ff;");

        // Pass the selectedTier to DatabaseService
        boolean success = DatabaseService.orderCard(currentUser.getId(), address, selectedTier, token);
        if (success) {
            checkCardStatus();
        } else {
            orderErrorLabel.setText("Failed to place order.");
            orderErrorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        }
    }

    @FXML
    protected void handleFreezeToggle() {
        cardStatusLabel.getStyleClass().removeAll("text-status-active", "text-status-frozen");

        if (freezeToggleBtn.isSelected()) {
            cardStatusLabel.setText("FROZEN");
            cardStatusLabel.getStyleClass().add("text-status-frozen");
        } else {
            cardStatusLabel.setText("ACTIVE");
            cardStatusLabel.getStyleClass().add("text-status-active");
        }
    }

    @FXML
    protected void handleRevealToggle() {
        if (revealToggleBtn.isSelected()) {
            cardNumberLabel.setText(unmaskedCardNumber);
            cardCvvLabel.setText(unmaskedCvv);
        } else {
            maskCardDetails();
        }
    }

    private void maskCardDetails() {
        if (unmaskedCardNumber != null && unmaskedCardNumber.length() >= 4) {
            String lastFour = unmaskedCardNumber.substring(unmaskedCardNumber.length() - 4);
            cardNumberLabel.setText("**** **** **** " + lastFour);
        }
        cardCvvLabel.setText("***");
    }

    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            javafx.stage.Stage stage = (javafx.stage.Stage) orderCardView.getScene().getWindow();

            // PREVENT WINDOW SHRINKING
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            stage.setScene(scene);

            if (isMax) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}