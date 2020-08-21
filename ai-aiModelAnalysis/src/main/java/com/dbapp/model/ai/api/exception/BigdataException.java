package com.dbapp.model.ai.api.exception;

import com.dbapp.app.mirror.response.BigdataResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BigdataException extends RuntimeException {
    private BigdataResponse response;

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

