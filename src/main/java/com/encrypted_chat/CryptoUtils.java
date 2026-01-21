package com.encrypted_chat;

// CryptoUtils.java
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    
    // Diffie-Hellman implementation
    public static class DiffieHellman {
        private static final BigInteger P = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
            "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
            "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
            "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
            "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
            "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
            "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
            "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
            "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
            "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
            "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16
        );
        private static final BigInteger G = BigInteger.valueOf(2);
        
        private BigInteger privateKey;
        private BigInteger publicKey;
        
        public DiffieHellman() {
            SecureRandom random = new SecureRandom();
            privateKey = new BigInteger(2048, random);
            publicKey = G.modPow(privateKey, P);
        }
        
        public BigInteger getPublicKey() {
            return publicKey;
        }
        
        public byte[] computeSharedSecret(BigInteger otherPublicKey) {
            BigInteger shared = otherPublicKey.modPow(privateKey, P);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                return md.digest(shared.toByteArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    // AES encryption
    public static class AESEncryption {
        private SecretKeySpec secretKey;
        
        public void setKey(byte[] keyBytes) {
            secretKey = new SecretKeySpec(keyBytes, "AES");
        }
        
        public String encrypt(String plainText) {
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public String decrypt(String encryptedText) {
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}