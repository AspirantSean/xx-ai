package com.dbapp.extension.ai.config.source;

import lombok.Getter;

@Getter
public enum DataSourceType {
    POSTGRESQL("postgresql", "classpath*:mappers/*Mapper.xml"),
    GAUSS("opengauss", "classpath*:mappers/gauss/*Mapper-gauss.xml"),
    MYSQL("mysql", "classpath*:mappers/mysql/*Mapper-mysql.xml");
    private String value;
    private String locations;

    DataSourceType(String value, String locations) {
        this.value = value;
        this.locations = locations;
    }

    public static DataSourceType valueOfName(String name) {
        for (DataSourceType value : DataSourceType.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
