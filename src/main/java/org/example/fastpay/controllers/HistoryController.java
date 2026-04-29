package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.fastpay.services.DatabaseService;
import org.example.fastpay.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HistoryController {

    @FXML private VBox transactionsContainer;
    @FXML private ComboBox<String> timeFilterCombo;
    @FXML private Label transactionCountLabel;

    private String token;
    private String userId;
    private JSONArray allTransactions; // Store all so we don't re-fetch from DB on filter change

    @FXML
    public void initialize() {
        token = SessionManager.getInstance().getAccessToken();
        userId = SessionManager.getInstance().getCurrentUser().getId();

        // Setup Filters
        timeFilterCombo.getItems().addAll("All", "Last 7 Days", "Last 30 Days", "Last 3 Months");
        timeFilterCombo.getSelectionModel().select("All");
        timeFilterCombo.setOnAction(e -> renderTransactions());

        Platform.runLater(() -> {
            // Fetch once from DB
            allTransactions = DatabaseService.getTransactions(userId, token);
            renderTransactions();
        });
    }

    private void renderTransactions() {
        transactionsContainer.getChildren().clear();

        if (allTransactions == null || allTransactions.length() == 0) {
            Label emptyLabel = new Label("No transactions found.");
            emptyLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 14px;");
            transactionsContainer.getChildren().add(emptyLabel);
            transactionCountLabel.setText("0 Transactions");
            return;
        }

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        ZonedDateTime now = ZonedDateTime.now();
        String selectedFilter = timeFilterCombo.getValue();
        int visibleCount = 0;

        for (int i = 0; i < allTransactions.length(); i++) {
            JSONObject tx = allTransactions.getJSONObject(i);
            String rawDate = tx.getString("created_at");
            ZonedDateTime txDate = ZonedDateTime.parse(rawDate).withZoneSameInstant(java.time.ZoneId.systemDefault());

            // Time Filtering Logic
            long daysBetween = ChronoUnit.DAYS.between(txDate, now);
            if (selectedFilter.equals("Last 7 Days") && daysBetween > 7) continue;
            if (selectedFilter.equals("Last 30 Days") && daysBetween > 30) continue;
            if (selectedFilter.equals("Last 3 Months") && daysBetween > 90) continue;

            visibleCount++;

            double amount = tx.getDouble("amount");
            String type = tx.getString("type");
            String desc = tx.optString("description", "Transaction");

            // --- UI REFINEMENT FOR THE USER ---
            if (desc.equals("External Card Top-up")) desc = "Account Topup"; // Fix old DB entries

            boolean isIncoming = type.equals("DEPOSIT") || type.equals("TRANSFER_IN");
            boolean isNeutral = type.equals("INTERNAL_TRANSFER");

            String amountPrefix = isIncoming ? "+ Rs. " : "- Rs. ";
            String amountColor = isIncoming ? "#4caf50" : "#ff6b6b";
            String icon = isIncoming ? "⬇" : "⬆";
            String typeLabelText = type.replace("_", " ");

            // Apply Neutral Styling for Partition Transfers
            if (isNeutral) {
                amountPrefix = "Rs. ";
                amountColor = "#4a5568"; // Slate Grey
                icon = "🔄";
                typeLabelText = "Partition Transfer";
            }

            // Build the UI Row
            HBox row = new HBox(15.0);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10 5 10 5; -fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");

            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-background-color: #f4f7f9; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-alignment: center; -fx-font-size: 16px; -fx-text-fill: " + amountColor + ";");

            VBox textCol = new VBox(2.0);
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label typeLabel = new Label(typeLabelText);
            typeLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;");
            textCol.getChildren().addAll(descLabel, typeLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            VBox amountCol = new VBox(2.0);
            amountCol.setAlignment(Pos.CENTER_RIGHT);
            Label amountLabel = new Label(amountPrefix + String.format("%,.2f", amount));
            amountLabel.setStyle("-fx-text-fill: " + amountColor + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label dateLabel = new Label(txDate.format(outputFormatter));
            dateLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;");
            amountCol.getChildren().addAll(amountLabel, dateLabel);

            row.getChildren().addAll(iconLabel, textCol, spacer, amountCol);
            transactionsContainer.getChildren().add(row);
        }

        transactionCountLabel.setText(visibleCount + " Transactions");
    }

    // --- NAVIGATION METHODS WITH WINDOW SIZE PRESERVATION ---
    @FXML
    protected void goBackToDashboard() { routeToScreen("views/dashboard-view.fxml"); }

    @FXML
    protected void openCardManagement() { routeToScreen("views/card-management-view.fxml"); }

    private void routeToScreen(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource(fxmlPath));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());

            javafx.stage.Stage stage = (javafx.stage.Stage) transactionsContainer.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            stage.setScene(scene);
            if (isMax) stage.setMaximized(true);
            else { stage.setWidth(width); stage.setHeight(height); }
        } catch (Exception e) { e.printStackTrace(); }
    }

}