package de.adorsys.android.smsparsertest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TestKey {

    public static String getPublicKey(String input) {
        MessageDigest messageDigest;
        String encdeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(input.getBytes("UTF-8"));
            encdeStr = byte2Hex(hash);
        }catch (Exception e){
            System.out.println("hash錯誤");
        }
        return encdeStr;
    }
    private static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i=0;i<bytes.length;i++  ){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){//1得到一位的進行補0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
