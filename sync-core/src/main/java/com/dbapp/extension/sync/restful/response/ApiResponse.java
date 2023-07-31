package com.dbapp.extension.sync.restful.response;

import com.dbapp.extension.sync.restful.RestfulException;
import com.dbapp.extension.sync.restful.entity.ErrorCode;
import com.dbapp.extension.sync.restful.entity.SuccessCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.function.Function;

/**
 * @ClassName ApiResponse
 * @Description restful接口响应
 * @Author joker.tong
 * @Date 2021/2/20 16:36
 * @Version 1.0
 **/
public class ApiResponse<T> {

    public static <T> DataResponse.DataBuilder<T> success(T data) {
        return new DataResponse.DataBuilder<>(SuccessCode.Success, data);
    }

    public static <T> DataResponse.DataBuilder<T> success(SuccessCode code, T data) {
        return new DataResponse.DataBuilder<>(code, data);
    }

    public static ErrorResponse.ErrorResBuilder error(ErrorCode errorCode) {
        return new ErrorResponse.ErrorResBuilder(errorCode);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this instanceof DataResponse;
    }

    /**
     * @param dataFunction
     * @param <R>
     * @return
     */
    public <R> T getData(Function<R, T> dataFunction) {
        R response = (R) this;
        return dataFunction.apply(response);
    }

    public void check(String message) {
        if (this instanceof ErrorResponse) {
            throw new RestfulException(message, (ErrorResponse) this);
        }
    }

    @SuppressWarnings("unchecked")
    public <U extends ApiResponse<?>> U realResponse() {
        return (U) this;
    }
}
