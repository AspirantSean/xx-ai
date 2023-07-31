package com.dbapp.extension.sync.model.ao;

import cn.hutool.core.collection.CollUtil;
import com.dbapp.extension.sync.constant.Constant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncTableConfig {

    /**
     * 模式，在postgresql中存在，相当于逻辑数据库，是对数据库中对象的逻辑分组与管理
     */
    private String schema;
    /**
     * 表名
     */
    private String table;
    /**
     * 映射到elasticsearch中时，此表对应的字段名，当此表为子属性时可使用
     */
    private String label;
    /**
     * 主键，原字段
     */
    private String key;
    /**
     * 需同步的字段列表
     */
    private List<String> columns;
    /**
     * 转换配置
     */
    private TransformConfig transform;
    /**
     * 此对象作为子属性时与主表的关联关系
     */
    private RelationShip relationShip;
    /**
     * 数据库子表-elasticsearch子属性
     */
    private List<SyncTableConfig> children;
    /**
     * 主表
     */
    @JsonIgnore
    private transient SyncTableConfig parent;
    /**
     * 所属数据库配置
     */
    @JsonIgnore
    private transient DatabaseConfig belongTo;

    public SyncTableConfig setParent(SyncTableConfig parent) {
        this.parent = parent;
        if (CollUtil.isNotEmpty(children)) {
            for (SyncTableConfig child : children) {
                child.setParent(this);
            }
        }
        return this;
    }

    public SyncTableConfig setBelongTo(DatabaseConfig belongTo) {
        this.belongTo = belongTo;
        if (CollUtil.isNotEmpty(children)) {
            for (SyncTableConfig child : children) {
                child.setBelongTo(belongTo);
            }
        }
        return this;
    }

    @JsonIgnore
    private String getTableName() {
        return belongTo.getDatabase() + "." + (StringUtils.isBlank(schema) ? "" : schema + ".") + getTable();
    }

    private String aliasName() {
        String alias = StringUtils.isBlank(this.label) ? this.table : this.label;
        return parent == null ? alias : parent.aliasName() + "_" + alias;
    }

    @JsonIgnore
    public String getAliasWithQuotation() {
        return Constant.quotation + aliasName() + Constant.quotation;
    }

    public String queryDataSql() {
        return selectColumn()
                .stream()
                .collect(Collectors.joining(", ", "SELECT ", fromTable()));
    }

    private String sqlByTable(String pSql) {
        // 查自身表
        StringBuilder tableSql = new StringBuilder("SELECT");
        if (parent == null) {
            tableSql.append(" ")
                    .append(String.join(", ", columns))
                    .append(" FROM ")
                    .append(getTable())
                    .append(" WHERE ")
                    .append(getKey())
                    .append(" IN (${ids})");
        } else {
            RelationShip.ForeignKey foreignKey = getRelationShip().getForeignKey();
            List<String> pfk = foreignKey.getParent();
            List<String> cfk = foreignKey.getChild();
            if (isKeyAssociationContinuous()) {
                tableSql.append(" ")
                        .append(String.join(", ", columns))
                        .append(" FROM ")
                        .append(getTable())
                        .append(" WHERE ")
                        .append(cfk.get(0))
                        .append(" IN (${ids})");
            } else {
                String pAlias = getTable() + "_relate_" + parent.getTable();
                tableSql.append(" ")
                        .append(String.join(", ", columns))
                        .append(" FROM ")
                        .append(getTable())
                        .append(" LEFT JOIN (")
                        .append("SELECT ")
                        .append(String.join(", ", pfk))
                        .append(pSql.substring(pSql.indexOf("FROM") + 4))
                        .append(") AS ")
                        .append(pAlias)
                        .append(" ON ")
                        .append(Stream.iterate(0, index -> index + 1)
                                .limit(pfk.size())
                                .map(index -> pAlias + "." + pfk.get(index) + " = " + getTable() + "." + cfk.get(index))
                                .collect(Collectors.joining(" AND ")));
            }
        }
        return tableSql.toString();
    }

    @JsonIgnore
    private boolean isKeyAssociationContinuous() {
        if (parent == null) {
            return true;
        } else {
            RelationShip.ForeignKey foreignKey = getRelationShip().getForeignKey();
            List<String> pfk = foreignKey.getParent();
            return pfk.size() == 1 && ((parent.parent == null && parent.getKey().equals(pfk.get(0))) || (parent.parent != null && pfk.get(0).equals(parent.getRelationShip().getForeignKey().getChild().get(0)))) && parent.isKeyAssociationContinuous();
        }
    }

    /**
     * todo 未完成分表查sql
     *
     * @param pSql
     */
    public void queryDataSqlByTable(String pSql) {
        // 本表
        String sql = sqlByTable(pSql);
        // 子表
        if (CollUtil.isNotEmpty(this.children)) {
            for (SyncTableConfig child : this.children) {
                String subSql = child.sqlByTable(sql);
            }
        }
    }

    private String fromTable() {
        String originalTableName = getTableName();// 表名
        String alias = getAliasWithQuotation();// 别名
        // 构建表名与别名映射
        StringBuilder fromBuilder = new StringBuilder(originalTableName)
                .append(" AS ")
                .append(alias);
        // 拼接 ON 条件
        RelationShip.ForeignKey foreignKey = relationShip != null ? relationShip.getForeignKey() : null;
        if (foreignKey != null) {
            List<String> parentKeys = foreignKey.getParent();
            List<String> childKeys = foreignKey.getChild();
            String parentAliasDot = parent.getAliasWithQuotation() + ".";
            String aliasDot = alias + ".";
            String onCondition = Stream.iterate(0, index -> index + 1)
                    .limit(parentKeys.size())
                    .map(index -> parentAliasDot + parentKeys.get(index)
                            + " = " + aliasDot + childKeys.get(index))
                    .collect(Collectors.joining(" AND ", " ON ", " "));
            fromBuilder.insert(0, " LEFT JOIN ").append(onCondition);
        } else {
            fromBuilder.insert(0, " FROM ");
        }
        // 拼接子表
        if (CollUtil.isNotEmpty(children)) {
            String subTables = children.stream()
                    .map(SyncTableConfig::fromTable)
                    .collect(Collectors.joining(""));
            fromBuilder.append(subTables);
        }
        return fromBuilder.toString();
    }

    private List<String> selectColumn() {
        List<String> columnList = new ArrayList<>();
        // 表路径
        String aliasNameUnderline = aliasName() + "_";
        String tableNameDot = getAliasWithQuotation() + ".";
        // 当前表字段
        if (CollUtil.isEmpty(this.columns)) {
            this.columns = Collections.singletonList("*");
        }
        Map<String, String> renameMap;
        if (transform != null && CollUtil.isNotEmpty(renameMap = transform.getRename())) {
            this.columns.stream()
                    .map(column -> {
                        String rename = renameMap.get(column);
                        return tableNameDot + column + " AS " + Constant.quotation + aliasNameUnderline + (StringUtils.isBlank(rename) ? column : rename) + Constant.quotation;
                    })
                    .forEach(columnList::add);

        } else {
            this.columns.stream()
                    .map(column -> tableNameDot + column + " AS " + Constant.quotation + aliasNameUnderline + column + Constant.quotation)
                    .forEach(columnList::add);
        }
        // 子表字段
        if (CollUtil.isNotEmpty(this.children)) {
            this.children.stream()
                    .map(SyncTableConfig::selectColumn)
                    .forEach(columnList::addAll);
        }
        return columnList;
    }

    /**
     * 查询结果集映射到elasticsearch对象的关系
     *
     * @return
     */
    public Mapping mapping() {
        Mapping mapping = new Mapping();
        // 表路径
        String aliasNameUnderline = aliasName() + "_";
        // 主键
        mapping.setKey(aliasNameUnderline + getKey());
        mapping.setKeyAlias(getKey());
        // 属性
        Map<String, String> map = new HashMap<>();
        // alias->rename
        Map<String, String> renameMap;
        if (transform != null && CollUtil.isNotEmpty(renameMap = transform.getRename())) {
            map.putAll(this.columns.stream()
                    .collect(Collectors.toMap(
                            column -> {
                                String rename = renameMap.get(column);
                                return aliasNameUnderline + (StringUtils.isBlank(rename) ? column : rename);
                            },
                            column -> {
                                String rename = renameMap.get(column);
                                return StringUtils.isBlank(rename) ? column : rename;
                            })));
            String keyRename;
            if (StringUtils.isNotBlank(keyRename = renameMap.get(getKey()))) {
                mapping.setKey(aliasNameUnderline + keyRename);
                mapping.setKeyAlias(keyRename);
            }
        } else {
            map.putAll(this.columns.stream()
                    .collect(Collectors.toMap(
                            column -> aliasNameUnderline + column,
                            column -> column)));
        }
        mapping.setMap(map);
        // 子属性
        if (CollUtil.isNotEmpty(this.children)) {
            mapping.setMappings(this.children.stream()
                    .collect(Collectors.toMap(SyncTableConfig::getLabel, SyncTableConfig::mapping)));
        }
        // 关联关系
        if (relationShip != null) {
            mapping.setRelationType(relationShip.getType());
        }
        return mapping;
    }

    /**
     * 获取elasticsearch数据模板
     *
     * @return {"properties": {"fieldName": {"type": "keyword"}, "fieldName": {"properties":{...}}}}
     */
    Map<String, Object> template() {
        Map<String, Object> templateMapping = this.transform.getMapping();
        if (CollUtil.isNotEmpty(this.children)) {
            Map<String, Map<String, Object>> childrenTemplates = this.children.stream()
                    .map(child -> Pair.of(StringUtils.isBlank(child.getLabel()) ? child.getTable() : child.getLabel(), child.template()))
                    .filter(pair -> CollUtil.isNotEmpty(pair.getRight()))
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            if (CollUtil.isNotEmpty(childrenTemplates)) {
                if (templateMapping == null) {
                    templateMapping = new HashMap<>();
                }
                templateMapping.putAll(childrenTemplates);
            }
        }
        return templateMapping;
    }

    /**
     * 返回值Tuple3<String, String, String>中，第一个字段为变更的表名，第二个为关联的字段名，第三个为如何通过变更数据获取关联字段值
     *
     * @return
     */
    public List<Tuple3<String, String, String>> obtainRelatedTables() {
        List<Tuple3<String, String, String>> relatedTables = new ArrayList<>();
        // 获取当前表与外键信息
        String tableName = getTable();
        if (parent == null) {
            relatedTables.add(Tuples.of(tableName, this.key, ""));// 队首一定为当前表，即最外层表
        } else {
            RelationShip.ForeignKey foreignKey = getRelationShip().getForeignKey();
            if (parent.parent == null) {
                // parent是最外层
                if (foreignKey.getParent().size() == 1 && Objects.equals(parent.getKey(), foreignKey.getParent().get(0))) {
                    // 主表的主键作为当前表外键关联。获取当前表与外键信息
                    relatedTables.add(Tuples.of(tableName, foreignKey.getChild().get(0), ""));
                } else {
                    // 主表主键不是当前表外键或不是当前表唯一外键时，获取主表数据，返回主表主键
                    List<String> parentForeignKeys = foreignKey.getParent();
                    List<String> foreignKeys = foreignKey.getChild();
                    String onCondition = Stream.iterate(0, index -> index + 1)
                            .limit(parentForeignKeys.size())
                            .map(index -> parentForeignKeys.get(index)
                                    + " = OLD." + foreignKeys.get(index))
                            .collect(Collectors.joining(" AND ", " ON ", " "));
                    relatedTables.add(Tuples.of(tableName, parent.getKey(),
                            String.format("SELECT %s FROM %s WHERE %s", parent.getKey(), parent.getTableName(), onCondition)));
                }
            } else {
                // parent的外键存在说明还有上级主表，
                SyncTableConfig currentTableConfig = this;
                SyncTableConfig parentTableConfig = parent;
                // 如果向上关联的过程都是通过同一个字段值则无需进行反查表
                boolean needBackCheck = false;
                do {
                    List<String> cpfs = currentTableConfig.getRelationShip().getForeignKey().getParent();// 父表被子表关联的字段
                    List<String> pcfs = parentTableConfig.getRelationShip().getForeignKey().getChild();// 父表作为子表时关联的字段
                    if ((parentTableConfig.parent == null && cpfs.size() == 1 && parentTableConfig.getKey().equals(cpfs.get(0)))
                            || (parentTableConfig.parent != null && cpfs.size() == 1 && pcfs.size() == 1 && Objects.equals(cpfs.get(0), pcfs.get(0)))) {
                        currentTableConfig = parentTableConfig;
                        parentTableConfig = currentTableConfig.getParent();
                    } else {
                        needBackCheck = true;
                        break;
                    }
                } while (parentTableConfig != null);

                if (!needBackCheck) {
                    relatedTables.add(Tuples.of(tableName, foreignKey.getChild().get(0), ""));
                } else {
                    // 如果向上关联的过程不都是通过同一个字段值则需进行反查表
                    List<String> parentForeignKeys = foreignKey.getParent();
                    List<String> foreignKeys = foreignKey.getChild();
                    String fromTable = String.format("SELECT %s FROM %s WHERE %s",
                            String.join(", ", parent.getRelationShip()
                                    .getForeignKey()
                                    .getChild()),
                            parent.getTableName(),
                            Stream.iterate(0, index -> index + 1)
                                    .limit(parentForeignKeys.size())
                                    .map(index -> parentForeignKeys.get(index)
                                            + " = OLD." + foreignKeys.get(index))
                                    .collect(Collectors.joining(" AND ", " ON ", " ")));
                    // 进入循环
                    int number = 0;
                    currentTableConfig = parent;
                    parentTableConfig = currentTableConfig.getParent();
                    boolean hasAdded = false;
                    do {
                        String ctAlias = "c" + number;
                        String ptAlias = "p" + number;
                        RelationShip.ForeignKey currentForeignKey = currentTableConfig.getRelationShip().getForeignKey();
                        List<String> cForeignKeys = currentForeignKey.getChild();
                        List<String> pForeignKeys = currentForeignKey.getParent();
                        String selectField;
                        if (parentTableConfig.parent == null) {
                            List<String> cpfs = currentTableConfig.getRelationShip().getForeignKey().getParent();
                            if (cpfs.size() == 1 && parentTableConfig.getKey().equals(cpfs.get(0))) {
                                relatedTables.add(Tuples.of(tableName, currentTableConfig.getRelationShip().getForeignKey().getChild().get(0), fromTable));
                                hasAdded = true;
                                break;
                            }
                            selectField = ptAlias + "." + parentTableConfig.getKey();
                        } else {
                            // 父表的被关联字段与父表关联其他表的字段相同时
                            List<String> cpfs = currentTableConfig.getRelationShip().getForeignKey().getParent();
                            List<String> pcfs = parentTableConfig.getRelationShip().getForeignKey().getChild();
                            if (cpfs.size() == 1 && pcfs.size() == 1 && Objects.equals(cpfs.get(0), pcfs.get(0))) {
                                fromTable = fromTable.replaceFirst("\\." + currentTableConfig.getRelationShip().getForeignKey().getChild().get(0),
                                        "\\." + currentTableConfig.getRelationShip().getForeignKey().getChild().get(0) + " AS " + pcfs.get(0));
                                // 下个循环
                                currentTableConfig = parentTableConfig;
                                parentTableConfig = currentTableConfig.getParent();
                                number++;
                                continue;
                            }
                            // 父表的被关联字段与父表关联其他表的字段不相同时
                            selectField = parentTableConfig.getRelationShip()
                                    .getForeignKey()
                                    .getChild()
                                    .stream()
                                    .map(field -> ptAlias + "." + field)
                                    .collect(Collectors.joining(", "));
                        }
                        fromTable = String.format("SELECT %s FROM %s AS %s LEFT JOIN (%s) AS %s ON %s",
                                selectField, parentTableConfig.getTableName(), ptAlias, fromTable, ctAlias, Stream.iterate(0, index -> index + 1)
                                        .limit(pForeignKeys.size())
                                        .map(index -> ptAlias + "." + pForeignKeys.get(index)
                                                + " = " + ctAlias + "." + cForeignKeys.get(index))
                                        .collect(Collectors.joining(" AND ", " ON ", " ")));// 当前表数据
                        // 下个循环
                        currentTableConfig = parentTableConfig;
                        parentTableConfig = currentTableConfig.getParent();
                        number++;
                    } while (parentTableConfig != null);
                    if (!hasAdded) {
                        relatedTables.add(Tuples.of(tableName, currentTableConfig.getKey(), fromTable));
                    }
                }
            }
        }
        // 获取子表与当前表外键信息
        if (CollUtil.isNotEmpty(children)) {
            for (SyncTableConfig child : children) {
                relatedTables.addAll(child.obtainRelatedTables());
            }
        }
        return relatedTables;
    }


}
