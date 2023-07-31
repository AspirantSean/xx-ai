package com.dbapp.extension.sync.restful.response;


import com.dbapp.extension.sync.restful.entity.ErrorCode;

import java.util.List;

/**
 * @ClassName ErrorResponse
 * @Description 错误响应
 * @Author joker.tong
 * @Date 2021/2/20 16:36
 * @Version 1.0
 **/
public class ErrorResponse extends ApiResponse {
    //错误信息
    private ErrorRes error;

    public ErrorRes getError() {
        return error;
    }

    public void setError(ErrorRes error) {
        this.error = error;
    }

    public static class ErrorResBuilder {
        private ErrorRes error = new ErrorRes();

        public ErrorResBuilder(ErrorCode errorCode) {
            this.error.setCode(errorCode);
        }

        public ErrorResBuilder message(String message) {
            this.error.setMessage(message);
            return this;
        }

        public ErrorResBuilder target(String target) {
            this.error.setTarget(target);
            return this;
        }

        public ErrorResBuilder details(List<ErrorRes> details) {
            this.error.setDetails(details);
            return this;
        }

        public ErrorResBuilder innerError(InnerError innerError) {
            this.error.setInnerError(innerError);
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse response = new ErrorResponse();
            response.setError(this.error);
            return response;
        }
    }
}
