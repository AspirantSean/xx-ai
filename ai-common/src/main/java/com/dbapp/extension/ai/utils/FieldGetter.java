package com.dbapp.extension.ai.utils;

import com.dbapp.extension.ai.baas.dto.entity.output.OutputField;

import java.util.function.Function;

/**
 * @ClassName FieldGetter
 * @Description 输出字段匹配
 * @Author joker.tong
 * @Date 2020/3/31 10:08
 * @Version 1.0
 **/
public interface FieldGetter<V> extends Function<OutputField, Function<? extends OutputField, V>> {
    /**
     * 设置字段可选处理逻辑
     *
     * @param then
     * @return
     */
    default FieldGetter<V> or(FieldGetter<V> then) {
        return f -> {
            Function<? extends OutputField, V> func = this.apply(f);
            if (func != null) {
                return func;
            }
            return then.apply(f);
        };
    }

    /**
     * 尝试对字段做匹配操作，匹配不上返回null
     *
     * @param field
     * @return
     */
    default V get(OutputField field) {
        if (field == null) {
            return null;
        }
        Function<OutputField, V> func = (Function<OutputField, V>) this.apply(field);
        if (func != null) {
            return func.apply(field);
        }
        return null;
    }
}