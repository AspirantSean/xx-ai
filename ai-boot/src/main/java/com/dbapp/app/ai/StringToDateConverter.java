package com.dbapp.app.ai;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringToDateConverter implements Converter<String, Date> {
    private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String SHORTDATEFORMAT = "yyyy-MM-dd";
    private static final String MINUTEDATEFORMAT = "yyyy-MM-dd HH:mm";

    Logger logger = LoggerFactory.getLogger(StringToDateConverter.class);

    @Override
    public Date convert(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        source = source.trim();
        try {
            if (source.contains("-")) {
                SimpleDateFormat formatter;
                if (source.contains(":")) {
                    formatter = new SimpleDateFormat(DATEFORMAT);
                } else {
                    if (source.split(":").length == 2) {
                        formatter = new SimpleDateFormat(MINUTEDATEFORMAT);
                    } else {
                        formatter = new SimpleDateFormat(SHORTDATEFORMAT);
                    }
                }
                Date dtDate = formatter.parse(source);
                return dtDate;
            } else if (source.matches("^\\d+$")) {
                Long lDate = new Long(source);
                return new Date(lDate);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        logger.info("Error Source: {}", source);
        throw new RuntimeException(String.format("parser %s to Date fail", source));
    }
}
