package com.dbapp.extension.sync.restful.entity;

import cn.hutool.core.util.StrUtil;
import com.dbapp.extension.sync.restful.response.ApiResponse;
import com.dbapp.extension.sync.restful.response.ErrorResponse;
import org.springframework.http.HttpStatus;

/**
 * @ClassName ErrorCode
 * @Description 内置错误码类型
 * @Author joker.tong
 * @Date 2021/2/21 10:14
 * @Version 1.0
 **/
public enum ErrorCode implements ResponseCode {
    //提供的参数无效
    BadArgument(HttpStatus.BAD_REQUEST),
    //提供的用户参数无效
    BadUserArgument(HttpStatus.BAD_REQUEST),
    //请求在当前上下文中无效
    InvalidOperation(HttpStatus.BAD_REQUEST),
    //认证失败
    AuthFailed(HttpStatus.UNAUTHORIZED),
    //请求未获授权，无法访问资源
    Unauthorized(HttpStatus.UNAUTHORIZED),
    //无效的apiKey
    InvalidApiKey(HttpStatus.UNAUTHORIZED),
    //无法完成验证
    NotAcceptable(HttpStatus.NOT_ACCEPTABLE),
    //用户无权限
    Forbidden(HttpStatus.FORBIDDEN),
    //找不到资源
    NotFound(HttpStatus.NOT_FOUND),
    //无法在允许时间内完成操作
    RequestTimeout(HttpStatus.REQUEST_TIMEOUT),
    //请求资源冲突，POST已存在资源或PATCH不存在资源
    Conflict(HttpStatus.CONFLICT),
    //请求资源超出系统上限
    Overlimit(HttpStatus.CONFLICT),
    //内部错误
    InternalErrors(HttpStatus.INTERNAL_SERVER_ERROR),

    //未实现
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED),
    //服务暂时不可用
    ServiceUnavailable(HttpStatus.SERVICE_UNAVAILABLE),
    //网关超时
    GatewayTimeout(HttpStatus.GATEWAY_TIMEOUT);

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    private final HttpStatus status;

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorResponse.ErrorResBuilder message(String message, String... args) {
        return ApiResponse.error(this).message(StrUtil.format(message, args));
    }

    public ErrorResponse build() {
        return ApiResponse.error(this).build();
    }

    public static boolean isError(int status) {
        HttpStatus httpStatus = HttpStatus.valueOf(status);
        return httpStatus.is4xxClientError() || httpStatus.is5xxServerError();
    }
}
