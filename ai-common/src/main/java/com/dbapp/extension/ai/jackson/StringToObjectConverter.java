package com.dbapp.extension.ai.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * @ClassName StringToObjectConverter
 * @Description
 * 如果输入为string，反序列化为java对象
 * 否则转化输入为java对象
 * @Author joker.tong
 * @Date 2019/12/3 17:33
 * @Version 1.0
 **/
public class StringToObjectConverter<T> extends StdConverter<Object, T> {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> tClass;

    public StringToObjectConverter() {
        this.tClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public T convert(Object s) {
        try {
            if (s instanceof String) {
                return objectMapper.readValue((String) s, this.tClass);
            } else {
                return objectMapper.convertValue(s, this.tClass);
            }
        } catch (IOException e) {
            throw new RuntimeJsonMappingException(e.getMessage());
        }
    }
}
