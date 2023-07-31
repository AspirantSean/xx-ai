package com.dbapp.extension.sync.restful.entity;

import org.springframework.http.HttpStatus;

/**
 * @ClassName ResponseCode
 * @Description 响应码
 * @Author joker.tong
 * @Date 2021/2/26 14:21
 * @Version 1.0
 **/
public interface ResponseCode {
    HttpStatus getStatus();
}
