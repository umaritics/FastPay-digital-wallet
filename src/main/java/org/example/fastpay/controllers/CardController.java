package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
        String newStatus = freezeToggleBtn.isSelected() ? "FROZEN" : "ACTIVE";

        // Create a payload with ONLY the status
        org.json.JSONObject payload = new org.json.JSONObject();
        payload.put("status", newStatus);

        // Pass it to our unified method
        boolean success = DatabaseService.updateCardData(currentUser.getId(), payload, token);

        if (success) {
            cardStatusLabel.getStyleClass().removeAll("text-status-active", "text-status-frozen");
            if (newStatus.equals("FROZEN")) {
                cardStatusLabel.setText("FROZEN");
                cardStatusLabel.getStyleClass().add("text-status-frozen");
            } else {
                cardStatusLabel.setText("ACTIVE");
                cardStatusLabel.getStyleClass().add("text-status-active");
            }
        } else {
            freezeToggleBtn.setSelected(!freezeToggleBtn.isSelected());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update card status.");
            alert.show();
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
    @FXML
    protected void openCardSettings() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Advanced Card Settings");
        dialog.setHeaderText("Manage your FastPay Card security and limits.");

        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 10, 20));

        // PIN Change
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("Enter new 4-digit PIN");
        pinField.getStyleClass().add("input-field-dark");

        // Daily Limit
        TextField limitField = new TextField();
        limitField.setText(String.valueOf(currentCard.optDouble("daily_limit", 50000.00)));
        limitField.getStyleClass().add("input-field-dark");
        limitField.setTextFormatter(new TextFormatter<>(change -> {
            // Allows empty string, or digits with at most one decimal point
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null; // Rejects the keystroke if it's a letter or a minus sign
        }));

        // Toggles
        CheckBox onlineToggle = new CheckBox("Enable Online Transactions");
        onlineToggle.setSelected(currentCard.optBoolean("online_payments", true));
        onlineToggle.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold;");

        CheckBox tapToggle = new CheckBox("Enable NFC (Tap & Pay)");
        tapToggle.setSelected(currentCard.optBoolean("tap_and_pay", true));
        tapToggle.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold;");

        grid.add(new Label("Card PIN:"), 0, 0);
        grid.add(pinField, 1, 0);
        grid.add(new Label("Daily Limit (PKR):"), 0, 1);
        grid.add(limitField, 1, 1);
        grid.add(onlineToggle, 0, 2, 2, 1);
        grid.add(tapToggle, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Inject Dialog CSS
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog");
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/org/example/fastpay/styles/application.css").toExternalForm());
        } catch (Exception ignored) {
        }

        java.util.Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                // Construct a payload with ALL the settings
                org.json.JSONObject payload = new org.json.JSONObject();

                String newPin = pinField.getText().trim();
                if (!newPin.isEmpty()) {
                    payload.put("pin", newPin);
                }

                payload.put("daily_limit", Double.parseDouble(limitField.getText().trim()));
                payload.put("online_payments", onlineToggle.isSelected());
                payload.put("tap_and_pay", tapToggle.isSelected());

                // Pass it to the EXACT SAME unified method
                boolean success = DatabaseService.updateCardData(currentUser.getId(), payload, token);

                if (success) {
                    checkCardStatus(); // Refresh UI
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to securely update card settings.");
                    alert.show();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid limit amount entered.");
                alert.show();
            }
        }
    }
}