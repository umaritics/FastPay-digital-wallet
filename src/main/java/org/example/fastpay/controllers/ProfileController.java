package org.example.fastpay.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ProfileController {

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label cnicLabel;
    @FXML private Label joinedLabel;
    @FXML private TextField ibanField;

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        // In your real app, fetch this from Supabase / SessionManager
        // e.g., User currentUser = SessionManager.getInstance().getCurrentUser();

        // Mock data loading
        fullNameLabel.setText("Muhammad Umar");
        emailLabel.setText("umar@msmtechnologies.com");
        phoneLabel.setText("0300 1234567");
        cnicLabel.setText("37405-XXXXXXX-X");
        joinedLabel.setText("December 2025");

        // FastPay uses the PK format for IBAN
        ibanField.setText("PK34 FAST 0000 1111 2222 3333");
    }

    @FXML
    protected void handleShareWhatsApp() {
        String iban = ibanField.getText();
        String name = fullNameLabel.getText();

        // Construct the message
        String message = "Hello! Please send the funds to my FastPay account.\n\n"
                + "Account Title: " + name + "\n"
                + "IBAN: " + iban + "\n\n"
                + "Powered by FastPay - Digital Wallet.";

        try {
            // URL encode the message so spaces and newlines work in the browser
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

            // Create the WhatsApp web API URL
            String whatsappUrl = "https://wa.me/?text=" + encodedMessage;

            // Check if the desktop supports opening browsers
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(whatsappUrl));
            } else {
                new Alert(Alert.AlertType.ERROR, "Sharing is not supported on this operating system.").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not launch WhatsApp. Please copy the IBAN manually.").show();
        }
    }

    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());

            javafx.stage.Stage stage = (javafx.stage.Stage) fullNameLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}