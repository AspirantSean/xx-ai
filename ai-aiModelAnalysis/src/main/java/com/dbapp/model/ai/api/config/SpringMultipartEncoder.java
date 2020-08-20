package com.dbapp.model.ai.api.config;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class SpringMultipartEncoder extends SpringFormEncoder {

    public SpringMultipartEncoder(Encoder delegate) {
        super(delegate);
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        // MultipartFile数组处理
        if (bodyType.equals(MultipartFile[].class)) {
            MultipartFile[] file = (MultipartFile[]) object;
            if (file != null) {
                Map data = Collections.singletonMap(file.length == 0 ? "" : file[0].getName(), object);
                super.encode(data, MAP_STRING_WILDCARD, template);
            }
        } else {
            // 其他类型调用父类默认处理方法
            super.encode(object, bodyType, template);
        }
    }
}
