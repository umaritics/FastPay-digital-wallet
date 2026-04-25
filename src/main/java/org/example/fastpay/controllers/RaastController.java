package org.example.fastpay.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.fastpay.utils.SessionManager;

public class RaastController {

    @FXML private ToggleButton sendRaastToggle;
    @FXML private ToggleButton linkRaastToggle;

    @FXML private VBox sendFormBox;
    @FXML private VBox linkFormBox;

    @FXML private TextField raastIdField;
    @FXML private TextField amountField;
    @FXML private Label userPhoneLabel;

    @FXML
    public void initialize() {
        setupToggles();
        setupValidations();

        // Load the user's phone number into the Link tab
        if (SessionManager.getInstance().getCurrentUser() != null) {
            // Assuming your User model has a getPhone() method.
            // If not, you might need to fetch it or use placeholder for now.
            userPhoneLabel.setText("Registered FastPay Number");
        }
    }

    private void setupToggles() {
        ToggleGroup group = new ToggleGroup();
        sendRaastToggle.setToggleGroup(group);
        linkRaastToggle.setToggleGroup(group);

        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true); // Prevent deselecting both
            } else {
                boolean isSend = (newVal == sendRaastToggle);

                // Toggle Views
                sendFormBox.setVisible(isSend);
                sendFormBox.setManaged(isSend);
                linkFormBox.setVisible(!isSend);
                linkFormBox.setManaged(!isSend);

                // Toggle Button Styles
                if (isSend) {
                    sendRaastToggle.setStyle("-fx-background-color: #4da6ff; -fx-text-fill: white; -fx-background-radius: 25;");
                    linkRaastToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #4a5568; -fx-font-weight: bold;");
                } else {
                    linkRaastToggle.setStyle("-fx-background-color: #4da6ff; -fx-text-fill: white; -fx-background-radius: 25;");
                    sendRaastToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #4a5568; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupValidations() {
        // Amount should only accept numbers and decimals
        amountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));

        // Raast IDs are alphanumeric (IBAN) or numeric (Phone).
        // We will allow standard alphanumeric input but prevent spaces.
        raastIdField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[a-zA-Z0-9]*") ? change : null));
    }

    @FXML
    protected void handleSendPayment() {
        String raastId = raastIdField.getText().trim();
        String amount = amountField.getText().trim();

        if (raastId.isEmpty() || amount.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter both Raast ID and Amount.").show();
            return;
        }

        // Placeholder for future DB integration
        System.out.println("Initiating Raast transfer to: " + raastId + " for Rs. " + amount);
        new Alert(Alert.AlertType.INFORMATION, "Raast integration coming soon!").show();
    }

    @FXML
    protected void handleLinkRaast() {
        // Placeholder for future DB integration
        System.out.println("Linking Raast ID...");
        new Alert(Alert.AlertType.INFORMATION, "Your FastPay number has been linked to Raast!").show();
    }

    // --- PROPER BACK NAVIGATION ---
    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());

            // Re-apply the global stylesheet
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());

            // Swap the scene on the current stage
            javafx.stage.Stage stage = (javafx.stage.Stage) sendRaastToggle.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CRITICAL: Could not return to dashboard.");
        }
    }
}