package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.fastpay.models.User;
import org.example.fastpay.services.DatabaseService;
import org.example.fastpay.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label generalBalanceLabel;
    @FXML private Button toggleBalanceBtn;
    @FXML private HBox partitionsContainer;
    @FXML private Pagination servicesPagination;

    private User currentUser;
    private String token;
    private List<String> partitionNames = new ArrayList<>(); // To store names for the dropdown
    private boolean isBalanceHidden = false;
    // Service names for the pagination block
    private final String[] allServices = {
            "Mobile\nTop Up", "Pay Bills", "Bill Split", "Pay\nMerchant",
            "M-Tag\nTop-up", "Donations", "Fast Loan", "Discounts",
            "Remittance", "Online\nPayment", "Insurance", "Traffic\nChallan"
    };

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        token = SessionManager.getInstance().getAccessToken();

        if (currentUser != null) {
            String namePrefix = currentUser.getEmail().split("@")[0];
            namePrefix = namePrefix.substring(0, 1).toUpperCase() + namePrefix.substring(1);
            greetingLabel.setText("Hello, " + namePrefix + " 👋");

            Platform.runLater(this::loadWalletData);
        }

        if (servicesPagination != null) {
            // Force the bullet style strictly through Java
            servicesPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
            // Change page count to 2 since we are showing 6 per page (12 total items)
            servicesPagination.setPageCount(2);
            servicesPagination.setPageFactory(this::createServicePage);
        }
        if (toggleBalanceBtn != null) {
            toggleBalanceBtn.setOnAction(e -> togglePrivacyMode());
        }
    }
    // PRIVACY TOGGLE LOGIC
    private void togglePrivacyMode() {
        isBalanceHidden = !isBalanceHidden;

        // Optional: Swap the eye icon if you have an 'icon-eye-closed.png'
        try {
            String iconName = isBalanceHidden ? "icon-eye-closed.png" : "icon-eye.png";
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/org/example/fastpay/assets/" + iconName)));
            icon.setFitHeight(20.0); icon.setFitWidth(20.0);
            toggleBalanceBtn.setGraphic(icon);
        } catch (Exception ignored) {}

        // Reload the UI to apply the masks
        loadWalletData();
    }

    private Node createServicePage(int pageIndex) {
        HBox pageBox = new HBox(15.0);
        pageBox.setAlignment(Pos.CENTER);

        int startIndex = pageIndex * 6;
        int endIndex = Math.min(startIndex + 6, allServices.length);

        for (int i = startIndex; i < endIndex; i++) {
            String serviceName = allServices[i];
            Button serviceBtn = new Button(serviceName);
            serviceBtn.getStyleClass().add("quick-action-card");
            serviceBtn.setPrefWidth(110.0);
            serviceBtn.setPrefHeight(110.0);

            // Format the string: "Mobile Top Up" -> "icon-mobile-top-up.png"
            String formattedFileName = "icon-" + serviceName.toLowerCase().replaceAll("\\s+", "-") + ".png";

            try {
                // Tries to load the specific icon, falls back to placeholder if you haven't downloaded it yet
                java.io.InputStream stream = getClass().getResourceAsStream("/org/example/fastpay/assets/" + formattedFileName);
                if (stream == null) {
                    stream = getClass().getResourceAsStream("/org/example/fastpay/assets/icon-placeholder.png");
                }
                ImageView icon = new ImageView(new Image(stream));
                icon.setFitHeight(30.0);
                icon.setFitWidth(30.0);
                serviceBtn.setGraphic(icon);
            } catch (Exception e) {
                System.out.println("Could not load icon for: " + serviceName);
            }

            pageBox.getChildren().add(serviceBtn);
        }
        return pageBox;
    }

    private void loadWalletData() {
        partitionsContainer.getChildren().clear();
        partitionNames.clear();
        double totalBalance = 0.0;

        JSONArray partitions = DatabaseService.getUserPartitions(currentUser.getId(), token);

        for (int i = 0; i < partitions.length(); i++) {
            JSONObject partition = partitions.getJSONObject(i);
            String name = partition.optString("name", "Unnamed");
            double balance = partition.optDouble("balance", 0.0);
            boolean isGeneral = partition.optBoolean("is_general", false);

            totalBalance += balance;
            partitionNames.add(name);

            // Apply Privacy Masking
            String displayBalance = isBalanceHidden ? "Rs. * * * * *" : String.format("Rs. %,.2f", balance);

            if (isGeneral) {
                generalBalanceLabel.setText(displayBalance);
            } else {
                VBox card = createPartitionCard(name, balance, displayBalance);
                partitionsContainer.getChildren().add(card);
            }
        }

        String displayTotal = isBalanceHidden ? "Rs. * * * * *" : String.format("Rs. %,.2f", totalBalance);
        totalBalanceLabel.setText(displayTotal);
    }

    // BULLETPROOF PARTITION CARD
    private VBox createPartitionCard(String name, double rawBalance, String displayBalance) {
        VBox card = new VBox();

        // Locked Dimensions
        card.setPrefSize(150.0, 85.0);
        card.setMinSize(150.0, 85.0);
        card.setMaxSize(150.0, 85.0);

        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        card.setPadding(new Insets(10.0, 15.0, 10.0, 15.0));
        card.setSpacing(5.0);

        // Name Label (Using inline CSS to guarantee rendering)
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);
        nameLabel.setMinHeight(Control.USE_PREF_SIZE); // Forbids JavaFX from squishing the text to 0 height

        // Balance Label
        Label balanceLabel = new Label(displayBalance);
        balanceLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");
        balanceLabel.setMinHeight(Control.USE_PREF_SIZE);

        card.getChildren().addAll(nameLabel, balanceLabel);
        return card;
    }

    @FXML
    protected void openCreatePartitionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Virtual Partition");
        dialog.setHeaderText("Create and fund a new partition");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15); // Slightly more vertical breathing room
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nameField = new TextField();
        nameField.setPromptText("Partition Name");
        nameField.getStyleClass().add("dialog-input-field");

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.getStyleClass().add("input-field");

        ComboBox<String> sourceCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(partitionNames);
        if (!partitionNames.isEmpty()) sourceCombo.getSelectionModel().selectFirst();
        sourceCombo.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 5;");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Initial Fund (Rs):"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Fund From:"), 0, 2);
        grid.add(sourceCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // --- NEW: INJECT CSS INTO THE DIALOG ---
        DialogPane dialogPane = dialog.getDialogPane();
        try {
            String cssPath = getClass().getResource("/org/example/fastpay/styles/application.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
        } catch (Exception ignored) {}

        // Add a custom class to the dialog pane itself
        dialogPane.getStyleClass().add("custom-dialog");

        // Style the 'Create' button to match your primary blue buttons
        Node createBtn = dialogPane.lookupButton(createButtonType);
        if (createBtn != null) {
            createBtn.getStyleClass().add("primary-btn");
        }
        // ---------------------------------------

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == createButtonType) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                boolean success = DatabaseService.createPartition(currentUser.getId(), name, token);
                if (success) {
                    loadWalletData();
                }
            }
        }
    }

    @FXML
    protected void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/auth-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 900, 650);
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}