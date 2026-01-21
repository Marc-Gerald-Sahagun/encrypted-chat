package com.encrypted_chat;

// ChatClient.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;

public class ChatClient implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private CryptoUtils.DiffieHellman dh;
    private CryptoUtils.AESEncryption encryption;
    private volatile boolean running = true;
    
    public interface MessageCallback { void onMessage(String message); }
    public interface StatusCallback { void onStatus(String status); }
    
    private MessageCallback messageCallback;
    private StatusCallback statusCallback;
    
    public ChatClient(String host, int port, String username) throws IOException {
        this.socket = new Socket(host, port);
        this.dh = new CryptoUtils.DiffieHellman();
        this.encryption = new CryptoUtils.AESEncryption();
    }
    
    public void setMessageCallback(MessageCallback callback) { this.messageCallback = callback; }
    public void setStatusCallback(StatusCallback callback) { this.statusCallback = callback; }
    
    @Override
    public void run() {
        try {
            updateStatus("Connected to server. Performing key exchange...");
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Key exchange
            BigInteger serverPublicKey = new BigInteger(in.readLine());
            out.println(dh.getPublicKey().toString());
            encryption.setKey(dh.computeSharedSecret(serverPublicKey));
            
            updateStatus("Secure connection established!");
            
            String encryptedMsg;
            while (running && (encryptedMsg = in.readLine()) != null) {
                if (messageCallback != null) messageCallback.onMessage(encryption.decrypt(encryptedMsg));
            }
        } catch (Exception e) {
            updateStatus("Error: " + e.getMessage());
        }
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(encryption.encrypt(message));
            if (messageCallback != null) messageCallback.onMessage(message);
        }
    }
    
    private void updateStatus(String status) {
        if (statusCallback != null) statusCallback.onStatus(status);
    }
    
    public void stop() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}