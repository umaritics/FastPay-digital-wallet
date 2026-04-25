package org.example.fastpay.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptController {

    @FXML private VBox receiptContent;

    @FXML private Label nameLabel;
    @FXML private Label amountLabel;
    @FXML private Label headerDateLabel;

    @FXML private Label baseAmountLabel;
    @FXML private Label totalAmountLabel;

    @FXML private Label txIdLabel;
    @FXML private Label detailsDateLabel;

    private String rawAmount;
    private String cleanTxId;
    private String webReceiptUrl;

    public void setData(double amount, String name, String phone, String txId) {
        this.rawAmount = String.format("%,.2f", amount);

        // Truncate the UUID to the first segment (e.g., 9B8FDA66)
        this.cleanTxId = txId.split("-")[0].toUpperCase();
        this.webReceiptUrl = "https://fastpay-six.vercel.app/receipt?id=" + txId;

        // Populate Labels
        nameLabel.setText(name);

        String formattedAmount = "Rs. " + rawAmount;
        amountLabel.setText(formattedAmount);
        baseAmountLabel.setText(formattedAmount);
        totalAmountLabel.setText(formattedAmount);

        txIdLabel.setText(cleanTxId);

        // Format Date: "05 Sep 2025, 04:06 PM"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String currentDate = ZonedDateTime.now().format(formatter);
        headerDateLabel.setText(currentDate);
        detailsDateLabel.setText(currentDate);
    }

    @FXML
    protected void handleDownload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        fileChooser.setInitialFileName("FastPay_Receipt_" + cleanTxId + ".png");

        Stage stage = (Stage) amountLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                WritableImage snapshot = receiptContent.snapshot(new javafx.scene.SnapshotParameters(), null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (Exception e) {
                System.out.println("Failed to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleShare() {
        try {
            String message = "I just sent you Rs. " + rawAmount + " via FastPay! 🚀\n" +
                    "Transaction ID: " + cleanTxId + "\n" +
                    "View secure receipt: " + webReceiptUrl;

            String encodedMessage = java.net.URLEncoder.encode(message, "UTF-8").replace("+", "%20");

            // REMOVED phone parameter. This forces WhatsApp to ask "Who do you want to send this to?"
            String waUri = "whatsapp://send?text=" + encodedMessage;

            java.awt.Desktop.getDesktop().browse(new java.net.URI(waUri));
        } catch (Exception e) {
            System.out.println("Could not open WhatsApp: " + e.getMessage());
        }
    }

    @FXML
    protected void handleClose() {
        Stage stage = (Stage) amountLabel.getScene().getWindow();
        stage.close();
    }
}