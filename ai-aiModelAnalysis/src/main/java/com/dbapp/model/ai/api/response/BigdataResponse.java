package com.dbapp.model.ai.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BigdataResponse<T> {
    //响应码
    private int code;
    //响应消息
    private String message;
    //返回数据
    private T data;
}
