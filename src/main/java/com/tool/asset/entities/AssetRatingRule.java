package com.tool.asset.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.tool.asset.sqlTool.SQL;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ytm
 * @version 2.0
 * @since 2021/11/19 14:42
 */
@Data
public class AssetRatingRule implements Comparable<AssetRatingRule> {
    /**
     * 资产评级规则ID
     */
    private long id;
    /**
     * 评级分组
     */
    private String groupName;
    /**
     * 评级等级
     *
     * @see AssetHealthyStatus
     */
    private AssetHealthyStatus rank;
    /**
     * 评级资产字段名
     */
    private String fieldName;
    /**
     * 定级标签
     */
    private String gradingLabel;
    /**
     * 资产评级其他标签
     */
    private String otherTag;
    /**
     * 分值
     */
    private Double score;
    /**
     * 评级条件
     */
    private String condition;
    /**
     * 数据来源
     */
    private String datasource;
    /**
     * 备注
     */
    private String note;
    /**
     * 评级类型，根据datasource
     */
    @JsonIgnore
    private transient volatile int rateType;
    /**
     * 所在分组
     *
     * @see AssetRatingGroup
     */
    @JsonIgnore
    private transient AssetRatingGroup group;
    /**
     * 转为查询语句后的语句
     */
    @JsonIgnore
    private transient String sql;
    /**
     * 子查询条件，当前为DNS服务器提供，后续可继续拓展
     */
    @JsonIgnore
    private transient String subCondition = "appProtocol != 'dns'";
    /**
     * 必定成立的条件
     */
    @JsonIgnore
    private transient String sureCondition = "1 = 1";
    /**
     * 时间格式化工具
     */
    @JsonIgnore
    public static transient DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 当次评级时间
     */
    @JsonIgnore
    private transient LocalDateTime ratingTime = LocalDateTime.now();
    /**
     * 查询时间范围
     */
    @JsonIgnore
    private transient Pair<String, String> timeGetter = Pair.of(ratingTime.toLocalDate().atStartOfDay().format(dateTimeFormatter), ratingTime.format(dateTimeFormatter));
    /**
     * 当天的windowId列表
     */
    @JsonIgnore
    private transient List<String> windowIds = new ArrayList<>();

    /**
     * 等级排序映射
     */
    private static final transient Map<AssetHealthyStatus, Integer> RANK_ORDER_MAP = ImmutableMap.of(
            AssetHealthyStatus.fallen, 0,
            AssetHealthyStatus.high_risk, 1,
            AssetHealthyStatus.medium_risk, 2,
            AssetHealthyStatus.low_risk, 3,
            AssetHealthyStatus.healthy, 4);

    public void setDatasource(String datasource) {
        this.datasource = datasource;
        switch (this.datasource) {
            case "告警列表":
                this.rateType = WaitForRatingAsset.RATE_CK_ALARM;
                break;
            case "漏洞弱点":
            case "弱点管理":
                this.rateType = WaitForRatingAsset.RATE_VULNERABILITY;
                break;
        }
    }

    /**
     * 初始化评级时间
     *
     * @param startTime
     * @param endTime
     * @param ratingTime
     */
    public void init(String startTime, String endTime, LocalDateTime ratingTime, List<String> windowIds) {
        this.ratingTime = ratingTime;
        this.timeGetter = Pair.of(startTime, LocalDateTime.parse(endTime, dateTimeFormatter).plusSeconds(1).format(dateTimeFormatter));
        this.windowIds = windowIds;
    }

    public int getRateType() {
        if (0 == this.rateType) {
            setDatasource(this.datasource);
        }
        return this.rateType;
    }

    @Override
    public int compareTo(AssetRatingRule assetRatingRule) {
        int diff = RANK_ORDER_MAP.get(rank) - RANK_ORDER_MAP.get(assetRatingRule.rank);
        if (diff != 0) {// 先根据等级排序
            return diff;
        }
        return Double.compare(assetRatingRule.score, score);// 再根据评分倒序排序
    }

    /**
     * 根据数据源获取表名
     *
     * @return
     */
    public String tableName(String... fields) {
        switch (this.datasource) {
            case "告警列表":
                return generateCKTableName(fields);
            case "漏洞弱点":
            case "弱点管理":
                return "t_asset_vulnerability_information";
            default:
                return this.datasource;
        }
    }

    static final String TABLE_AILPHA_SECURITY_ALARM = "ailpha_security_alarm";
    static final String TABLE_AILPHA_SECURITY_MERGE_ALARM_HANDLE = "ailpha_security_merge_alarm_handle";

    private String generateCKTableName(String... fields) {
        String subSql = new SQL()
                .SELECT("min(t.eventId) AS eventId",
                        "count(1) AS eventCount",
                        "min(t.startTime) AS startTime",
                        "max(t.endTime) AS endTime",
                        "t.windowId",
                        "t.aggCondition",
                        "t.netId")
                .FROM(TABLE_AILPHA_SECURITY_ALARM + " AS t")
                .WHERE("t.endTime >= '" + timeGetter.getLeft() + "'")
                .WHERE("t.endTime < '" + timeGetter.getRight() + "'")
                .GROUP_BY("t.windowId", "t.aggCondition", "t.netId")
                .toString();
        Set<String> specialFields = new HashSet<>(Arrays.asList("eventCount", "startTime", "endTime"));
        String sql = new SQL().SELECT(Stream.of(fields)
                        .distinct()
                        .map(field -> specialFields.contains(field) ? "b." + field + " AS " + field : "a." + field)
                        .toArray(String[]::new))
                .FROM(TABLE_AILPHA_SECURITY_ALARM + " AS a")
                .ANY_RIGHT_JOIN("(" + subSql + ") AS b", "a.eventId = b.eventId AND a.windowId = b.windowId AND a.aggCondition = b.aggCondition")
                .WHERE("a.endTime >= '" + timeGetter.getLeft() + "'")
                .WHERE("a.endTime < '" + timeGetter.getRight() + "'")
                .WHERE("a.eventId = b.eventId")
                .WHERE("a.windowId = b.windowId")
                .WHERE("a.aggCondition = b.aggCondition")
                .WHERE("(a.windowId, a.aggCondition) NOT IN ("
                        + new SQL()
                        .SELECT("windowId", "aggCondition")
                        .FROM(TABLE_AILPHA_SECURITY_MERGE_ALARM_HANDLE + " final")
                        .WHERE(windowIds == null || windowIds.isEmpty()
                                ? "handleTime >= today()"
                                : windowIds.stream().collect(Collectors.joining("', '", "windowId IN ('", "')")))
                        .WHERE("alarmStatus != 'unprocessed'").toString() + ")")
                .toString();
        return "(" + sql + ")";
    }

}
