module org.example.fastpay {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.fastpay to javafx.fxml;
    exports org.example.fastpay;
}