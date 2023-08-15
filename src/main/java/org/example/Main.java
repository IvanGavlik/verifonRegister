package org.example;

import java.io.*;
import java.net.*;
public class Main {
    public static void main(String[] args) {
        String host = "device_ip_address";
        int port = 5015;

        String registrationMessage = "<TRANSACTION>\n" +
                "  <FUNCTION_TYPE>SECURITY</FUNCTION_TYPE>\n" +
                "  <COMMAND>REGISTER</COMMAND>\n" +
                "  <ENTRY_CODE>1</ENTRY_CODE>\n" +
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
