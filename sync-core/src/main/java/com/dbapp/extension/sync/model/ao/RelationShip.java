package com.dbapp.extension.sync.model.ao;

import com.dbapp.extension.sync.enums.RelationType;
import com.dbapp.extension.sync.enums.ValueType;
import com.dbapp.extension.sync.enums.VariantType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationShip {
    /**
     * 对象映射到elasticsearch中时以什么类型保存，默认object
     */
    private VariantType variant = VariantType.object;
    /**
     * 关联关系
     */
    private RelationType type;
    /**
     * 通过外键关联时使用
     */
    private ForeignKey foreignKey;
    /**
     * 通过外表关联时使用
     * todo 暂未定义
     */
    private Object throughTables;
    /**
     * 特定连接条件
     */
    private List<Condition> conditions;

    /**
     * 子表与主表的外键关联关系
     */
    @Data
    public static class ForeignKey {
        /**
         * 主表被关联字段：字段数等于子表字段数
         */
        private List<String> parent;
        /**
         * 子表关联字段：字段数等于主表字段数
         */
        private List<String> child;
    }

    @Data
    public static class Condition {
        /**
         * 字段名
         */
        private String field;
        /**
         * 字段值
         */
        private String value;
        /**
         * 字段类型
         */
        private ValueType type;
        /**
         * 算术运算符
         */
        private String arithmeticOperator;
        /**
         * 逻辑连接符
         */
        private String logicalOperator;

        public String convertToString(String tableNameDot) {
            return type.convertToCondition(StringUtils.isBlank(tableNameDot) ? field : tableNameDot + field, arithmeticOperator, value);
        }

    }

}
