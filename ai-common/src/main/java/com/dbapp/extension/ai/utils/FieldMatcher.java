package com.dbapp.extension.ai.utils;

import com.dbapp.extension.ai.baas.dto.entity.output.OutputField;
import lombok.AllArgsConstructor;

/**
 * @ClassName FieldGetter
 * @Description 输出字段值获取
 * @Author joker.tong
 * @Date 2020/3/31 10:09
 * @Version 1.0
 **/
@AllArgsConstructor
public class FieldMatcher<V> {
    private FieldGetter<V> apply;
    private OutputField field;

    /**
     * 设置字段可选处理逻辑
     *
     * @param then
     * @return
     */
    public FieldMatcher<V> or(FieldGetter<V> then) {
        this.apply = this.apply.or(then);
        return this;
    }

    /**
     * 触发字段匹配操作
     *
     * @return
     */
    public V get() {
        return this.apply.get(field);
    }
}