package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    private java.util.LinkedHashMap<String, String> partitionMap = new java.util.LinkedHashMap<>();    private boolean isBalanceHidden = false;
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

            serviceBtn.setOnAction(event -> handleServiceClick(serviceName));

            pageBox.getChildren().add(serviceBtn);
        }
        return pageBox;
    }


    private void handleServiceClick(String serviceName) {
        String fxmlFile = "";

        // 1. Map the service name to the correct FXML file
        switch (serviceName) {
            case "Mobile\nTop Up":
                fxmlFile = "views/mobile-topup-view.fxml";
                break;
            case "Pay Bills":
                fxmlFile = "views/pay-bills-view.fxml";
                break;
            case "Donations":
                fxmlFile = "views/donations-view.fxml";
                break;
            case "M-Tag\nTop-up":
                fxmlFile = "views/mtag-view.fxml";
                break;
            default:
                // If a button is clicked that doesn't have a screen yet
                System.out.println("Screen not implemented yet for: " + serviceName);
                // Optional: Show a quick JavaFX Alert here saying "Coming Soon!"
                return;
        }

        // 2. Perform the Scene Switch
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource(fxmlFile));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());

            // Re-apply your global stylesheet
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());

            // Grab the current window (Stage) from the active layout
            // Note: Replace 'servicesPagination' with whatever the ID of your pagination/container is
            javafx.stage.Stage stage = (javafx.stage.Stage) servicesPagination.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            System.out.println("CRITICAL: Failed to load screen for " + serviceName);
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleRefresh() {
        loadWalletData();
    }

    private void loadWalletData() {
        partitionsContainer.getChildren().clear();
        double totalBalance = 0.0;

        JSONArray partitions = DatabaseService.getUserPartitions(currentUser.getId(), token);

        for (int i = 0; i < partitions.length(); i++) {
            JSONObject partition = partitions.getJSONObject(i);
            String id = partition.getString("id"); // Get the Database UUID
            String name = partition.optString("name", "Unnamed");
            double balance = partition.optDouble("balance", 0.0);
            boolean isGeneral = partition.optBoolean("is_general", false);

            totalBalance += balance;

            // Map the visual name to the database ID!
            partitionMap.put(name, id);

            // Apply Privacy Masking
            String displayBalance = isBalanceHidden ? "Rs. * * * * *" : String.format("Rs. %,.2f", balance);

            if (isGeneral) {
                generalBalanceLabel.setText(displayBalance);
            } else {
                // We now pass the 'id' as the first parameter
                VBox card = createPartitionCard(id, name, balance, displayBalance);
                partitionsContainer.getChildren().add(card);
            }
        }

        String displayTotal = isBalanceHidden ? "Rs. * * * * *" : String.format("Rs. %,.2f", totalBalance);
        totalBalanceLabel.setText(displayTotal);
    }

    private VBox createPartitionCard(String partitionId, String name, double rawBalance, String displayBalance) {
        VBox card = new VBox();
        card.setPrefSize(150.0, 85.0);
        card.setMinSize(150.0, 85.0);
        card.setMaxSize(150.0, 85.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        card.setPadding(new Insets(10.0, 15.0, 10.0, 15.0));
        card.setSpacing(5.0);

        // Header HBox (Name + Menu)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(100.0);
        nameLabel.setMinHeight(Control.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Context Menu (Three Dots)
        MenuButton menuButton = new MenuButton("");
        menuButton.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0;");

        MenuItem topUpItem = new MenuItem("Top Up");
        topUpItem.setOnAction(e -> openTopUpDialog(partitionId, name));

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setStyle("-fx-text-fill: #ff6b6b;");
        deleteItem.setOnAction(e -> handleDeletePartition(partitionId, name));

        menuButton.getItems().addAll(topUpItem, deleteItem);
        header.getChildren().addAll(nameLabel, spacer, menuButton);

        Label balanceLabel = new Label(displayBalance);
        balanceLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");
        balanceLabel.setMinHeight(Control.USE_PREF_SIZE);

        card.getChildren().addAll(header, balanceLabel);
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
        nameField.getStyleClass().add("input-field-dark");

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.getStyleClass().add("input-field-dark");
        amountField.setTextFormatter(new TextFormatter<>(change -> {
            // Allows empty string, or digits with at most one decimal point
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null; // Rejects the keystroke if it's a letter or a minus sign
        }));

        ComboBox<String> sourceCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(partitionMap.keySet()); // Load names from the Map
        if (!partitionMap.isEmpty()) sourceCombo.getSelectionModel().select("General");
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
            String amountStr = amountField.getText().trim();

            // Retrieve the hidden UUID based on the selected name
            String sourcePartitionId = partitionMap.get(sourceCombo.getValue());

            double initialBalance = Double.parseDouble(amountField.getText().trim());
            if (initialBalance <= 0) {
                new Alert(Alert.AlertType.ERROR, "Amount must be greater than zero.").show();
                return; // Stop execution
            }

            if (!name.isEmpty() && sourcePartitionId != null) {
                boolean success = DatabaseService.createPartition(currentUser.getId(), name, sourcePartitionId, initialBalance, token);
                if (success) {
                    loadWalletData(); // UI refreshes showing the deducted source and the new partition!
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Insufficient funds in the source partition or network error.");
                    error.show();
                }
            }
        }
    }

    @FXML
    protected void openAddMoneyPopup() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Top-up Wallet");
        dialog.setHeaderText("Choose your funding method");

        // 1. Create a Custom Layout for the center of the Dialog
        javafx.scene.layout.HBox contentBox = new javafx.scene.layout.HBox(30);
        contentBox.setAlignment(javafx.geometry.Pos.CENTER);
        contentBox.setPadding(new javafx.geometry.Insets(30, 40, 30, 40));

        // 2. Create the Big Bank Button
        Button bankBtn = new Button("Bank / Debit Card");
        bankBtn.getStyleClass().add("quick-action-card"); // Reusing your dashboard style!
        bankBtn.setPrefSize(180, 180);
        try {
            javafx.scene.image.ImageView bankIcon = new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/org/example/fastpay/assets/icon-bank-transfer.png")));
            bankIcon.setFitWidth(50); bankIcon.setFitHeight(50);
            bankBtn.setGraphic(bankIcon);
            bankBtn.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
        } catch (Exception ignored) {}

        // 3. Create the Big QR Button
        Button qrBtn = new Button("QR Payment Slip");
        qrBtn.getStyleClass().add("quick-action-card");
        qrBtn.setPrefSize(180, 180);
        try {
            javafx.scene.image.ImageView qrIcon = new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/org/example/fastpay/assets/icon-qr-topup.png")));
            qrIcon.setFitWidth(50); qrIcon.setFitHeight(50);
            qrBtn.setGraphic(qrIcon);
            qrBtn.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
        } catch (Exception ignored) {}

        contentBox.getChildren().addAll(bankBtn, qrBtn);

        // 4. Inject the layout into the Dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(contentBox);
        dialogPane.getStyleClass().add("custom-dialog");
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/org/example/fastpay/styles/application.css").toExternalForm());
        } catch (Exception ignored) {}

        // 5. Add a hidden Cancel button so the window 'X' still works
        dialogPane.getButtonTypes().add(ButtonType.CANCEL);
        javafx.scene.Node closeBtn = dialogPane.lookupButton(ButtonType.CANCEL);
        closeBtn.setVisible(false);
        closeBtn.setManaged(false);

        // 6. Handle Clicks manually to close the dialog and route
        bankBtn.setOnAction(e -> {
            dialog.setResult("BANK");
            dialog.close();
        });

        qrBtn.setOnAction(e -> {
            dialog.setResult("QR");
            dialog.close();
        });

        // 7. Process the Result
        java.util.Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (result.get().equals("BANK")) {
                routeToScreen("views/add-money-view.fxml");
            } else if (result.get().equals("QR")) {
                generateQRSplash();
            }
        }
    }

    // Helper method to keep window sizing consistent across routing
    private void routeToScreen(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource(fxmlPath));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());

            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            stage.setScene(scene);

            if (isMax) stage.setMaximized(true);
            else { stage.setWidth(width); stage.setHeight(height); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openTopUpDialog(String targetPartitionId, String targetPartitionName) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Top Up Partition");
        dialog.setHeaderText("Transfer funds into: " + targetPartitionName);

        ButtonType transferBtn = new ButtonType("Transfer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(transferBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (Rs)");

        ComboBox<String> sourceCombo = new ComboBox<>();
        // Add all partitions except the one we are transferring TO
        for (String pName : partitionMap.keySet()) {
            if (!pName.equals(targetPartitionName)) sourceCombo.getItems().add(pName);
        }
        if (!sourceCombo.getItems().isEmpty()) sourceCombo.getSelectionModel().selectFirst();

        grid.add(new Label("Fund From:"), 0, 0); grid.add(sourceCombo, 1, 0);
        grid.add(new Label("Amount:"), 0, 1); grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        try { dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/example/fastpay/styles/application.css").toExternalForm()); } catch (Exception ignored) {}

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == transferBtn) {
            String sourceId = partitionMap.get(sourceCombo.getValue());
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (DatabaseService.transferFunds(currentUser.getId(), sourceId, targetPartitionId, amount, token)) {
                    loadWalletData(); // Refresh UI instantly
                } else {
                    new Alert(Alert.AlertType.ERROR, "Insufficient funds or network error.").show();
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid amount.").show();
            }
        }
    }
    private void generateQRSplash() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("QR Payment Slip");
        dialog.setHeaderText("Generate a secure payment request");

        ButtonType generateBtn = new ButtonType("Generate Slip", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateBtn, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(20));

        Label instruction = new Label("Enter amount to request (PKR):");
        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 1500");
        amountField.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");

        amountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));

        content.getChildren().addAll(instruction, amountField);
        dialog.getDialogPane().setContent(content);

        try { dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/example/fastpay/styles/application.css").toExternalForm()); } catch (Exception ignored) {}

        java.util.Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == generateBtn) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new NumberFormatException();

                // 1. FINTECH ARCHITECTURE: Calculate and Store Expiry Date (24 hours)
                java.time.Instant expiryInstant = java.time.Instant.now().plus(java.time.Duration.ofHours(24));
                String invoiceId = DatabaseService.createQrInvoice(currentUser.getId(), amount, expiryInstant, token);

                if (invoiceId != null) {
                    String paymentUrl = "https://fastpay-six.vercel.app/pay?invoice=" + invoiceId;

                    // 2. Generate QR
                    com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
                    com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(paymentUrl, com.google.zxing.BarcodeFormat.QR_CODE, 250, 250);
                    java.awt.image.BufferedImage bufferedImage = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
                    javafx.scene.image.Image qrImage = javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null);

                    // 3. SECURE MASKING: Get General partition ID and create IBAN style mask
                    String generalPartitionId = partitionMap.get("General");

                    // Fallback just in case the ID is unusually short or null
                    if (generalPartitionId == null || generalPartitionId.length() < 4) {
                        generalPartitionId = "0000000000000000";
                    }

                    String maskedId = "PK** **** **** **** **" + generalPartitionId.substring(generalPartitionId.length() - 4);

                    // 4. LOAD CUSTOM FINTECH INVOICE DIALOG
                    javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/qr-invoice-dialog.fxml"));                    javafx.scene.Parent dialogContent = fxmlLoader.load();
                    QrInvoiceDialogController controller = fxmlLoader.getController();

                    // 5. Populate the Controller
                    controller.setData(qrImage, currentUser.getEmail().split("@")[0].toUpperCase(), maskedId, amount, expiryInstant);

                    // 6. Show the Undecorated Modern Dialog
                    javafx.stage.Stage dialogStage = new javafx.stage.Stage();
                    dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

                    javafx.scene.Scene scene = new javafx.scene.Scene(dialogContent);
                    dialogStage.setScene(scene);
                    dialogStage.showAndWait();

                } else {
                    new Alert(Alert.AlertType.ERROR, "Network error generating invoice.").show();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
            }
        }
    }

    private void handleDeletePartition(String partitionId, String partitionName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Partition");
        alert.setHeaderText("Are you sure you want to delete '" + partitionName + "'?");
        alert.setContentText("Any remaining balance will be safely swept back into your General Partition.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DatabaseService.deletePartition(currentUser.getId(), partitionId, token)) {
                loadWalletData(); // UI refreshes, card disappears, general balance updates
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete partition.").show();
            }
        }
    }

    //---------------------------------------------------------
    // Routing Functions
    //---------------------------------------------------------
    @FXML
    protected void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/auth-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();

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
    protected void openCardManagement() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/card-management-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // 1. Get the current stage from any button on the screen
            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();

            // 2. Capture current state BEFORE setting the scene
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            // 3. Set the new scene
            stage.setScene(scene);

            // 4. Reapply the state
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
    protected void openHistory() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/history-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // 1. Get the current stage from any button on the screen
            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();

            // 2. Capture current state BEFORE setting the scene
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            // 3. Set the new scene
            stage.setScene(scene);

            // 4. Reapply the state
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
    protected void openSendMoney() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/send-money-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // 1. Get the current stage from any button on the screen
            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();

            // 2. Capture current state BEFORE setting the scene
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            // 3. Set the new scene
            stage.setScene(scene);

            // 4. Reapply the state
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
    protected void openRaast() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/raast-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // 1. Get the current stage from any button on the screen
            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();

            // 2. Capture current state BEFORE setting the scene
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            // 3. Set the new scene
            stage.setScene(scene);

            // 4. Reapply the state
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
    protected void openMobile() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/mobile-topup-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // 1. Get the current stage from any button on the screen
            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();

            // 2. Capture current state BEFORE setting the scene
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            // 3. Set the new scene
            stage.setScene(scene);

            // 4. Reapply the state
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
    protected void openPayBill() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/pay-bills-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            String cssPath = org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // 1. Get the current stage from any button on the screen
            javafx.stage.Stage stage = (javafx.stage.Stage) greetingLabel.getScene().getWindow();

            // 2. Capture current state BEFORE setting the scene
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMax = stage.isMaximized();

            // 3. Set the new scene
            stage.setScene(scene);

            // 4. Reapply the state
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