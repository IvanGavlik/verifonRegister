package org.example;

import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        String host = args[0];
        int port = 5015;

        Random generator = new Random();
        String entryCode = String.valueOf(generator.nextInt(9999));

        System.out.println("Entry code " + entryCode);


        String registrationMessage = "<TRANSACTION>\n" +
                "  <FUNCTION_TYPE>SECURITY</FUNCTION_TYPE>\n" +
                "  <COMMAND>REGISTER</COMMAND>\n" +
                "  <ENTRY_CODE>"+ entryCode +"</ENTRY_CODE>\n" +
                "  <KEY>Your_Public_Key_Here</KEY>\n" +
                "  <REG_VER>2</REG_VER>\n" +
                "</TRANSACTION>";

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send registration message
            out.println(registrationMessage);

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
