module org.example.fastpay {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.net.http;
    requires com.google.zxing;
    requires java.desktop;
    requires com.google.zxing.javase;
    requires javafx.swing;

    opens org.example.fastpay to javafx.fxml;
    opens org.example.fastpay.controllers to javafx.fxml;
    exports org.example.fastpay;
}