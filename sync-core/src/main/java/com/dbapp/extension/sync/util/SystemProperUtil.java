package com.dbapp.extension.sync.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by darrendu on 17/3/4.
 */
public class SystemProperUtil {
    private static String file_separator = System.getProperty("file.separator");

    private SystemProperUtil() {
    }

    public static File getResourcesAsFile(String fileName) throws IOException {
        return new ClassPathResource(fileName).getFile();
    }

    public static InputStream getResourcesAsInputStream(String fileName) throws IOException {
        return new ClassPathResource(fileName).getInputStream();
    }

    public static ClassPathResource getGlobalPropertiesPathResource() {
        String activeProfiles = System.getProperty("spring.profiles.active");
        String infix = StringUtils.isBlank(activeProfiles) ? "" : "-" + activeProfiles;
        return new ClassPathResource("global" + infix + ".properties");
    }

    public static String getFileSeparator() {
        return file_separator;
    }


}
