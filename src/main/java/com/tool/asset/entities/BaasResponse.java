package com.tool.asset.entities;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName BaasResponse
 * @Description baasapi接口响应
 * @Author joker.tong
 * @Date 2019/7/23 13:22
 * @Version 1.0
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaasResponse {
    public static final BaasResponse BAAS_SUCCESS = new BaasResponse(0, "success", null);
    //响应码
    private int code;
    //响应消息
    private String message;
    //返回数据
    private JSONObject data;
}
