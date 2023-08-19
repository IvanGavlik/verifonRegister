package org.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Supplier;

public class App {
    public static void main(String[] args) {
        App app = new App(args[0]);
        app.unregister();
        RegisterResponse registerResponse = app.register();
        TestMacResponse testMacResponse = app.testMac(registerResponse);
        System.out.println(testMacResponse);
    }
    private static final int PORT = 5015;
    private final String HOST;
    private Counter counterInstance = new Counter();

    private App(String host) {
        this.HOST = host;
    }

    public void unregister() {
        XMLRepresentation unregisterRequest = new UnregisterRequest();
        socketSend(HOST, PORT, unregisterRequest.get());
    }
    private RegisterResponse register() {
        int entryCode = new Random().nextInt((9999 - 1001) + 1) + 1001;
        byte[] key = Security.INSTANCE.getPublicKeyEncoded();
        XMLRepresentation registerRequest = new RegisterRequest(entryCode, key);
        String response = socketSend(HOST, PORT, registerRequest.get());
        return new RegisterResponse(response);
    }

    private TestMacResponse testMac(RegisterResponse registerResponse) {
        try {
            final long counterNext = counterInstance.getNext();

            byte[] aes128Key = Security.INSTANCE
                    .decodeAndDecrypt(registerResponse.getMacKey().getBytes());
            byte[] mac = Security.INSTANCE
                    .aes128AndEncode(aes128Key, String.valueOf(counterNext).getBytes());

            XMLRepresentation testMac = new TestMacRequest(registerResponse.getMacLabel(), mac, counterNext);
            String response = socketSend(HOST, PORT, testMac.get());
            return new TestMacResponse(response);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
    private static String socketSend(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(new InputStreamReader(socket.getInputStream()))) {

            // Send registration message
            out.println(message);

            // Receive and process response
            StringBuilder responseBuilder = new StringBuilder();
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                System.out.println("Response from device: " + l);
                // Process the response as needed
                responseBuilder.append(l);
                if (l.contains("</RESPONSE>")) {
                    socket.close();
                }
                System.out.println("Response processing");
            }
            System.out.println("Response end");
            return responseBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

interface XMLRepresentation extends Supplier<String> { }
class UnregisterRequest implements XMLRepresentation {
    private final String type = "SECURITY";
    private final String command = "UNREGISTERALL";

    @Override
    public String get() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<TRANSACTION>")
                .append("<FUNCTION_TYPE>").append(this.type).append("</FUNCTION_TYPE>")
                .append("<COMMAND>").append(command).append("</COMMAND>")
                .append("</TRANSACTION>");
        return stringBuilder.toString();
    }
}
class RegisterRequest implements XMLRepresentation {

    private final String type = "SECURITY";
    private final String command = "REGISTER";

    private final int entryCode;

    private final byte[] key;

    private final int regVersion = 1;

    public RegisterRequest(final int entryCode, final byte[] key) {
        this.entryCode = entryCode;
        this.key = key;
    }

    @Override
    public String get() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<TRANSACTION>")
                .append("<FUNCTION_TYPE>").append(type).append("</FUNCTION_TYPE>")
                .append("<COMMAND>").append(command).append("</COMMAND>")
                .append("<ENTRY_CODE>").append(entryCode).append("</ENTRY_CODE>")
                .append("<KEY>").append(key).append("</KEY>")
                .append("<REG_VER>").append(regVersion).append("</REG_VER>")
                .append("</TRANSACTION>");
        return stringBuilder.toString();
    }
}
class TestMacRequest implements XMLRepresentation {
    private final String type = "SECURITY";
    private final String command = "TEST_MAC";
    private final String macLabel;
    private final byte[] mac;
    private final long counter;

    public TestMacRequest(final String macLabel, final byte[] mac, final long counter) {
        this.macLabel = macLabel;
        this.mac = mac;
        this.counter = counter;
    }
    @Override
    public String get() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<TRANSACTION>")
                .append("<FUNCTION_TYPE>").append(type).append("</FUNCTION_TYPE>")
                .append("<COMMAND>").append(command).append("</COMMAND>")
                .append("<MAC_LABEL>").append(macLabel).append("</MAC_LABEL>")
                .append("<MAC>").append(mac).append("</MAC>")
                .append("<COUNTER>").append(counter).append("</COUNTER>")
                .append("</TRANSACTION>");
        return stringBuilder.toString();
    }
}
abstract class XMLResponse {
    protected XMLResponse(final String response) {
        this.constructObjectFromResponse(response);
    }
    abstract void constructObjectFromResponse(final String response);

    protected String getValueFromXMLElement(final String element, final String xml) {
        int start = xml.indexOf("<"+element+">");
        int end = xml.indexOf("</"+element+">");
        return xml.substring(start, end).replace("<"+element+">", "").trim();
    }
}
class RegisterResponse extends XMLResponse {
    private String responseText;
    private String result;
    private String resultCode;
    private String terminationStatus;
    private String macKey;
    private String macLabel;
    private String entryCode;

    protected RegisterResponse(final String response) {
        super(response);
    }

    @Override
    void constructObjectFromResponse(final String response) {
        this.responseText = getValueFromXMLElement("RESPONSE_TEXT", response);
        this.result = getValueFromXMLElement("RESULT", response);
        this.resultCode = getValueFromXMLElement("RESULT_CODE", response);
        this.terminationStatus = getValueFromXMLElement("TERMINATION_STATUS", response);
        this.macKey = getValueFromXMLElement("MAC_KEY", response);
        this.macLabel = getValueFromXMLElement("MAC_LABEL", response);
        this.entryCode = getValueFromXMLElement("ENTRY_CODE", response);
    }

    public String getResponseText() {
        return responseText;
    }

    public String getResult() {
        return result;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getTerminationStatus() {
        return terminationStatus;
    }

    public String getMacKey() {
        return macKey;
    }

    public String getMacLabel() {
        return macLabel;
    }

    public String getEntryCode() {
        return entryCode;
    }

    @Override
    public String toString() {
        return "RegisterResponse{" +
                "responseText='" + responseText + '\'' +
                ", result='" + result + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", terminationStatus='" + terminationStatus + '\'' +
                ", macKey='" + macKey + '\'' +
                ", macLabel='" + macLabel + '\'' +
                ", entryCode='" + entryCode + '\'' +
                '}';
    }
}
class TestMacResponse extends XMLResponse {

    private String responseText;
    private String result;
    private String resultCode;
    private String terminationStatus;
    private String counter;
    public TestMacResponse(final String response) {
        super(response);
    }

    @Override
    void constructObjectFromResponse(String response) {
        this.responseText = getValueFromXMLElement("RESPONSE_TEXT", response);
        this.result = getValueFromXMLElement("RESULT", response);
        this.resultCode = getValueFromXMLElement("RESULT_CODE", response);
        this.terminationStatus = getValueFromXMLElement("TERMINATION_STATUS", response);
        this.counter = getValueFromXMLElement("COUNTER", response);
    }

    public String getResponseText() {
        return responseText;
    }

    public String getResult() {
        return result;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getTerminationStatus() {
        return terminationStatus;
    }

    public String getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return "TestMacResponse{" +
                "responseText='" + responseText + '\'' +
                ", result='" + result + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", terminationStatus='" + terminationStatus + '\'' +
                ", counter='" + counter + '\'' +
                '}';
    }
}
