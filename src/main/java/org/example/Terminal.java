package org.example;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Base64;

public class Terminal {
    static byte[] publicEncodedBytes;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keypair = keyGen.genKeyPair();
            publicEncodedBytes = keypair.getPublic().getEncoded();
        } catch (Exception exception) {
            System.out.println("Start failed: " + exception);
        }

    }
    public String publicEncodedString = Base64.getEncoder().encodeToString(publicEncodedBytes);
    public String entryCode;
    public String keyRequest;
    public String macKey;

    public long counter;
    public byte[] macCounter;
    public String macLabel;
    public String lastResponse;

    public void setMacKey() {
       int start = lastResponse.indexOf("<MAC_KEY>");
       int end = lastResponse.indexOf("</MAC_KEY>");
       macKey = lastResponse.substring(start, end).replace("<MAC_KEY>", "").trim();
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
