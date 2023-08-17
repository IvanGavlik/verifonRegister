package org.example;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static final int PORT = 5015;
    static Counter counterInstance = new Counter();
    static Terminal terminal = new Terminal();

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println("App start version 1.0.7");
        meni();

        int i = Integer.MAX_VALUE;
        while ( i != 0) {
            Scanner scanner = new Scanner(System.in);
            String s = scanner.nextLine();
            i = Integer.valueOf(s);
            switch (i) {
                case 1:
                    register(args);
                    break;
                case 2:
                    testMac(args);
                    break;
                case 3:
                    unregister(args);
                    break;
                default:
                    break;
            }
            meni();
        }
    }

    private static void meni() {
        System.out.println("Enter:");
        System.out.println("    1 to register");
        System.out.println("    2 to test mac");
        System.out.println("    3 to unregiser");
        System.out.println("    0 to close program");
    }

    private static void unregister(String[] args) {
        String unregisterMessage = "<TRANSACTION>\n" +
                "  <FUNCTION_TYPE>SECURITY</FUNCTION_TYPE>\n" +
                "  <COMMAND>UNREGISTERALL</COMMAND>\n" +
                "</TRANSACTION>";
        socketSend(args[0], PORT, unregisterMessage);
        debug();
    }

    private static void testMac(String[] args) {
        final String host = args[0];
        terminal.counter = counterInstance.getNext();

        try {
            byte[] macBytes = String.valueOf(terminal.counter).getBytes("UTF-8");
            SecretKeySpec signingKey = new SecretKeySpec(terminal.macKey, "AES");
            Mac mac = null;
            try {
                mac = Mac.getInstance("HmacSHA256");
                mac.init(signingKey);
                terminal.macCounter = mac.doFinal(macBytes);
            } catch (NoSuchAlgorithmException e) {
                debug();
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                debug();
                throw new RuntimeException(e);
            }

        } catch (UnsupportedEncodingException e) {
            debug();
            throw new RuntimeException(e);
        }

        String macMessage = "<TRANSACTION>\n" +
                "  <FUNCTION_TYPE>SECURITY</FUNCTION_TYPE>\n" +
                "  <COMMAND>TEST_MAC</COMMAND>\n" +
                "  <MAC_LABEL>"+terminal.macLabel+"</MAC_LABEL>\n" +
                "  <MAC>"+terminal.macCounter+"</MAC>\n" +
                "  <COUNTER>"+ terminal.counter +"</COUNTER>\n" + //TODO Min value:1 and Max value: 4294967295
                "</TRANSACTION>";

        socketSend(host, PORT, macMessage);

        debug();
    }

    private static void register(String[] args) {
       final String host = args[0];

       Random generator = new Random();
        terminal.entryCode = String.valueOf(generator.nextInt(9999));

        System.out.println("Entry code " + terminal.entryCode);

        String registrationMessage = "<TRANSACTION>\n" +
                "  <FUNCTION_TYPE>SECURITY</FUNCTION_TYPE>\n" +
                "  <COMMAND>REGISTER</COMMAND>\n" +
                "  <ENTRY_CODE>"+ terminal.entryCode +"</ENTRY_CODE>\n" +
                "  <KEY>"+terminal.publicEncodedString+"</KEY>\n" +
                "  <REG_VER>1</REG_VER>\n" +
                "</TRANSACTION>";

        socketSend(host, PORT, registrationMessage);

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
        terminal.setMacKey();
        terminal.setMacLabel();

        debug();
    }

    private static void socketSend(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send registration message
            out.println(message);

            // Receive and process response
            StringBuilder responseBuilder = new StringBuilder();
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Response from device: " + response);
                // Process the response as needed
                responseBuilder.append(response);
            }
            terminal.lastResponse = responseBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            debug();
        }
    }

    private static void debug() {
        System.out.println("_________DEBUG INFO START_____________");
        System.out.println(terminal);
        System.out.println("_________DEBUG INFO END_____________");
    }
}
