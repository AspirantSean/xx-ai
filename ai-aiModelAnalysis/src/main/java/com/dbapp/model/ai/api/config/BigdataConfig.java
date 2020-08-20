package com.dbapp.model.ai.api.config;

import com.dbapp.utils.GlobalAttribute;
import feign.Logger;
import feign.RequestInterceptor;
import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;

public class BigdataConfig {
    /**
     * 所有请求头中添加token
     *
     * @return
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return t -> t.query("apiKey", GlobalAttribute.getPropertyString("bigdata.apiKey", "HYiAERrVecKWWdKay4Isl7E8F1OUCM1V"));
    }

    /**
     * 定义日志对象
     *
     * @return
     */
    @Bean
    public Logger logger(){
        return new Slf4jLogger("BigdataApi");
    }
}
