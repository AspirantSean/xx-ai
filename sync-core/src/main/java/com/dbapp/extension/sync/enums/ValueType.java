package com.dbapp.extension.sync.enums;

public enum ValueType {
    Function("%s %s %s"), String("%s %s '%s'"), Int("%s %s %s");

    private final String format;

    ValueType(String format) {
        this.format = format;
    }

    public String convertToCondition(String field, String operator, String value) {
        return java.lang.String.format(format, field, operator, value);
    }
}
