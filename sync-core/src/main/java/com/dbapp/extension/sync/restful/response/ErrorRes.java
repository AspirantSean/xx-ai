package com.dbapp.extension.sync.restful.response;

import com.dbapp.extension.sync.restful.entity.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @ClassName ErrorRes
 * @Description 错误信息
 * @Author joker.tong
 * @Date 2021/2/21 10:33
 * @Version 1.0
 **/
public class ErrorRes {
    //错误码
    private ErrorCode code;
    //错误信息
    private String message;
    //错误位置
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String target;
    //错误组
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ErrorRes> details;
    //内部详细错误
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("innererror")
    private InnerError innerError;

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<ErrorRes> getDetails() {
        return details;
    }

    public void setDetails(List<ErrorRes> details) {
        this.details = details;
    }

    public InnerError getInnerError() {
        return innerError;
    }

    public void setInnerError(InnerError innerError) {
        this.innerError = innerError;
    }
}
