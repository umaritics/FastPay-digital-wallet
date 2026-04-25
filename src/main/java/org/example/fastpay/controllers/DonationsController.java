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

public class DonationsController {

    @FXML private TextField searchField;
    @FXML private FlowPane charityGrid;
    @FXML private VBox featuredCampaignsContainer;

    private List<CharityItem> allCharities = new ArrayList<>();

    private static class CharityItem {
        String name; String icon;
        CharityItem(String name, String icon) { this.name = name; this.icon = icon; }
    }

    @FXML
    public void initialize() {
        populateCharityData();
        renderCharities(allCharities);
        loadFeaturedCampaigns();

        // Real-time Search Filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                renderCharities(allCharities);
            } else {
                List<CharityItem> filtered = new ArrayList<>();
                for (CharityItem c : allCharities) {
                    if (c.name.toLowerCase().contains(newVal.toLowerCase())) {
                        filtered.add(c);
                    }
                }
                renderCharities(filtered);
            }
        });
    }

    // --- RIGHT PANEL LOGIC ---
    private void loadFeaturedCampaigns() {
        featuredCampaignsContainer.getChildren().clear();

        addCampaignToPanel("🕊️", "Gaza Relief Fund", "Alkhidmat Foundation");
        addCampaignToPanel("🍲", "Ramadan Rashan", "Saylani Welfare");
        addCampaignToPanel("🩸", "Blood Donation Drive", "Red Crescent Society");
    }

    private void addCampaignToPanel(String iconStr, String title, String subtitle) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-cursor: hand; -fx-padding: 12; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 1px; -fx-border-radius: 10; -fx-background-radius: 10;");

        row.setOnMouseEntered(e -> row.setStyle("-fx-cursor: hand; -fx-padding: 12; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 10; -fx-background-radius: 10;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand; -fx-padding: 12; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 1px; -fx-border-radius: 10; -fx-background-radius: 10;"));

        Label icon = new Label(iconStr);
        icon.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-min-width: 45px; -fx-min-height: 45px; -fx-alignment: center; -fx-font-size: 20px; -fx-text-fill: #1a2130; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");

        VBox textCol = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
        textCol.getChildren().addAll(titleLbl, subtitleLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("Donate");
        arrow.setStyle("-fx-text-fill: #4da6ff; -fx-font-size: 13px; -fx-font-weight: bold;");

        row.getChildren().addAll(icon, textCol, spacer, arrow);
        row.setOnMouseClicked(e -> handleCharityClick(title));

        featuredCampaignsContainer.getChildren().add(row);
    }

    // --- LEFT GRID LOGIC ---
    private void populateCharityData() {
        allCharities.add(new CharityItem("Alkhidmat Foundation", "🤝"));
        allCharities.add(new CharityItem("Edhi Foundation", "🚑"));
        allCharities.add(new CharityItem("MTJ Foundation", "🕌"));
        allCharities.add(new CharityItem("Red Crescent Society", "🌙"));
        allCharities.add(new CharityItem("Shaukat Khanum", "🏥"));
        allCharities.add(new CharityItem("Saylani Welfare", "🍲"));
        allCharities.add(new CharityItem("Akhuwat", "💸"));
        allCharities.add(new CharityItem("TCF (The Citizens Foundation)", "🏫"));
        allCharities.add(new CharityItem("Transparent Hands", "🖐️"));
        allCharities.add(new CharityItem("WWF Pakistan", "🐼"));
        allCharities.add(new CharityItem("Chhipa Welfare", "🚑"));
        allCharities.add(new CharityItem("Bait-uss-Salam", "🕋"));
        allCharities.add(new CharityItem("SIUT", "🩺"));
        allCharities.add(new CharityItem("Indus Hospital", "🏥"));
        allCharities.add(new CharityItem("JDC Foundation", "⛺"));
    }

    private void renderCharities(List<CharityItem> charitiesToRender) {
        charityGrid.getChildren().clear();
        for (CharityItem charity : charitiesToRender) {
            charityGrid.getChildren().add(createCharityCard(charity));
        }
    }

    private VBox createCharityCard(CharityItem charity) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10.0);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-pref-width: 140; -fx-pref-height: 140; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        card.setCursor(Cursor.HAND);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20; -fx-background-radius: 15; -fx-pref-width: 140; -fx-pref-height: 140; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-pref-width: 140; -fx-pref-height: 140; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);"));

        // Fixed Emoji rendering
        Label iconLabel = new Label(charity.icon);
        iconLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: #1a2130; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");

        Label nameLabel = new Label(charity.name);
        nameLabel.setStyle("-fx-text-fill: #1a2130; -fx-font-size: 13px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(iconLabel, nameLabel);
        card.setOnMouseClicked(event -> handleCharityClick(charity.name));

        return card;
    }

    private void handleCharityClick(String charityName) {
        System.out.println("Opening donation form for: " + charityName);
        new Alert(Alert.AlertType.INFORMATION, "Donation portal for " + charityName + " coming soon!").show();
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