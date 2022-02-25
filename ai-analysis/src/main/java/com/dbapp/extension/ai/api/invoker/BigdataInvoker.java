package com.dbapp.extension.ai.api.invoker;

import com.alibaba.fastjson.JSON;
import com.dbapp.extension.ai.api.exception.BigdataException;
import com.dbapp.extension.mirror.response.BigdataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public interface BigdataInvoker {
    Logger LOGGER = LoggerFactory.getLogger(BigdataInvoker.class);

    static <T> T invoke(Supplier<com.dbapp.extension.mirror.response.BigdataResponse<T>> supplier) {
        BigdataResponse<T> response = doInvoke(supplier);
        if (response.getCode() != 0) {
            LOGGER.error("Bigdata接口查询失败，{}", JSON.toJSONString(response));
            throw new BigdataException(String.format("Bigdata接口查询失败，code: %d, message: %s", response.getCode(), response.getMessage()), response);
        }
        return response.getData();
    }

    static <T> T doInvoke(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BigdataException("Bigdata接口服务异常", e);
        }
    }

}
