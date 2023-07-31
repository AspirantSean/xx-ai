package com.dbapp.extension.sync.restful.exception;

import com.dbapp.extension.sync.restful.response.ErrorResponse;

/**
 * {@code RestfulApiException}
 * restful响应异常
 *
 * @author joker.tong
 * @version 1.0
 **/
public class RestfulApiException extends RuntimeException {
    private final ErrorResponse response;

    public RestfulApiException(String message, ErrorResponse response) {
        super(message);
        this.response = response;
    }

    public RestfulApiException(String message, ErrorResponse response, Throwable cause) {
        super(message, cause);
        this.response = response;
    }
}
