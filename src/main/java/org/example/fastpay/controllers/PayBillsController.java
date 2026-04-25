package org.example.fastpay.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PayBillsController {

    @FXML private TextField searchField;
    @FXML private FlowPane billerGrid;
    @FXML private VBox savedBillsContainer; // The new Right Panel container

    private List<BillerItem> allBillers = new ArrayList<>();

    private static class BillerItem {
        String name; String icon;
        BillerItem(String name, String icon) { this.name = name; this.icon = icon; }
    }

    @FXML
    public void initialize() {
        populateBillerData();
        renderBillers(allBillers);
        loadSavedBills(); // Inject the saved bills into the right panel

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                renderBillers(allBillers);
            } else {
                List<BillerItem> filtered = new ArrayList<>();
                for (BillerItem b : allBillers) {
                    if (b.name.toLowerCase().contains(newVal.toLowerCase())) {
                        filtered.add(b);
                    }
                }
                renderBillers(filtered);
            }
        });
    }

    // --- NEW: RIGHT PANEL LOGIC ---
    private void loadSavedBills() {
        savedBillsContainer.getChildren().clear();

        // Hardcoded dummy data. Later, fetch this from Supabase.
        addSavedBillToPanel("⚡", "Home LESCO", "Consumer #: 1122334455");
        addSavedBillToPanel("🎓", "FAST NUCES", "Roll #: 21i-1234");
        addSavedBillToPanel("🌐", "PTCL Broadband", "Account #: 051-9876543");
        addSavedBillToPanel("💧", "WASA Water", "Consumer #: 99887766");
    }

    private void addSavedBillToPanel(String iconStr, String title, String subtitle) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-cursor: hand; -fx-padding: 12; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 1px; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Hover effect for the row
        row.setOnMouseEntered(e -> row.setStyle("-fx-cursor: hand; -fx-padding: 12; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 10; -fx-background-radius: 10;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand; -fx-padding: 12; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 1px; -fx-border-radius: 10; -fx-background-radius: 10;"));

        // Icon
        Label icon = new Label(iconStr);
        icon.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-min-width: 45px; -fx-min-height: 45px; -fx-alignment: center; -fx-font-size: 20px;");

        // Text
        VBox textCol = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
        textCol.getChildren().addAll(titleLbl, subtitleLbl);

        // Spacer to push the arrow to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Arrow indicator
        Label arrow = new Label("→");
        arrow.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 16px; -fx-font-weight: bold;");

        row.getChildren().addAll(icon, textCol, spacer, arrow);

        // Click Action
        row.setOnMouseClicked(e -> handleBillerClick(title));

        savedBillsContainer.getChildren().add(row);
    }

    // --- EXISTING LEFT GRID LOGIC ---
    private void populateBillerData() {
        allBillers.add(new BillerItem("Electricity", "⚡"));
        allBillers.add(new BillerItem("Telephone", "☎️"));
        allBillers.add(new BillerItem("Internet", "🌐"));
        allBillers.add(new BillerItem("Gas", "🔥"));
        allBillers.add(new BillerItem("Water", "💧"));
        allBillers.add(new BillerItem("Solar", "☀️"));
        allBillers.add(new BillerItem("Education", "🎓"));
        allBillers.add(new BillerItem("Credit Card", "💳"));
        allBillers.add(new BillerItem("Government Fees", "🏛️"));
        allBillers.add(new BillerItem("Leisure Clubs", "🏌️"));
        allBillers.add(new BillerItem("Investments", "📈"));
        allBillers.add(new BillerItem("Nadra Fee", "🪪"));
        allBillers.add(new BillerItem("Housing Societies", "🏘️"));
        allBillers.add(new BillerItem("1Bill Credit Cards", "🏦"));
        allBillers.add(new BillerItem("1 Bill Invoice/Voucher", "🧾"));
        allBillers.add(new BillerItem("Kuickpay", "💸"));
        allBillers.add(new BillerItem("Paypro", "💼"));
        allBillers.add(new BillerItem("Haball", "🔗"));
        allBillers.add(new BillerItem("SimPaisa", "📱"));
        allBillers.add(new BillerItem("1 Bill Topup", "⬆️"));
        allBillers.add(new BillerItem("AirSial", "✈️"));
        allBillers.add(new BillerItem("Games", "🎮"));
        allBillers.add(new BillerItem("Term Deposit", "🔒"));
        allBillers.add(new BillerItem("Savings Pocket", "💰"));
        allBillers.add(new BillerItem("Health Insurance", "🏥"));
        allBillers.add(new BillerItem("Mobile Insurance", "🛡️"));
    }

    private void renderBillers(List<BillerItem> billersToRender) {
        billerGrid.getChildren().clear();
        for (BillerItem biller : billersToRender) {
            billerGrid.getChildren().add(createBillerCard(biller));
        }
    }

    private VBox createBillerCard(BillerItem biller) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10.0);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-pref-width: 140; -fx-pref-height: 140; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        card.setCursor(Cursor.HAND);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20; -fx-background-radius: 15; -fx-pref-width: 140; -fx-pref-height: 140; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-pref-width: 140; -fx-pref-height: 140; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);"));

        Label iconLabel = new Label(biller.icon);
        iconLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: #1a2130; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");

        Label nameLabel = new Label(biller.name);
        nameLabel.setStyle("-fx-text-fill: #1a2130; -fx-font-size: 13px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(iconLabel, nameLabel);
        card.setOnMouseClicked(event -> handleBillerClick(biller.name));

        return card;
    }

    private void handleBillerClick(String billerName) {
        System.out.println("Opening payment form for: " + billerName);
        new Alert(Alert.AlertType.INFORMATION, "Payment form for " + billerName + " coming soon!").show();
    }

    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}