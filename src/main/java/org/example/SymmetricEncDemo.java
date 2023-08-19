package org.example;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;

public class SymmetricEncDemo {

    public static void main(String[] args) throws Exception {
        // specify key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(192);
        Key key = keyGenerator.generateKey();
        System.out.println("key " + Arrays.toString(key.getEncoded()));

        byte[] msg = "MSG".getBytes();

        // encrypt
        Cipher chiper =  Cipher.getInstance("AES/ECB/PKCS5Padding");
        chiper.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = chiper.doFinal(msg);
        System.out.println(Arrays.toString(encrypted));

        // decrypt
        chiper.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = chiper.doFinal(encrypted);
        System.out.println(Arrays.toString(decrypted));

        System.out.println("------");
        tryWithBase64Encoding();
    }

    private static void tryWithBase64Encoding() throws Exception {
        // specify key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(192);
        Key key = keyGenerator.generateKey();
        System.out.println("key " + Arrays.toString(key.getEncoded()));

        byte[] msg = "MSG".getBytes();

        // encrypt and encode
        Cipher chiper =  Cipher.getInstance("AES/ECB/PKCS5Padding");
        chiper.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = chiper.doFinal(msg);
        byte[] encodedEncrypted = Base64.getEncoder().encode(encrypted);
        System.out.println(Arrays.toString(encodedEncrypted));

        // decode and decrypt
        chiper.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encodedEncrypted);
        byte[] decrypted = chiper.doFinal(decoded);
        System.out.println(Arrays.toString(decrypted));
    }
}
