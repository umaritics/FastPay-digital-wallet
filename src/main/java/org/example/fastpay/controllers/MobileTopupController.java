package org.example.fastpay.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MobileTopupController {

    // Network Toggles
    @FXML private ToggleButton jazzToggle;
    @FXML private ToggleButton zongToggle;
    @FXML private ToggleButton telenorToggle;
    @FXML private ToggleButton ufoneToggle;

    // Type Toggles
    @FXML private ToggleButton balanceToggle;
    @FXML private ToggleButton prepaidToggle;
    @FXML private ToggleButton postpaidToggle;

    // Forms
    @FXML private VBox balanceFormBox;
    @FXML private VBox packagesFormBox;

    // Inputs
    @FXML private TextField phoneField;
    @FXML private TextField amountField;
    @FXML private ListView<PackageItem> packagesListView;

    // Simple Data Model for Hardcoded Packages
    private static class PackageItem {
        String name; String details; String price;
        PackageItem(String name, String details, String price) { this.name = name; this.details = details; this.price = price; }
    }

    @FXML
    public void initialize() {
        setupNetworkToggles();
        setupTypeToggles();
        setupInputValidations();
        loadDummyPackages();
    }

    private void setupNetworkToggles() {
        ToggleGroup networkGroup = new ToggleGroup();
        jazzToggle.setToggleGroup(networkGroup);
        zongToggle.setToggleGroup(networkGroup);
        telenorToggle.setToggleGroup(networkGroup);
        ufoneToggle.setToggleGroup(networkGroup);
        jazzToggle.setSelected(true); // Default selection
    }

    private void setupTypeToggles() {
        ToggleGroup typeGroup = new ToggleGroup();
        balanceToggle.setToggleGroup(typeGroup);
        prepaidToggle.setToggleGroup(typeGroup);
        postpaidToggle.setToggleGroup(typeGroup);

        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            } else {
                // If Balance or Postpaid is selected, show Amount Form.
                // If Prepaid is selected, show Packages List.
                boolean showPackages = (newVal == prepaidToggle);

                packagesFormBox.setVisible(showPackages);
                packagesFormBox.setManaged(showPackages);

                balanceFormBox.setVisible(!showPackages);
                balanceFormBox.setManaged(!showPackages);
            }
        });
    }

    private void setupInputValidations() {
        // Strict 11-digit phone rule
        phoneField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("^$|^0$|^03\\d{0,9}$") ? change : null));

        // Amount numbers only
        amountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
    }

    private void loadDummyPackages() {
        ObservableList<PackageItem> packages = FXCollections.observableArrayList(
                new PackageItem("Monthly Super Duper", "10GB Data, 1000 All-Net Mins", "Rs. 1,200"),
                new PackageItem("Weekly Mega", "5GB Data, 500 Mins", "Rs. 350"),
                new PackageItem("Daily Social", "1GB WhatsApp/FB", "Rs. 50")
        );

        packagesListView.setItems(packages);

        // Custom UI for the List View to make it look like modern cards
        packagesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PackageItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    HBox root = new HBox(10);
                    root.setStyle("-fx-padding: 10; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

                    VBox texts = new VBox(2);
                    Label nameLbl = new Label(item.name);
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a2130; -fx-font-size: 14px;");
                    Label detailLbl = new Label(item.details);
                    detailLbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
                    texts.getChildren().addAll(nameLbl, detailLbl);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label priceLbl = new Label(item.price);
                    priceLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #4da6ff; -fx-font-size: 14px;");

                    root.getChildren().addAll(texts, spacer, priceLbl);
                    setGraphic(root);
                }
            }
        });
    }

    @FXML
    protected void handleProceed() {
        String phone = phoneField.getText();
        if (phone.length() != 11) {
            new Alert(Alert.AlertType.WARNING, "Please enter a valid 11-digit mobile number.").show();
            return;
        }

        if (balanceFormBox.isVisible()) {
            if (amountField.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please enter an amount.").show();
                return;
            }
            System.out.println("Processing Top-up of Rs. " + amountField.getText() + " for " + phone);
            new Alert(Alert.AlertType.INFORMATION, "Mobile Top-up integration coming soon!").show();

        } else {
            PackageItem selectedPkg = packagesListView.getSelectionModel().getSelectedItem();
            if (selectedPkg == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a package.").show();
                return;
            }
            System.out.println("Subscribing to " + selectedPkg.name + " for " + phone);
            new Alert(Alert.AlertType.INFORMATION, "Package subscription integration coming soon!").show();
        }
    }

    @FXML
    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(org.example.fastpay.Main.class.getResource("views/dashboard-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
            scene.getStylesheets().add(org.example.fastpay.Main.class.getResource("styles/application.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) phoneField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}