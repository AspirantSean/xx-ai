package com.dbapp.extension.ai.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by darrendu on 17/3/4.
 */
public class SystemProperUtil {
    private static String file_separator = System.getProperty("file.separator");
    private static String profileResource = System.getProperty("mirror.profile.resources");


    static {
        if (StringUtils.isEmpty(profileResource)) {
            profileResource = System.getenv("APP_HOME") + file_separator + "conf";
        }
    }

    private SystemProperUtil() {
    }

    public static String getResourcesPath() {
        return profileResource;
    }

    public static String getFileSeparator() {
        return file_separator;
    }


}
