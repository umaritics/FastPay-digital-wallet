package org.example.fastpay.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.fastpay.services.DatabaseService;
import org.example.fastpay.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendMoneyController {

    @FXML private ToggleButton fastPayToggle;
    @FXML private ToggleButton bankToggle;

    @FXML private VBox transferFormBox;
    @FXML private VBox bankListBox;
    @FXML private VBox otherBankFormBox;
    @FXML private VBox contactsContainer; // For dynamic contacts

    @FXML private TextField phoneField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> partitionCombo;
    @FXML private Label partitionWarningLabel;
    @FXML private Button sendBtn;

    @FXML private TextField bankSearchField;
    @FXML private ListView<String> bankListView;

    @FXML private Label selectedBankLabel;
    @FXML private RadioButton ibanRadio;
    @FXML private RadioButton accountRadio;
    @FXML private TextField bankAccountField;
    @FXML private TextField otherBankAmountField;
    @FXML private ComboBox<String> otherBankPartitionCombo;

    private String token;
    private String currentUserId;
    private List<PartitionItem> allPartitions = new ArrayList<>();
    private ObservableList<String> masterBankList;

    private static class PartitionItem {
        String id; String name; double balance;
        PartitionItem(String id, String name, double balance) { this.id = id; this.name = name; this.balance = balance; }
        @Override public String toString() { return name + " (Rs. " + String.format("%,.2f", balance) + ")"; }
    }

    @FXML
    public void initialize() {
        token = SessionManager.getInstance().getAccessToken();
        currentUserId = SessionManager.getInstance().getCurrentUser().getId();

        setupToggles();
        setupInputValidations();
        setupBankList();
        loadUserPartitions();
        loadDynamicContacts(); // NEW!
    }

    private void loadDynamicContacts() {
        Platform.runLater(() -> {
            contactsContainer.getChildren().clear();
            JSONArray users = DatabaseService.getFastPayContacts(currentUserId,token);

            for (int i = 0; i < users.length(); i++) {
                JSONObject u = users.getJSONObject(i);
                String phone = u.optString("phone", "");
                String name = u.optString("full_name", "Unknown");

                // Skip users with missing phones or the current user's own profile (optional)
                if (phone.isEmpty()) continue;

                HBox contactBox = new HBox(15);
                contactBox.setAlignment(Pos.CENTER_LEFT);
                contactBox.setStyle("-fx-cursor: hand; -fx-padding: 10; -fx-background-color: transparent; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

                // Avatar
                Label avatar = new Label(name.substring(0, 1).toUpperCase());
                avatar.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 50%; -fx-min-width: 45px; -fx-min-height: 45px; -fx-alignment: center; -fx-text-fill: #1a2130; -fx-font-weight: bold;");

                VBox textCol = new VBox(2);
                Label nameLabel = new Label(name);
                nameLabel.setStyle("-fx-text-fill: #1a2130; -fx-font-weight: bold; -fx-font-size: 14px;");
                Label phoneLabel = new Label(phone);
                phoneLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 12px;");
                textCol.getChildren().addAll(nameLabel, phoneLabel);

                contactBox.getChildren().addAll(avatar, textCol);

                // Click action
                contactBox.setOnMouseClicked(e -> {
                    phoneField.setText(phone);
                    fastPayToggle.setSelected(true);
                });

                contactsContainer.getChildren().add(contactBox);
            }
        });
    }

    private void setupToggles() {
        ToggleGroup topGroup = new ToggleGroup();
        fastPayToggle.setToggleGroup(topGroup);
        bankToggle.setToggleGroup(topGroup);

        topGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
            else {
                boolean isFastPay = (newVal == fastPayToggle);
                transferFormBox.setVisible(isFastPay); transferFormBox.setManaged(isFastPay);
                if (isFastPay) {
                    bankListBox.setVisible(false); bankListBox.setManaged(false);
                    otherBankFormBox.setVisible(false); otherBankFormBox.setManaged(false);
                    fastPayToggle.setStyle("-fx-background-color: #4da6ff; -fx-text-fill: white; -fx-background-radius: 25;");
                    bankToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #4a5568; -fx-font-weight: bold;");
                } else {
                    showBankList();
                    bankToggle.setStyle("-fx-background-color: #4da6ff; -fx-text-fill: white; -fx-background-radius: 25;");
                    fastPayToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #4a5568; -fx-font-weight: bold;");
                }
            }
        });

        ToggleGroup bankInputGroup = new ToggleGroup();
        ibanRadio.setToggleGroup(bankInputGroup);
        accountRadio.setToggleGroup(bankInputGroup);
        bankInputGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            bankAccountField.setPromptText(newVal == ibanRadio ? "Enter IBAN (PK...)" : "Enter Account Number");
            bankAccountField.clear();
        });
    }

    private void setupBankList() {
        String[] banks = {
                "State Bank of Pakistan", "Habib Bank Limited", "United Bank Limited", "National Bank of Pakistan",
                "MCB Bank Limited", "Allied Bank Limited", "Bank Alfalah", "Faysal Bank", "Askari Bank", "Meezan Bank",
                "SadaPay", "NayaPay", "Easypaisa", "JazzCash"
        };
        Arrays.sort(banks);
        masterBankList = FXCollections.observableArrayList(banks);

        FilteredList<String> filteredData = new FilteredList<>(masterBankList, p -> true);
        bankSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(bank -> newVal == null || newVal.isEmpty() || bank.toLowerCase().contains(newVal.toLowerCase()));
        });
        bankListView.setItems(filteredData);

        bankListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else {
                    HBox box = new HBox(15); box.setAlignment(Pos.CENTER_LEFT);
                    Label icon = new Label(item.substring(0, 1));
                    icon.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 50%; -fx-min-width: 35px; -fx-min-height: 35px; -fx-alignment: center; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
                    Label text = new Label(item); text.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a2130;");
                    box.getChildren().addAll(icon, text);
                    setGraphic(box);
                }
            }
        });

        bankListView.setOnMouseClicked(e -> {
            String selected = bankListView.getSelectionModel().getSelectedItem();
            if (selected != null) { selectedBankLabel.setText(selected); openOtherBankForm(); }
        });
    }

    @FXML protected void showBankList() { transferFormBox.setVisible(false); transferFormBox.setManaged(false); otherBankFormBox.setVisible(false); otherBankFormBox.setManaged(false); bankListBox.setVisible(true); bankListBox.setManaged(true); }
    private void openOtherBankForm() { bankListBox.setVisible(false); bankListBox.setManaged(false); otherBankFormBox.setVisible(true); otherBankFormBox.setManaged(true); }

    private void setupInputValidations() {
        phoneField.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("^$|^0$|^03\\d{0,9}$") ? change : null));
        amountField.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
        otherBankAmountField.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));

        amountField.textProperty().addListener((obs, old, newVal) -> filterPartitions(newVal, partitionCombo));
        otherBankAmountField.textProperty().addListener((obs, old, newVal) -> filterPartitions(newVal, otherBankPartitionCombo));
    }

    private void loadUserPartitions() {
        Platform.runLater(() -> {
            allPartitions.clear();
            JSONArray partitions = DatabaseService.getUserPartitions(currentUserId, token);
            for (int i = 0; i < partitions.length(); i++) {
                JSONObject p = partitions.getJSONObject(i);
                allPartitions.add(new PartitionItem(p.getString("id"), p.optString("name", "Unnamed"), p.getDouble("balance")));
            }
            filterPartitions("", partitionCombo);
            filterPartitions("", otherBankPartitionCombo);
        });
    }

    private void filterPartitions(String amountStr, ComboBox<String> targetCombo) {
        double amountRequired = 0.0;
        if (!amountStr.isEmpty()) { try { amountRequired = Double.parseDouble(amountStr); } catch (Exception ignored) {} }

        targetCombo.getItems().clear();
        boolean hasValid = false;
        for (PartitionItem p : allPartitions) {
            if (p.balance >= amountRequired) { targetCombo.getItems().add(p.toString()); hasValid = true; }
        }
        if (hasValid) targetCombo.getSelectionModel().selectFirst();

        if (targetCombo == partitionCombo) {
            sendBtn.setDisable(!hasValid);
            partitionWarningLabel.setText(hasValid ? "" : "Insufficient funds.");
        }
    }

    // THE RESTORED TRANSFER LOGIC
    @FXML
    protected void handleSendMoney() {
        String phone = phoneField.getText().trim();
        String amountStr = amountField.getText().trim();
        String selectedPartitionString = partitionCombo.getValue();

        if (phone.length() != 11) { new Alert(Alert.AlertType.WARNING, "Phone number must be exactly 11 digits.").show(); return; }
        if (amountStr.isEmpty() || selectedPartitionString == null) return;

        double amount = Double.parseDouble(amountStr);
        String senderPartitionId = null;

        for (PartitionItem p : allPartitions) {
            if (p.toString().equals(selectedPartitionString)) { senderPartitionId = p.id; break; }
        }

        if (senderPartitionId == null) return;

        sendBtn.setDisable(true);
        sendBtn.setText("Processing Transfer...");

        final String finalSenderPartId = senderPartitionId;

        Task<JSONObject> transferTask = new Task<>() {
            @Override
            protected JSONObject call() {
                return DatabaseService.transferP2P(currentUserId, finalSenderPartId, phone, amount, token);
            }
        };

        transferTask.setOnSucceeded(e -> {
            JSONObject result = transferTask.getValue();
            if (result != null && result.optBoolean("success", false)) {
                String transactionId = result.getString("transaction_id");
                String receiverName = result.getString("receiver_name");

                phoneField.clear(); amountField.clear();

                //System.out.println("SUCCESS! ID: " + transactionId);
                showReceiptDialog(transactionId, receiverName, phone, amount);
                //new Alert(Alert.AlertType.INFORMATION, "Successfully sent Rs. " + amount + " to " + receiverName).show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Transfer failed. Please check the number or try again.").show();
            }
            sendBtn.setDisable(false); sendBtn.setText("Next");
            loadUserPartitions();
            loadDynamicContacts(); // Refresh balances and contacts
        });

        transferTask.setOnFailed(e -> {
            new Alert(Alert.AlertType.ERROR, "Network error occurred.").show();
            sendBtn.setDisable(false); sendBtn.setText("Next");
        });

        new Thread(transferTask).start();
    }

    private void showReceiptDialog(String txId, String receiverName, String receiverPhone, double amount) {
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/fastpay/views/receipt-dialog.fxml"));
                javafx.scene.Parent dialogContent = fxmlLoader.load();

                ReceiptController controller = fxmlLoader.getController();
                controller.setData(amount, receiverName, receiverPhone, txId);

                javafx.stage.Stage dialogStage = new javafx.stage.Stage();
                dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Makes it look like a sleek card

                javafx.scene.Scene scene = new javafx.scene.Scene(dialogContent);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                dialogStage.setScene(scene);

                dialogStage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML protected void handleOtherBankSend() { new Alert(Alert.AlertType.INFORMATION, "Integration with " + selectedBankLabel.getText() + " is coming soon!").show(); }

    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) fastPayToggle.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

}