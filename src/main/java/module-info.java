module com.encrypted_chat {
    requires transitive javafx.controls;
    requires javafx.fxml;

    opens com.encrypted_chat to javafx.fxml;
    exports com.encrypted_chat;
}
