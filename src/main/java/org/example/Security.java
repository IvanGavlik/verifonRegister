package org.example;

import javax.crypto.Cipher;
import javax.crypto.Mac;
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
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, this.keyPair.getPrivate());
            byte[] decoded = Base64.getDecoder().decode(value);
            return cipher.doFinal(decoded);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String aes128AndEncode(byte[] aes128Key, byte[] value) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(aes128Key, "AES");
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(signingKey);
            byte[] mac = hmac.doFinal(value);
            return Base64.getEncoder().encodeToString(mac);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

}
