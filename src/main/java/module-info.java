module org.example.fastpay {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.net.http;


    opens org.example.fastpay to javafx.fxml;
    exports org.example.fastpay;
}