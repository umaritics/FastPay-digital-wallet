package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
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
    @FXML private HBox partitionsContainer;
    @FXML private Pagination servicesPagination;

    private User currentUser;
    private String token;
    private List<String> partitionNames = new ArrayList<>(); // To store names for the dropdown

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
    }

    // Creates 6 square buttons with icons per page
    private Node createServicePage(int pageIndex) {
        HBox pageBox = new HBox(15.0); // 15px gap between buttons
        pageBox.setAlignment(Pos.CENTER);

        int startIndex = pageIndex * 6; // 6 items per page now
        int endIndex = Math.min(startIndex + 6, allServices.length);

        for (int i = startIndex; i < endIndex; i++) {
            Button serviceBtn = new Button(allServices[i]);
            serviceBtn.getStyleClass().add("quick-action-card");

            // Make them perfectly square
            serviceBtn.setPrefWidth(110.0);
            serviceBtn.setPrefHeight(110.0);

            // Add a placeholder image for the icon.
            // Save your downloaded PNGs as service-0.png, service-1.png, etc.
            try {
                ImageView icon = new ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/org/example/fastpay/assets/icon-placeholder.png")));
                icon.setFitHeight(30.0);
                icon.setFitWidth(30.0);
                serviceBtn.setGraphic(icon);
            } catch (Exception e) {
                System.out.println("Icon missing for: " + allServices[i]);
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
        System.out.println("DEBUG - Partitions: " + partitions.toString());

        for (int i = 0; i < partitions.length(); i++) {
            JSONObject partition = partitions.getJSONObject(i);
            String name = partition.optString("name", "Unnamed");
            double balance = partition.optDouble("balance", 0.0);
            boolean isGeneral = partition.optBoolean("is_general", false);

            totalBalance += balance;
            partitionNames.add(name); // Add to list for the dropdown

            if (isGeneral) {
                generalBalanceLabel.setText(String.format("Rs. %,.2f", balance));
            } else {
                VBox card = createPartitionCard(name, balance);
                partitionsContainer.getChildren().add(card);
            }
        }
        totalBalanceLabel.setText(String.format("Rs. %,.2f", totalBalance));
    }

    private VBox createPartitionCard(String name, double balance) {
        VBox card = new VBox();
        card.setPrefWidth(140.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        card.setPadding(new Insets(15.0));
        card.setSpacing(5.0);

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(javafx.scene.paint.Color.web("#1a2130"));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14.0));

        Label balanceLabel = new Label(String.format("Rs. %,.2f", balance));
        balanceLabel.setTextFill(javafx.scene.paint.Color.web("#4a5568"));

        card.getChildren().addAll(nameLabel, balanceLabel);
        return card;
    }

    @FXML
    protected void openCreatePartitionDialog() {
        // Custom Dialog Box
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Virtual Partition");
        dialog.setHeaderText("Create and fund a new partition");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Grid for inputs
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Partition Name");
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        ComboBox<String> sourceCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(partitionNames);
        if (!partitionNames.isEmpty()) sourceCombo.getSelectionModel().selectFirst();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Initial Fund (Rs):"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Fund From:"), 0, 2);
        grid.add(sourceCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == createButtonType) {
            String name = nameField.getText().trim();
            // TODO: In future iterations, process the 'amountField' and 'sourceCombo' values to deduct/transfer funds.
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