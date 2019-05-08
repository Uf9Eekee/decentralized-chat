//This currently does SHA256 hashes, and one day might do actual encryption as well.

import java.security.*;
import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;

public class CryptoManager {
    MessageDigest messageDigest;
    Cipher rsaCipher;
    Cipher aesCipher;
    SecureRandom secureRandom;
    private byte[] identifier = new byte[32];


    public CryptoManager() {
        secureRandom = new SecureRandom();
        secureRandom.nextBytes(identifier);
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getSHA256AsByteArray(byte[] bytes) {
        return messageDigest.digest(bytes);
    }

    public String getSHA256AsHexString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(messageDigest.digest(bytes));
    }
}