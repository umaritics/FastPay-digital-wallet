package org.example.fastpay.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class QrInvoiceDialogController {

    @FXML private ImageView qrImageView;
    @FXML private Label nameLabel;
    @FXML private Label accountIdLabel;
    @FXML private Label amountLabel;
    @FXML private Label expiryLabel;

    // This is the missing method! It receives the data and injects it into the UI.
    public void setData(Image qrImage, String name, String maskedId, double amount, Instant expiry) {
        qrImageView.setImage(qrImage);
        nameLabel.setText(name);
        accountIdLabel.setText(maskedId);
        amountLabel.setText(String.format("PKR %,.2f", amount));

        // Format the Expiry Date beautifully (e.g., 12 May, 2026)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
                .withZone(ZoneId.systemDefault());
        expiryLabel.setText("Expiry: " + formatter.format(expiry));
    }

    @FXML
    protected void handleClose() {
        // Find the window the button belongs to and close it
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}