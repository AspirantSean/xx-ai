package com.tool.asset.sqlTool;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSQL<T extends AbstractSQL> {

    private static final String AND = ") \nAND (";
    private static final String OR = ") \nOR (";

    private SQLStatement<T> sql = new SQLStatement<>();

    public abstract T getSelf();

    public T UPDATE(String table) {
        sql().statementType = SQLStatement.StatementType.UPDATE;
        sql().tables.add(table);
        return getSelf();
    }

    public T SET(String sets) {
        sql().sets.add(sets);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T SET(String... sets) {
        sql().sets.addAll(Arrays.asList(sets));
        return getSelf();
    }

    public T INSERT_INTO(String tableName) {
        sql().statementType = SQLStatement.StatementType.INSERT;
        sql().tables.add(tableName);
        return getSelf();
    }

    public T VALUES(String columns, String values) {
        sql().columns.add(columns);
        sql().values.add(values);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T INTO_COLUMNS(String... columns) {
        sql().columns.addAll(Arrays.asList(columns));
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T INTO_VALUES(String... values) {
        List<String> sqlValues = sql().values;
        if (sqlValues.isEmpty()) {
            sqlValues.addAll(Arrays.asList(values));
        } else {
            INTO_VALUES_BATCH(sqlValues.toArray(new String[0]));
            sqlValues.clear();
            INTO_VALUES_BATCH(values);
        }
        return getSelf();
    }

    public T INTO_VALUES_SELECT(AbstractSQL<T> selectSQL) {
        sql().valuesSelect.add(selectSQL);
        return getSelf();
    }

    public T INTO_VALUES_BATCH(String... values) {
        sql().valuesBatch.add(Arrays.asList(values));
        return getSelf();
    }

    public T INTO_VALUES_BATCH(List<List<String>> values) {
        sql().valuesBatch.addAll(values);
        return getSelf();
    }

    public T SELECT(String columns) {
        sql().statementType = SQLStatement.StatementType.SELECT;
        sql().select.add(columns);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T SELECT(String... columns) {
        sql().statementType = SQLStatement.StatementType.SELECT;
        sql().select.addAll(Arrays.asList(columns));
        return getSelf();
    }

    public T SELECT_DISTINCT(String columns) {
        sql().distinct = true;
        SELECT(columns);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T SELECT_DISTINCT(String... columns) {
        sql().distinct = true;
        SELECT(columns);
        return getSelf();
    }

    public T DELETE_FROM(String table) {
        sql().statementType = SQLStatement.StatementType.DELETE;
        sql().tables.add(table);
        return getSelf();
    }

    public T FROM(String table) {
        sql().tables.add(table);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T FROM(String... tables) {
        sql().tables.addAll(Arrays.asList(tables));
        return getSelf();
    }

    public T JOIN(String join) {
        sql().join.add(join);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T JOIN(String... joins) {
        sql().join.addAll(Arrays.asList(joins));
        return getSelf();
    }

    public T INNER_JOIN(String join) {
        sql().innerJoin.add(join);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T INNER_JOIN(String... joins) {
        sql().innerJoin.addAll(Arrays.asList(joins));
        return getSelf();
    }

    public T LEFT_OUTER_JOIN(String join) {
        sql().leftOuterJoin.add(join);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T LEFT_OUTER_JOIN(String... joins) {
        sql().leftOuterJoin.addAll(Arrays.asList(joins));
        return getSelf();
    }

    public T RIGHT_OUTER_JOIN(String join) {
        sql().rightOuterJoin.add(join);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T RIGHT_OUTER_JOIN(String... joins) {
        sql().rightOuterJoin.addAll(Arrays.asList(joins));
        return getSelf();
    }

    public T ANY_RIGHT_JOIN(String subSql, String onCondition) {
        sql().anyRightJoins.add(Pair.of(subSql, onCondition));
        return getSelf();
    }

    public T OUTER_JOIN(String join) {
        sql().outerJoin.add(join);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T OUTER_JOIN(String... joins) {
        sql().outerJoin.addAll(Arrays.asList(joins));
        return getSelf();
    }

    public T WHERE(String conditions) {
        if(StringUtils.isNotBlank(conditions)){
            sql().where.add(conditions);
            sql().lastList = sql().where;
        }
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T WHERE(String... conditions) {
        sql().where.addAll(Arrays.asList(conditions));
        sql().lastList = sql().where;
        return getSelf();
    }

    public T OR() {
        sql().lastList.add(OR);
        return getSelf();
    }

    public T AND() {
        sql().lastList.add(AND);
        return getSelf();
    }

    public T GROUP_BY(String columns) {
        sql().groupBy.add(columns);
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T GROUP_BY(String... columns) {
        sql().groupBy.addAll(Arrays.asList(columns));
        return getSelf();
    }

    public T HAVING(String conditions) {
        sql().having.add(conditions);
        sql().lastList = sql().having;
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T HAVING(String... conditions) {
        sql().having.addAll(Arrays.asList(conditions));
        sql().lastList = sql().having;
        return getSelf();
    }

    public T ORDER_BY(String columns) {
        if (StringUtils.isNotEmpty(columns)) {
            sql().orderBy.add(columns);
        }
        return getSelf();
    }

    /**
     * @since 3.4.2
     */
    public T ORDER_BY(String... columns) {
        sql().orderBy.addAll(Arrays.asList(columns));
        return getSelf();
    }

    public T LIMIT(long limit) {
        sql().limit = LimitParam.getLimitParam(limit);
        return getSelf();
    }

    public T LIMIT(long offset, long limit) {
        sql().limit = LimitParam.getLimitParam(offset, limit);
        return getSelf();
    }

    private SQLStatement sql() {
        return sql;
    }

    public <A extends Appendable> A usingAppender(A a) {
        sql().sql(a);
        return a;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sql().sql(sb);
        return sb.toString();
    }

    private static class SafeAppendable {
        private final Appendable a;
        private boolean empty = true;

        public SafeAppendable(Appendable a) {
            super();
            this.a = a;
        }

        public SafeAppendable append(CharSequence s) {
            try {
                if (empty && s.length() > 0) {
                    empty = false;
                }
                a.append(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public boolean isEmpty() {
            return empty;
        }

    }

    private static class LimitParam {
        Long offset;
        Long limit;

        public LimitParam(Long limit) {
            this.limit = limit;
        }

        public LimitParam(Long offset, Long limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public static LimitParam getLimitParam(Long limit) {
            return new LimitParam(limit);
        }

        public static LimitParam getLimitParam(Long offset, Long limit) {
            return new LimitParam(offset, limit);
        }

        @Override
        public String toString() {
            if (limit == null || limit < 0) {
                return "";
            } else if (offset == null) {
                return limit.toString();
            } else {
                return offset + ", " + limit;
            }
        }
    }

    private static class SQLStatement<T extends AbstractSQL> {

        public enum StatementType {
            DELETE, INSERT, SELECT, UPDATE;
        }

        StatementType statementType;
        List<String> sets = new ArrayList<>();
        List<String> select = new ArrayList<>();
        List<String> tables = new ArrayList<>();
        List<String> join = new ArrayList<>();
        List<String> innerJoin = new ArrayList<>();
        List<String> outerJoin = new ArrayList<>();
        List<String> leftOuterJoin = new ArrayList<>();
        List<String> rightOuterJoin = new ArrayList<>();
        List<Pair<String, String>> anyRightJoins = new ArrayList<>();
        List<String> where = new ArrayList<>();
        List<String> having = new ArrayList<>();
        List<String> groupBy = new ArrayList<>();
        List<String> orderBy = new ArrayList<>();
        List<String> lastList = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<List<String>> valuesBatch = new ArrayList<>();
        List<AbstractSQL<T>> valuesSelect = new ArrayList<>();
        boolean distinct;
        LimitParam limit;

        public SQLStatement() {
            // Prevent Synthetic Access
        }

        private void sqlClause(SafeAppendable builder, String keyword, List<String> parts, String open, String close,
                               String conjunction) {
            if (!parts.isEmpty()) {
                if (!builder.isEmpty()) {
                    builder.append("\n");
                }
                builder.append(keyword);
                builder.append(" ");
                builder.append(open);
                String last = "________";
                for (int i = 0, n = parts.size(); i < n; i++) {
                    String part = parts.get(i);
                    if (i > 0 && !part.equals(AND) && !part.equals(OR) && !last.equals(AND) && !last.equals(OR)) {
                        builder.append(conjunction);
                    }
                    builder.append(part);
                    last = part;
                }
                builder.append(close);
            }
        }

        private String selectSQL(SafeAppendable builder) {
            if (distinct) {
                sqlClause(builder, "SELECT DISTINCT", select, "", "", ", ");
            } else {
                sqlClause(builder, "SELECT", select, "", "", ", ");
            }

            sqlClause(builder, "FROM", tables, "", "", ", ");
            joins(builder);
            sqlClause(builder, "WHERE", where, "(", ")", " AND ");
            sqlClause(builder, "GROUP BY", groupBy, "", "", ", ");
            sqlClause(builder, "HAVING", having, "(", ")", " AND ");
            sqlClause(builder, "ORDER BY", orderBy, "", "", ", ");
            String limitString;
            if (limit != null && StringUtils.isNotBlank(limitString = limit.toString())) {
                sqlClause(builder, "LIMIT ", Collections.singletonList(limitString), "", "", "");
            }
            return builder.toString();
        }

        private void joins(SafeAppendable builder) {
            sqlClause(builder, "JOIN", join, "", "", "\nJOIN ");
            sqlClause(builder, "INNER JOIN", innerJoin, "", "", "\nINNER JOIN ");
            sqlClause(builder, "OUTER JOIN", outerJoin, "", "", "\nOUTER JOIN ");
            sqlClause(builder, "LEFT OUTER JOIN", leftOuterJoin, "", "", "\nLEFT OUTER JOIN ");
            sqlClause(builder, "RIGHT OUTER JOIN", rightOuterJoin, "", "", "\nRIGHT OUTER JOIN ");
            for (Pair<String, String> anyRightJoin : anyRightJoins) {
                sqlClause(builder, "ANY RIGHT JOIN", Collections.singletonList(anyRightJoin.getLeft()), "", "", "");
                sqlClause(builder, "ON", Collections.singletonList(anyRightJoin.getRight()), "", "", "");
            }
        }

        private String insertSQL(SafeAppendable builder) {
            sqlClause(builder, "INSERT INTO", tables, "", "", "");
            sqlClause(builder, "", columns, "(", ")", ", ");
            if (!valuesSelect.isEmpty()) {
                sqlClause(builder, "", Collections.singletonList(valuesSelect.get(0).toString()), "", "", "");
            } else {
                if (!values.isEmpty()) {
                    valuesBatch.add(values);
                    values.clear();
                }
                builder.append("\nVALUES ");
                for (int i = 0; i < valuesBatch.size(); i++) {
                    List<String> values = valuesBatch.get(i);
                    sqlClause(builder, "", values, "(", ")", ", ");
                    if (!values.isEmpty() && i < valuesBatch.size() - 1) {
                        builder.append(", ");
                    }
                }
            }
            return builder.toString();
        }

        private String deleteSQL(SafeAppendable builder) {
            sqlClause(builder, "DELETE FROM", tables, "", "", "");
            sqlClause(builder, "WHERE", where, "(", ")", " AND ");
            return builder.toString();
        }

        private String updateSQL(SafeAppendable builder) {
            sqlClause(builder, "UPDATE", tables, "", "", "");
            joins(builder);
            sqlClause(builder, "SET", sets, "", "", ", ");
            sqlClause(builder, "WHERE", where, "(", ")", " AND ");
            return builder.toString();
        }

        public String sql(Appendable a) {
            SafeAppendable builder = new SafeAppendable(a);
            if (statementType == null) {
                return null;
            }

            String answer;

            switch (statementType) {
                case DELETE:
                    answer = deleteSQL(builder);
                    break;

                case INSERT:
                    answer = insertSQL(builder);
                    break;

                case SELECT:
                    answer = selectSQL(builder);
                    break;

                case UPDATE:
                    answer = updateSQL(builder);
                    break;

                default:
                    answer = null;
            }

            return answer;
        }
    }
}