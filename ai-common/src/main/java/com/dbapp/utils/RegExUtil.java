package com.dbapp.utils;

import java.util.regex.Pattern;

public class RegExUtil {

    public static boolean verifyDomain(String domain) {
        String regex = "^(?=^.{3,255}$)([a-zA-Z0-9][-_a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-_a-zA-Z0-9]{0,62})+(?::\\d{1,5})?|(?:(?:\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5]):(\\d{1,5}))$";
        return Pattern.matches(regex, domain);
    }

    public static boolean verifyEmail(String email) {
        String regex = "^([a-z0-9A-Z]+[\\-|.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return Pattern.matches(regex, email);
    }

    public static boolean verifyFileHash(String fileHash) {
        String regex = "^[a-z0-9A-Z]{1,255}$";
        return Pattern.matches(regex, fileHash);
    }

}
