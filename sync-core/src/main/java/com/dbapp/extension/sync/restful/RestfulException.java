package com.dbapp.extension.sync.restful;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.dbapp.extension.sync.restful.entity.ErrorCode;
import com.dbapp.extension.sync.restful.response.ErrorResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

/**
 * {@code RestfulException}
 * restful接口异常
 *
 * @author joker.tong
 * @version 1.0
 **/
@Slf4j
@Getter
public class RestfulException extends RuntimeException {
    @Nullable
    private HttpStatus httpStatus;
    @Nullable
    private ErrorResponse errorResponse;

    public RestfulException(String message, ErrorResponse errorResponse) {
        this(message, errorResponse, null);
    }

    public RestfulException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public RestfulException(String message, ErrorResponse errorResponse, Throwable cause) {
        super(message, cause);
        this.errorResponse = errorResponse;
        if (errorResponse != null) {
            this.httpStatus = errorResponse.getError().getCode().getStatus();
        }
    }

    public RestfulException(String message, ErrorCode errorCode) {
        this(message, errorCode.message(message).build(), null);
    }

    public static void cast(HttpStatus status, String message, Object... args) {
        throw new RestfulException(StrUtil.format(message, args), status);
    }

    public static void cast(String message, Object... args) {
        throw new RestfulException(StrUtil.format(message, args), ErrorCode.BadArgument);
    }

    public static void nullThrow(Object object, String message, Object... args) {
        trueThrow(object == null, message, args);
    }

    public static void nullThrow(Object object, String message, String logger, Object... args) {
        if (object == null && StrUtil.isNotBlank(logger)) {
            log.error(StrUtil.format(logger, args));
        }
        trueThrow(object == null, message, args);
    }

    public static void trueThrow(boolean flag, String message, Object... args) {
        if (flag) {
            cast(message, args);
        }
    }

    /**
     * 检查是否为空
     *
     * @param str
     * @param message
     * @param args
     */
    public static void blankThrow(String str, String message, Object... args) {
        trueThrow(StrUtil.isBlank(str), message, args);
    }

    public static void containThrow(Object search, Object[] objects, String message) {
        falseThrow(ArrayUtil.contains(objects, search), message);
    }

    public static void falseThrow(boolean flag, String message, Object... args) {
        trueThrow(!flag, message, args);
    }
}
