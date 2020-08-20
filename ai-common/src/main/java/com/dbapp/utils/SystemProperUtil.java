package com.dbapp.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by darrendu on 17/3/4.
 */
public class SystemProperUtil {
    private static String file_separator = System.getProperty("file.separator");
    private static String profileHome = System.getProperty("mirror.profile.home", System.getenv("MIRROR_HOME"));


    static {
        if (StringUtils.isEmpty(profileHome)) {
            profileHome = System.getProperty("user.dir");
        }
    }

    private SystemProperUtil() {
    }

    public static String getProfileHome() {
        return profileHome;
    }

    public static String getConfPath() {
        return profileHome + file_separator + "conf";
    }

    public static String getSysPath() {
        return profileHome + file_separator + "system";
    }

    public static String getLibPath() {
        return profileHome + file_separator + "lib";
    }

    public static String getFileSeparator() {
        return file_separator;
    }


}
