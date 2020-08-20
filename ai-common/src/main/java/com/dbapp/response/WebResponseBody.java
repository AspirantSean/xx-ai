package com.dbapp.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class WebResponseBody {

    public static int CODE_OK = 0;
    public static int CODE_FAIL = -1;
    public static String MESSAGE_SUCCESS = "成功";
    public static String MESSAGE_FAIL = "失败";

    private int code;

    private String pageId;

    private Object data;

    private boolean success;

    private String message;

    public static WebResponseBody builder() {
        return new WebResponseBody();
    }

    private WebResponseBody() {
    }

    public WebResponseBody code(int code) {
        this.code = code;
        return this;
    }

    public WebResponseBody pageId(String pageId) {
        this.pageId = pageId;
        return this;
    }

    public WebResponseBody data(Object data) {
        this.data = data;
        return this;
    }

    public WebResponseBody success(boolean success) {
        this.success = success;
        return this;
    }

    public WebResponseBody message(String message) {
        this.message = message;
        return this;
    }

}
