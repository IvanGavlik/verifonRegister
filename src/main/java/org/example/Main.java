package org.example;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class Main {
    static byte[] publicEncodedBytes;

    static final int PORT = 5015;

    static Counter counterInstance = new Counter();

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

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println("App start version 1.0.2");
//        register(args);
        testMac(args);
    }

    private static void testMac(String[] args) {
        final String host = args[0];
        final long counter = counterInstance.getNext();
        System.out.println("next counter is " + counter);

        byte[] counterMac;
        try {
            byte[] macBytes = String.valueOf(counter).getBytes("UTF-8");
            SecretKeySpec signingKey = new SecretKeySpec(getMacKey(), "AES");
            Mac mac = null;
            try {
                mac = Mac.getInstance("HmacSHA256");
                mac.init(signingKey);
                counterMac = mac.doFinal(macBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String macMessage = "<TRANSACTION>\n" +
                "  <FUNCTION_TYPE>SECURITY</FUNCTION_TYPE>\n" +
                "  <COMMAND>TEST_MAC</COMMAND>\n" +
                "  <MAC_LABEL>P_VOEGZH</MAC_LABEL>\n" +
                "  <MAC>"+counterMac+"</MAC>\n" +
                "  <COUNTER>"+ counter +"</COUNTER>\n" + //TODO Min value:1 and Max value: 4294967295
                "</TRANSACTION>";

        socketSend(host, PORT, macMessage);
    }

    /**
     response
     Response from device: <RESPONSE>
     Response from device:   <RESPONSE_TEXT>Registered P_VOEGZH</RESPONSE_TEXT>
     Response from device:   <RESULT>OK</RESULT>
     Response from device:   <RESULT_CODE>-1</RESULT_CODE>
     Response from device:   <TERMINATION_STATUS>SUCCESS</TERMINATION_STATUS>
     Response from device:   <MAC_KEY>bu2qchsiidv3zeNjAt9LC8kUd2KpBPgG9iANnBHLpmZmap0gyoJTqezVkatYNwKMj3BPlFuYGYwjyleRaHqe6HEZ2VAuKl/JoTVRcu57Wcq5ouEYi5TsSTmuW/JfLQalmWz0XJdpJgqZ1CCMxREiXc1rRqJMzF/cF3AULaPJ/AqFzL7u489R1+sjJslBbOtvE35ddnnw48Lu/yiV6jEqPzIurfgT2CLtgO3ZmnNISBku9q0msT1HxlHvo3dmdVaGqUEydheaY04500aDbMIJzRfAPd/x+S/itycShl7r9dhKHa+U+KLFw6RQmo694Gb0a4+WsaQ+WRk19b/kHWWDmQ==</MAC_KEY>
     Response from device:   <MAC_LABEL>P_VOEGZH</MAC_LABEL>
     Response from device:   <ENTRY_CODE>8654</ENTRY_CODE>
     Response from device: </RESPONSE>
     */
    private static byte[] getMacKey() {
        return Base64.getDecoder()
                .decode("bu2qchsiidv3zeNjAt9LC8kUd2KpBPgG9iANnBHLpmZmap0gyoJTqezVkatYNwKMj3BPlFuYGYwjyleRaHqe6HEZ2VAuKl/JoTVRcu57Wcq5ouEYi5TsSTmuW/JfLQalmWz0XJdpJgqZ1CCMxREiXc1rRqJMzF/cF3AULaPJ/AqFzL7u489R1+sjJslBbOtvE35ddnnw48Lu/yiV6jEqPzIurfgT2CLtgO3ZmnNISBku9q0msT1HxlHvo3dmdVaGqUEydheaY04500aDbMIJzRfAPd/x+S/itycShl7r9dhKHa+U+KLFw6RQmo694Gb0a4+WsaQ+WRk19b/kHWWDmQ==".getBytes());
    }

    private static void register(String[] args) {
        final String host = args[0];

        Random generator = new Random();
        String entryCode = String.valueOf(generator.nextInt(9999));

        System.out.println("Entry code " + entryCode);
        String publicEncodedString = Base64.getEncoder().encodeToString(publicEncodedBytes);

        String registrationMessage = "<TRANSACTION>\n" +
                "  <FUNCTION_TYPE>SECURITY</FUNCTION_TYPE>\n" +
                "  <COMMAND>REGISTER</COMMAND>\n" +
                "  <ENTRY_CODE>"+ entryCode +"</ENTRY_CODE>\n" +
                "  <KEY>"+publicEncodedString+"</KEY>\n" +
                "  <REG_VER>1</REG_VER>\n" +
                "</TRANSACTION>";

        socketSend(host, PORT, registrationMessage);
    }

    private static void socketSend(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send registration message
            out.println(message);

            // Receive and process response
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Response from device: " + response);
                // Process the response as needed
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
