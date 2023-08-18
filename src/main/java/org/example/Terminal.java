package org.example;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Mac;

public class Terminal {
    static byte[] publicEncodedBytes;
    static PrivateKey privateKey;
    static PublicKey publicKey;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keypair = keyGen.genKeyPair();
            publicEncodedBytes = keypair.getPublic().getEncoded();
            publicKey = keypair.getPublic();
            privateKey = keypair.getPrivate();
        } catch (Exception exception) {
            System.out.println("Start failed: " + exception);
        }

    }
    public String publicEncodedString = Base64.getEncoder().encodeToString(publicEncodedBytes);
    public String entryCode;
    public String keyRequest;
    public byte[] macKey;

    public long counter;
    public byte[] macCounter;
    public String macLabel;
    public String lastResponse;

    public void setMacKey() throws Exception {
       int start = lastResponse.indexOf("<MAC_KEY>");
       int end = lastResponse.indexOf("</MAC_KEY>");
       final byte[] macKeyBase64Decoded = Base64.getDecoder().decode(
                lastResponse.substring(start, end).replace("<MAC_KEY>", "").trim());


        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        macKey = cipher.doFinal(macKeyBase64Decoded);

        // test mac command
        // convert counter to bytes
        byte[] macBytes = String.valueOf(counter).getBytes("UTF-8"); // import AES 128 MAC_KEY and create HMAC object with SHA-256
        SecretKeySpec signingKey = new SecretKeySpec(macKey, "AES");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] counterMac = mac.doFinal(macBytes);
        this.macCounter = Base64.getEncoder().encode(counterMac);
    }

    public void setMacLabel() {
        int start = lastResponse.indexOf("<MAC_LABEL>");
        int end = lastResponse.indexOf("</MAC_LABEL>");
        macLabel = lastResponse.substring(start, end).replace("<MAC_LABEL>", "").trim();
    }

    @Override
    public String toString() {
        return "Terminal{" +
                "publicEncodedString='" + publicEncodedString + '\'' +
                ", entryCode='" + entryCode + '\'' +
                ", keyRequest='" + keyRequest + '\'' +
                ", macKey='" + macKey + '\'' +
                ", counter=" + counter +
                ", macCounter=" + Arrays.toString(macCounter) +
                ", macLabel='" + macLabel + '\'' +
                ", lastResponse='" + lastResponse + '\'' +
                '}';
    }
}
