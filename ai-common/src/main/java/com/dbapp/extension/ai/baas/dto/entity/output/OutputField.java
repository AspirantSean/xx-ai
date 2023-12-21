package com.dbapp.extension.ai.baas.dto.entity.output;

import com.dbapp.extension.ai.utils.FieldGetter;
import com.dbapp.extension.ai.utils.FieldMatcher;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import static com.dbapp.extension.ai.baas.dto.entity.output.OutputField.*;

/**
 * @ClassName OutputField
 * @Description 输出字段描述
 * @Author joker.tong
 * @Date 2019/12/4 9:29
 * @Version 1.0
 **/
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "method", visible = true, defaultImpl = OutputField.class)
@JsonSubTypes({
        @JsonSubTypes.Type(name = DEFAULT_TEMPLATE, value = DefaultTemplate.class),
        @JsonSubTypes.Type(name = DYNAMIC_TEMPLATE, value = DynamicTemplate.class),
        @JsonSubTypes.Type(name = STATIC, value = StaticField.class),
        @JsonSubTypes.Type(name = WORD_MAPPING, value = WordMapping.class),
        @JsonSubTypes.Type(name = EXPRESSION, value = ExpressionField.class),
        @JsonSubTypes.Type(name = TOP, value = TopField.class),
        @JsonSubTypes.Type(name = NO_DROP, value = NoDropField.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OutputField {
    public static final String DEFAULT_TEMPLATE = "defaultTemplate";
    public static final String DYNAMIC_TEMPLATE = "dynamicTemplate";
    public static final String STATIC = "static";
    public static final String WORD_MAPPING = "wordmapping";
    public static final String EXPRESSION = "expression";
    public static final String TOP = "top";
    public static final String NO_DROP = "noDrop";
    //输出字段类型，expression、static、wordmapping、defaultTemplate
    private String method;

    public OutputField(String method) {
        this.method = method;
    }

    /**
     * 输出字段值动态匹配，根据不同类型选择处理函数
     *
     * @param apply
     * @param <V>
     * @return
     */
    public <V> FieldMatcher<V> match(FieldGetter<V> apply) {
        return new FieldMatcher<>(apply, this);
    }

    /**
     * 对字段值尝试取值，如果类型不匹配返回null
     *
     * @param apply
     * @param <V>
     * @return
     */
    public <V> V get(FieldGetter<V> apply) {
        return apply.get(this);
    }
}
