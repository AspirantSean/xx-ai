package com.dbapp.extension.ai.config.source;

import org.springframework.stereotype.Component;

/**
 * @Description: 动态数据源上下文
 * @author hjh
 * @date 2021/5/15 15:53
 */
@Component
public class DataSourceContextHolder {

    /**
     * 线程独立
     */
    private static ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    public static final String DB_DEFAULT_MySQL = "mysql";
    public static final String DB_TYPE_ShardingJDBC = "sharding";

    public static String getDataBaseType(){
        return contextHolder.get();
    }

    public static void setDataBaseType(String dataBase){
        contextHolder.set(dataBase);
    }

    public static void clearDataBaseType(){
        contextHolder.remove();
    }

}
