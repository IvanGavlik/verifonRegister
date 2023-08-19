package org.example;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class HashingDemo {
    public static void main(String[] args) throws Exception{
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] msg = "MSG".getBytes();
        byte[] digest = messageDigest.digest(msg);
        System.out.println(Arrays.toString(msg));
        System.out.println("---------------");
        byte[] bytes = "hello world".getBytes();
        System.out.println(Base64.getEncoder().encodeToString(bytes));
    }
}
