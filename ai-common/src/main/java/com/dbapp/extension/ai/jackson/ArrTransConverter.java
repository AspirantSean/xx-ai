package com.dbapp.extension.ai.jackson;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName ArrTransConverter
 * @Description jackson反序列化处理，List<Object>数据转换
 * @Author joker.tong
 * @Date 2019/12/5 19:15
 * @Version 1.0
 **/
public class ArrTransConverter<T> extends StdConverter<List<Object>, List<T>> {
    private Function<Map<String, Object>, T> getValue;
    private Class<T> tClass;

    public ArrTransConverter(Function<Map<String, Object>, T> getValue) {
        this.getValue = getValue;
        this.tClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public List<T> convert(List<Object> datas) {
        if (CollectionUtil.isEmpty(datas)) {
            return new ArrayList<>(0);
        }
        if (datas.get(0).getClass().isAssignableFrom(tClass)) {
            return (List<T>) datas;
        }
        return datas.stream()
                .map(o -> getValue.apply((Map<String, Object>) o))
                .collect(Collectors.toList());
    }
}
