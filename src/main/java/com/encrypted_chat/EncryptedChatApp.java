package com.encrypted_chat;


// EncryptedChatApp.java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EncryptedChatApp extends Application {
    private Stage primaryStage;
    private UserManager userManager;
    private ChatServer server;
    private ChatClient client;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        userManager = new UserManager();
        showLoginScreen();
    }
    
    private void showLoginScreen() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        
        Label title = new Label("Encrypted Chat Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);
        
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        
        loginBtn.setOnAction(e -> {
            if (userManager.authenticate(usernameField.getText(), passwordField.getText())) {
                showRoleSelection(usernameField.getText());
            } else {
                statusLabel.setText("Invalid credentials!");
            }
        });
        
        registerBtn.setOnAction(e -> {
            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                statusLabel.setText("Username and password required!");
            } else if (userManager.register(usernameField.getText(), passwordField.getText())) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("Registration successful! Please login.");
            } else {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Username already exists!");
            }
        });
        
        HBox buttonBox = new HBox(10, loginBtn, registerBtn);
        buttonBox.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(title, usernameField, passwordField, buttonBox, statusLabel);
        
        primaryStage.setScene(new Scene(layout, 400, 300));
        primaryStage.setTitle("Encrypted Chat - Login");
        primaryStage.show();
    }
    
    private void showRoleSelection(String username) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        
        Label title = new Label("Welcome, " + username + "!");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TextField portField = new TextField("8888");
        portField.setPromptText("Port");
        portField.setMaxWidth(200);
        
        TextField hostField = new TextField("localhost");
        hostField.setPromptText("Host");
        hostField.setMaxWidth(200);
        
        Button serverBtn = new Button("Start Server");
        Button clientBtn = new Button("Connect as Client");
        Label statusLabel = new Label();
        
        serverBtn.setOnAction(e -> {
            try {
                server = new ChatServer(Integer.parseInt(portField.getText()), username);
                new Thread(server).start();
                showChatScreen(username, true);
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        
        clientBtn.setOnAction(e -> {
            try {
                client = new ChatClient(hostField.getText(), Integer.parseInt(portField.getText()), username);
                new Thread(client).start();
                showChatScreen(username, false);
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        
        layout.getChildren().addAll(title, new Label("Port:"), portField, 
            new Label("Host (for client):"), hostField, serverBtn, clientBtn, statusLabel);
        
        primaryStage.setScene(new Scene(layout, 400, 350));
        primaryStage.setTitle("Encrypted Chat - Role Selection");
    }
    
    private void showChatScreen(String username, boolean isServer) {
        BorderPane layout = new BorderPane();
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        
        TextField messageField = new TextField();
        messageField.setPromptText("Type a message...");
        Button sendBtn = new Button("Send");
        
        HBox inputBox = new HBox(10, messageField, sendBtn);
        inputBox.setPadding(new Insets(10));
        HBox.setHgrow(messageField, Priority.ALWAYS);
        
        Label statusLabel = new Label("Status: Establishing connection...");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #f0f0f0;");
        
        layout.setCenter(chatArea);
        layout.setBottom(new VBox(statusLabel, inputBox));
        
        if (isServer && server != null) {
            server.setMessageCallback(msg -> Platform.runLater(() -> chatArea.appendText(msg + "\n")));
            server.setStatusCallback(status -> Platform.runLater(() -> statusLabel.setText("Status: " + status)));
        } else if (!isServer && client != null) {
            client.setMessageCallback(msg -> Platform.runLater(() -> chatArea.appendText(msg + "\n")));
            client.setStatusCallback(status -> Platform.runLater(() -> statusLabel.setText("Status: " + status)));
        }
        
        Runnable sendMessage = () -> {
            String msg = messageField.getText().trim();
            if (!msg.isEmpty()) {
                if (isServer && server != null) server.sendMessage(username + ": " + msg);
                else if (!isServer && client != null) client.sendMessage(username + ": " + msg);
                messageField.clear();
            }
        };
        
        sendBtn.setOnAction(e -> sendMessage.run());
        messageField.setOnAction(e -> sendMessage.run());
        
        primaryStage.setScene(new Scene(layout, 600, 400));
        primaryStage.setTitle("Encrypted Chat - " + username);
        primaryStage.setOnCloseRequest(e -> {
            if (server != null) server.stop();
            if (client != null) client.stop();
        });
    }
}