package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.fastpay.models.User;
import org.example.fastpay.services.DatabaseService;
import org.example.fastpay.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;

public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label generalBalanceLabel;
    @FXML private HBox partitionsContainer;
    @FXML private TableView<?> transactionsTable;

    private User currentUser;
    private String token;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        token = SessionManager.getInstance().getAccessToken();

        if (currentUser != null) {
            String namePrefix = currentUser.getEmail().split("@")[0];
            // Capitalize first letter
            namePrefix = namePrefix.substring(0, 1).toUpperCase() + namePrefix.substring(1);
            greetingLabel.setText("Hello, " + namePrefix + " 👋");

            // Load data asynchronously so the UI doesn't freeze
            Platform.runLater(this::loadWalletData);
        }
    }

    private void loadWalletData() {
        partitionsContainer.getChildren().clear();
        double totalBalance = 0.0;

        JSONArray partitions = DatabaseService.getUserPartitions(currentUser.getId(), token);

        for (int i = 0; i < partitions.length(); i++) {
            JSONObject partition = partitions.getJSONObject(i);
            String name = partition.getString("name");
            double balance = partition.getDouble("balance");
            boolean isGeneral = partition.getBoolean("is_general");

            totalBalance += balance;

            if (isGeneral) {
                // Update the static General Partition UI
                generalBalanceLabel.setText(String.format("Rs. %,.2f", balance));
            } else {
                // Create dynamic cards for custom partitions
                VBox card = createPartitionCard(name, balance);
                partitionsContainer.getChildren().add(card);
            }
        }

        // Update Total Balance at the top of the light blue card
        totalBalanceLabel.setText(String.format("Rs. %,.2f", totalBalance));
    }

    // HELPER: Generates the UI for custom partitions
    private VBox createPartitionCard(String name, double balance) {
        VBox card = new VBox();
        card.setPrefWidth(140.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setPadding(new Insets(15.0));
        card.setSpacing(5.0);

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(javafx.scene.paint.Color.web("#4a5568"));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14.0));

        Label balanceLabel = new Label(String.format("Rs. %,.2f", balance));
        balanceLabel.setTextFill(javafx.scene.paint.Color.web("#1a2130"));

        card.getChildren().addAll(nameLabel, balanceLabel);
        return card;
    }

    @FXML
    protected void openCreatePartitionDialog() {
        // Professional JavaFX Custom Dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Virtual Partition");
        dialog.setHeaderText("Create a new money partition");
        dialog.setContentText("Partition Name (e.g., Savings, Groceries):");

        // Remove standard window icon for a cleaner fintech look
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                boolean success = DatabaseService.createPartition(currentUser.getId(), name.trim(), token);
                if (success) {
                    loadWalletData(); // Refresh the UI instantly
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create partition. Please try again.");
                    alert.show();
                }
            }
        });
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