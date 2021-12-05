package au.com.bellsolutions;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

public class Util {

    public static byte[] rsaDecrypt(PublicKey pk, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, pk);
        return cipher.doFinal(data);
    }

    public static PublicKey getPublicKey(String modulus, String exponent) throws Exception {
        BigInteger m = new BigInteger(modulus,16);
        BigInteger e = new BigInteger(exponent,16);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(m,e);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    public static String toSHA1(byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return byteArrayToHexString(md.digest(data));
    }

    public static String readFileAsString(String fileName) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] b) {
        return byteArrayToHexString(b,0, b.length);
    }

    public static String byteArrayToHexString(byte data) {
        byte[] b = new byte[1];
        b[0] = data;
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result.toUpperCase();
    }

    public static String byteArrayToHexString(byte[] b, int start, int len) {
        String result = "";
        for (int i=start; i < b.length && i < start + len; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result.toUpperCase();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

