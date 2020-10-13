package com.dbapp.utils;

import org.apache.commons.lang.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liguangting on 15-8-13.
 */
public class Md5Util {

    private static char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String getHash(String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(plaintext.getBytes());
            return byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String byteToStr(byte[] byteArray) {
        String rst = "";
        for (int i = 0; i < byteArray.length; i++) {
            rst += byteToHex(byteArray[i]);
        }
        return rst;
    }

    private static String byteToHex(byte b) {
        char[] tempArr = new char[2];
        tempArr[0] = digit[(b >>> 4) & 0X0F];
        tempArr[1] = digit[b & 0X0F];
        String s = new String(tempArr);
        return s;
    }

    /***
     * MD5加码 生成32位md5码
     */
    public static String md5(String inStr){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    /***
     * MD5加码 生成16位md5码
     */
    public static String md5To16Bit(String inStr){
        return md5(inStr).substring(8, 24);
    }


    public static String md5ToEnBit(String inStr){
        StringBuilder builder = new StringBuilder();
        String md5 = md5(inStr);
        for (char c : md5.toCharArray()) {
            if(StringUtils.isNumeric(String.valueOf(c))){
                builder.append(transToEn(c));
            }else {
                builder.append(c);
            }
        }
        return builder.toString().substring(8, 20);
    }


    private static char transToEn(char i){
        char key;
        switch (i) {
            case '0':
                key = 'g';
                break;
            case '1':
                key = 'h';
                break;
            case '2':
                key = 'i';
                break;
            case '3':
                key = 'j';
                break;
            case '4':
                key = 'k';
                break;
            case '5':
                key = 'l';
                break;
            case '6':
                key = 'm';
                break;
            case '7':
                key = 'n';
                break;
            case '8':
                key = 'o';
                break;
            case '9':
                key = 'p';
                break;
            default:
                key = 'x';
        }
        return key;
    }

}
