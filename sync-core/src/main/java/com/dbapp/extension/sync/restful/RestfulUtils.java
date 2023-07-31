package com.dbapp.extension.sync.restful;

import com.dbapp.extension.sync.restful.entity.Sort;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName RestfulUtils
 * @Description restful工具类
 * @Author joker.tong
 * @Date 2021/2/21 15:11
 * @Version 1.0
 **/
public class RestfulUtils {


    //默认每页记录数
    public static final int DEFAULT_PAGE_SIZE = 10;
    //页码参数名
    public static final String PAGE_NUM_PARAM = "$page";
    //每页记录数参数名
    public static final String PAGE_SIZE_PARAM = "$size";

    //排序参数名
    public static final String SORT_PARAM = "$orderBy";
    //多个查询参数以逗号分隔
    public static final String ORDER_DELIMITER = ",";
    //排序字段以排序类型已空格分隔
    public static final String DIRECTION_DELIMITER = "\\s+";
    //默认排序类型
    public static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.ASC;
    //apikey超时时间
    public static final long APIKEY_EXPIRE_TIME = TimeUnit.HOURS.toMillis(4);

}
