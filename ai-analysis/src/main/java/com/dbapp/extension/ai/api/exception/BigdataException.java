package com.dbapp.extension.ai.api.exception;

import com.dbapp.extension.mirror.response.BigdataResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BigdataException extends RuntimeException {
    private com.dbapp.extension.mirror.response.BigdataResponse response;

    public BigdataException(String msg) {
        super(msg);
    }

    public BigdataException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BigdataException(String msg, BigdataResponse response) {
        super(msg);
        this.response = response;
    }
}


