package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class Security {
    public static Security INSTANCE = new Security();
    private KeyPair keyPair;
    private Security()  {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.genKeyPair();
        } catch (Exception exception) {
            throw new RuntimeException();
        }
    }
    public String getPublicKeyEncoded() {
        return Base64.getEncoder().encodeToString(this.keyPair.getPublic().getEncoded());
    }

    public byte[] decodeAndDecrypt(byte[] value) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, this.keyPair.getPrivate());
            byte[] decoded = Base64.getDecoder().decode(value);
            return cipher.doFinal(decoded);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public byte[] aes128AndEncode(byte[] aes128Key, byte[] value) {
        try {
            SecretKey macKey = new SecretKeySpec(aes128Key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, macKey);
            byte[] mac = cipher.doFinal(String.valueOf(value).getBytes());
            return mac;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

}
