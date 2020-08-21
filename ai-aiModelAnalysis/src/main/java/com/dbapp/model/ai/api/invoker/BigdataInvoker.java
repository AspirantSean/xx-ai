package com.dbapp.model.ai.api.invoker;

import com.alibaba.fastjson.JSON;
import com.dbapp.app.mirror.response.BigdataResponse;
import com.dbapp.model.ai.api.exception.BigdataException;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public interface BigdataInvoker {
    Logger LOGGER = LoggerFactory.getLogger(BigdataInvoker.class);

    static <T> T invoke(Supplier<BigdataResponse<T>> supplier) {
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
        } catch (HystrixBadRequestException e) {
            LOGGER.error(e.getCause().getMessage(), e);
            throw new BigdataException("Bigdata接口参数异常", e);
        } catch (HystrixRuntimeException e) {
            LOGGER.error(e.getCause().getMessage(), e);
            throw new BigdataException("Bigdata接口调用失败", e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BigdataException("Bigdata接口服务异常", e);
        }
    }

}
