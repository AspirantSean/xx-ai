package com.dbapp.extension.sync.restful.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @ClassName InnerError
 * @Description 内部详细错误信息
 * @Author joker.tong
 * @Date 2021/2/21 10:33
 * @Version 1.0
 **/
public class InnerError {
    //错误码
    private String code;
    private Object data;
    //内部详细错误
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("innerError")
    private InnerError innerError;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public InnerError getInnerError() {
        return innerError;
    }

    public void setInnerError(InnerError innerError) {
        this.innerError = innerError;
    }

    public InnerError() {

    }

    public InnerError(String code, Object data) {
        this.code = code;
        this.data = data;
    }
}
