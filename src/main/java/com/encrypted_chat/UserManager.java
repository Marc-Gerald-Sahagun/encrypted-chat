package com.encrypted_chat;

// UserManager.java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private Map<String, String> users = new HashMap<>();
    
    public UserManager() {
        try {
            File f = new File("users.dat");
            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                users = (Map<String, String>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {}
    }
    
    public boolean register(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, hashPassword(password));
        saveUsers();
        return true;
    }
    
    public boolean authenticate(String username, String password) {
        String stored = users.get(username);
        return stored != null && stored.equals(hashPassword(password));
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void saveUsers() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"));
            oos.writeObject(users);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}