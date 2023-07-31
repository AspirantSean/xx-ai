package com.dbapp.extension.sync.restful.entity;

import com.dbapp.extension.sync.restful.response.ApiResponse;
import com.dbapp.extension.sync.restful.response.DataResponse;
import org.springframework.http.HttpStatus;

/**
 * @ClassName SuccessCode
 * @Description 操作成功码
 * @Author joker.tong
 * @Date 2021/2/24 10:22
 * @Version 1.0
 **/
public enum SuccessCode implements ResponseCode {
    //成功
    Success(HttpStatus.OK),
    //创建成功
    Created(HttpStatus.CREATED),
    //更新成功
    Updated(HttpStatus.OK),
    //已接受，但未完成
    Accepted(HttpStatus.ACCEPTED),
    //删除成功
    Deleted(HttpStatus.NO_CONTENT);

    SuccessCode(HttpStatus status) {
        this.status = status;
    }

    private final HttpStatus status;

    public HttpStatus getStatus() {
        return status;
    }

    public <T> DataResponse.DataBuilder<T> data(T data) {
        return ApiResponse.success(this, data);
    }

    public static boolean isSuccess(int status) {
        return HttpStatus.valueOf(status).is2xxSuccessful();
    }
}
