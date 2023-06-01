package com.tool.asset.handler;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tool.asset.api.BaaSCKServiceApi;
import com.tool.asset.ckmapper.AssetStatisticsTaskMapper;
import com.tool.asset.config.CKConfig;
import com.tool.asset.dao.AssetInformationDao;
import com.tool.asset.entities.*;
import com.tool.asset.sqlTool.SQL;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ytm
 * @version 2.0
 * @since 2023/5/29 9:23
 */
@Slf4j
public class AssetRatingHandler {

    protected final AssetInformationDao assetInformationDao = AssetInformationDao.getInstance();
    private final AssetStatisticsTaskMapper assetStatisticsTaskMapper = CKConfig.getAssetStatisticsTaskMapper();
    private final BaaSCKServiceApi iBaaSCKServiceApi;

    private AssetRatingHandler(String baasUrl) {
        iBaaSCKServiceApi = BaaSCKServiceApi.getInstance(baasUrl);
    }

    private static AssetRatingHandler assetRatingHandler;

    public synchronized static AssetRatingHandler getInstance(String baasUrl) {
        if (assetRatingHandler == null) {
            assetRatingHandler = new AssetRatingHandler(baasUrl);
        }
        return assetRatingHandler;
    }

    /**
     * 添加待评级资产，不能频繁调用
     * <br/>添加成功，则返回
     * <br/>否则（即待评级资产满足一定数量）使用调用线程立即触发该资产的评级
     *
     * @param rating
     * @param assetIds
     */
    public void pushRatingAsset(String rating, String... assetIds) {
        if (ArrayUtils.isEmpty(assetIds)) {
            return;
        }
        log("---------------------开始资产评级计算---------------------");
        long begin = System.currentTimeMillis();
        // 若待评级资产满足一定数量，则使用调用线程立即触发资产评级
        List<WaitForRatingAsset> waitForRatingAssets = assetInformationDao.getRatingAssetByIds(Arrays.asList(assetIds));
        if (waitForRatingAssets.isEmpty()) {// 资产不存在
            return;
        }
        LocalDateTime ratingTime = LocalDateTime.parse(rating, AssetRatingRule.dateTimeFormatter);// 当前时间，即评级任务时间
        LocalDateTime createTime = ratingTime.toLocalDate().atStartOfDay();// 当天日期
        List<AssetRatingGroup> assetRatingGroups = getRatingRules(createTime, ratingTime);
        if (assetRatingGroups.isEmpty()) {
            return;
        }
        //
        calculateWaitRatingAssets(assetRatingGroups, waitForRatingAssets);// 单个资产评级
        long took = System.currentTimeMillis() - begin;
        log("---------------------结束资产评级计算，耗时：{}小时{}分{}秒{}毫秒---------------------", took / 1000 / 60 / 60, took / 1000 / 60 % 60, took / 1000 % 60, took % 1000);
    }

    /**
     * 计算资产评级
     */
    public void calculate(String rating) {
        log("---------------------开始资产评级计算---------------------");
        long begin = System.currentTimeMillis();
        // 当前时间，即评级任务时间
        LocalDateTime ratingTime = LocalDateTime.parse(rating, AssetRatingRule.dateTimeFormatter);
        ratingTime = LocalDateTime.of(
                ratingTime.getYear(), ratingTime.getMonth(), ratingTime.getDayOfMonth(),
                ratingTime.getHour(), ratingTime.getMinute(), ratingTime.getSecond());// 移除毫秒值，MySQL中未存毫秒值，会导致误删评级数据
        LocalDateTime createTime = ratingTime.toLocalDate().atStartOfDay();// 当天日期
        List<AssetRatingGroup> assetRatingGroups = getRatingRules(createTime, ratingTime);
        if (assetRatingGroups.isEmpty()) {
            log("---------------------资产评级规则为空，或校验不通过，评级任务结束---------------------");
            return;
        }
        Map<Boolean, List<WaitForRatingAsset>> waitForRatingAssets = CollUtil.unionAll(
                        assetStatisticsTaskMapper.selectAssetIdsWhichHasAlarms(),// 查询存在告警的资产
                        assetInformationDao.selectAssetIdsWhichHasVulnerability())// 查询存在弱点漏洞的资产
                .stream()
                .collect(Collectors.toMap(
                        WaitForRatingAsset::getAssetId,
                        waitForRatingAsset -> waitForRatingAsset,
                        WaitForRatingAsset::elevateRuleType))
                .values()
                .stream()
                .collect(Collectors.partitioningBy(waitForRatingAsset -> "DNS服务器".equals(waitForRatingAsset.getAssetType())));// 按是否dns服务器分组
        log("当前待评级资产情况，待评级资产数：{}，待评级资产：{}", waitForRatingAssets.size(), waitForRatingAssets);
        try {
            // 资产评级
            batchCalculate(assetRatingGroups, waitForRatingAssets.get(Boolean.TRUE));// DNS服务器类型资产
            batchCalculate(assetRatingGroups, waitForRatingAssets.get(Boolean.FALSE));// 非DNS服务器类型资产
            // 不存在告警或漏洞的资产全部删除评级，或不参与评级
            assetInformationDao.changeAssetToHealth(Timestamp.valueOf(createTime), Timestamp.valueOf(ratingTime));
            // 删除资产评分100，健康的资产评级数据
            assetInformationDao.deleteHealthAssets();
        } catch (Exception e) {
            logError("资产评级评分异常", e);
        }
        long took = System.currentTimeMillis() - begin;
        log("---------------------结束资产评级计算，耗时：{}小时{}分{}秒{}毫秒---------------------", took / 1000 / 60 / 60, took / 1000 / 60 % 60, took / 1000 % 60, took % 1000);
    }

    /**
     * 分批处理列表中资产评级
     *
     * @param assetRatingGroups
     * @param waitForRatingAssets 需要区分是否DNS服务器资产
     */
    private void batchCalculate(List<AssetRatingGroup> assetRatingGroups, List<WaitForRatingAsset> waitForRatingAssets) {
        if (waitForRatingAssets.isEmpty()) {
            return;
        }
        // 资产评级
        int limit = 500;
        for (List<WaitForRatingAsset> waitForRatingAssetList : Lists.partition(waitForRatingAssets, limit)) {
            if (waitForRatingAssetList.isEmpty()) {// 资产已查完，结束
                continue;
            }
            doCalculate(assetRatingGroups, waitForRatingAssetList);// 评级当前批次资产
        }
    }

    /**
     * 若等待队列中存在资产，则进行等待队列资产评级
     *
     * @param assetRatingGroups
     * @param waitForRatingAssets
     */
    private void calculateWaitRatingAssets(List<AssetRatingGroup> assetRatingGroups, List<WaitForRatingAsset> waitForRatingAssets) {
        if (waitForRatingAssets.isEmpty()) {
            return;
        }
        Map<Boolean, List<WaitForRatingAsset>> waitForRatingAssetMap = waitForRatingAssets.stream()
                .collect(Collectors.partitioningBy(waitForRatingAsset -> "DNS服务器".equals(waitForRatingAsset.getAssetType())));
        // 资产评级
        doCalculate(assetRatingGroups, waitForRatingAssetMap.get(Boolean.TRUE));// DNS服务器类型资产
        doCalculate(assetRatingGroups, waitForRatingAssetMap.get(Boolean.FALSE));// 非DNS服务器类型资产
    }

    /**
     * 获取评级条件
     *
     * @param createTime
     * @param ratingTime
     * @return
     */
    private List<AssetRatingGroup> getRatingRules(LocalDateTime createTime, LocalDateTime ratingTime) {
        // 获取当天的windowId用于查询
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTime = createTime.format(dateTimeFormatter);// 当天日期
        String endTime = ratingTime.format(dateTimeFormatter);// 当前时间
        List<String> windowIds;
        try {
            JSONObject result = iBaaSCKServiceApi.getWindowIds(startTime, endTime);
            windowIds = result.getJSONArray("data").toJavaList(String.class);
        } catch (Exception e) {
            windowIds = new ArrayList<>();
            logError("获取baas计算的windowId列表失败，默认不使用windowIds", e);
        }
        List<String> finalWindowIds = windowIds;
        return assetInformationDao.getAssetRatingRules()
                .stream()
                .filter(rule -> StringUtils.isNotBlank(rule.getCondition()))
                .peek(assetRatingRule -> assetRatingRule.init(startTime, endTime, ratingTime, finalWindowIds))// 评级时间统一，当天windowId设置
                .filter(this::prepareAndValidateSQL)// 清除语法校验失败的规则
                .collect(Collectors.groupingBy(AssetRatingRule::getGroupName))
                .entrySet()
                .stream()
                .map(entry -> new AssetRatingGroup(entry.getKey(), entry.getValue()).sortAssetRatingRules())
                .collect(Collectors.toList());
    }

    /**
     * 计算资产评级
     *
     * @param assetRatingGroups
     * @param waitForRatingAssets
     */
    private void doCalculate(List<AssetRatingGroup> assetRatingGroups, List<WaitForRatingAsset> waitForRatingAssets) {
        if (waitForRatingAssets.isEmpty()) {
            return;
        }
        // 资产类型，是否DNS
        boolean isDNS = "DNS服务器".equals(waitForRatingAssets.get(0).getAssetType());
        // 评级计算
        log("当前评级资产批次：{}", waitForRatingAssets);
        Map<String, WaitForRatingAsset> waitForRatingAssetMap = waitForRatingAssets.stream()
                .collect(Collectors.toMap(WaitForRatingAsset::getAssetId, waitForRatingAsset -> waitForRatingAsset, (f, l) -> f));
        Map<String, AssetRatingIntermediateData> assetRatingIntermediateResults = new HashMap<>();
        for (AssetRatingGroup assetRatingGroup : assetRatingGroups) {// 按组每次对所有资产评级计算
            Map<String, WaitForRatingAsset> waitForRating = new HashMap<>(waitForRatingAssetMap);
            for (AssetRatingRule assetRatingRule : assetRatingGroup.getAssetRatingRules()) {// 每组只需满足条件最优的评级即可，都不满足则轮空，所有组都轮空时为健康
                String sql = assetRatingRule.getSql()
                        .replace(PLACEHOLDER,
                                waitForRating.values()
                                        .stream()
                                        .filter(waitForRatingAsset -> waitForRatingAsset.hasRuleType(assetRatingRule.getRateType()))
                                        .map(WaitForRatingAsset::getAssetId)
                                        .collect(Collectors.joining("', '", "'", "'")));
                log("当前评级查询语句：{}", sql);
                List<AssetRatingHit> assetRatingHits;
                if ("告警列表".equals(assetRatingRule.getDatasource())) {
                    assetRatingHits = assetStatisticsTaskMapper.ratingHits(
                            sql.replace(SPECIAL_CONDITION_PLACEHOLDER, isDNS ? assetRatingRule.getSubCondition() : assetRatingRule.getSureCondition()));
                } else {// 漏洞另算
                    assetRatingHits = assetInformationDao.rateAssetByVulnerability(
                            sql.replace(SPECIAL_CONDITION_PLACEHOLDER, assetRatingRule.getSureCondition()));
                }
                for (AssetRatingHit assetRatingHit : assetRatingHits) {
                    if (!assetRatingHit.isSatisfied()) {// 不满足此次评级条件则进入本组的下一次评级
                        continue;
                    }
                    String assetId = assetRatingHit.getAssetId();
                    waitForRating.remove(assetId);// 满足本组评级条件的则进入下一组评级，本组评级不再进行
                    AssetRatingIntermediateData assetRatingIntermediateData = assetRatingIntermediateResults.get(assetId);
                    if (assetRatingIntermediateData == null) {
                        assetRatingIntermediateData = new AssetRatingIntermediateData(assetId);
                        assetRatingIntermediateResults.put(assetId, assetRatingIntermediateData);
                    }
                    assetRatingIntermediateData.addAssetRatingRule(assetRatingRule);
                    log("资产{}满足{}评级", assetId, assetRatingRule.getId());
                }
            }
        }
        List<String> healthAssetIds = new ArrayList<>(waitForRatingAssetMap.keySet());// 不满足所有评级条件的资产为健康
        healthAssetIds.removeAll(assetRatingIntermediateResults.keySet());
        if (!healthAssetIds.isEmpty()) {
            try {
                LocalDateTime ratingTime = assetRatingGroups.get(0).getAssetRatingRules().get(0).getRatingTime();
                Timestamp createTime = Timestamp.valueOf(ratingTime.toLocalDate().atStartOfDay());
                Timestamp finishTime = Timestamp.valueOf(ratingTime);
                int deleteHealthyAssetCount = assetInformationDao.deleteHealthAssetsById(healthAssetIds, createTime, finishTime);
                log("删除健康资产{}条，assetId：{}", deleteHealthyAssetCount, healthAssetIds);
            } catch (Exception e) {
                logError("清理健康资产失败", e);
            }
        }
        // 所有评级结果
        List<AssetRatingIntermediateData> ratingResults = new ArrayList<>(assetRatingIntermediateResults.values());
        List<AssetRating> assetRatings = ratingResults.stream()
                .filter(AssetRatingIntermediateData::canGrading)
                .filter(assetRatingIntermediateData -> StringUtils.isNotBlank(assetRatingIntermediateData.getAssetId()))
                .map(AssetRatingIntermediateData::grading)
                .filter(assetRating -> StringUtils.isNotBlank(assetRating.getAssetId()))
                .collect(Collectors.toList());
        if (!assetRatings.isEmpty()) {
            try {
                int insertCount = assetInformationDao.insertAssetRating(assetRatings);
                // 存入redis资产评级活动列表
                log("当前批次资产数{}，更新资产评级数据{}条（统计时：新增一条+1，更新一条+2）", waitForRatingAssets.size(), insertCount);
            } catch (Exception e) {
                logError("插入资产评级数据异常，评级信息：" + JSON.toJSONString(assetRatings), e);
            }
        } else {
            log("当前批次资产数{}，更新资产评级数据0条", waitForRatingAssets.size());
        }
    }

    private static final String PLACEHOLDER = "${assetIds}";
    private static final String SPECIAL_CONDITION_PLACEHOLDER = "${specialCondition}";

    /**
     * 准备查询语句
     *
     * @param assetRatingRule
     */
    public boolean prepareAndValidateSQL(AssetRatingRule assetRatingRule) {
        try {
            String condition = assetRatingRule.getCondition();// 1、获取评级条件
            if (StringUtils.isBlank(condition)) {
                return false;
            }
            List<String> fields = new ArrayList<>();
            String fieldName = assetRatingRule.getFieldName();
            fields.add(fieldName);
            fields.add("appProtocol");
            SQL sql = new SQL()// 1.1、组合查询语句
                    .SELECT(fieldName + " AS assetId")// 1.2、查询的字段
                    .WHERE(fieldName + " IN (" + PLACEHOLDER + ")")// 1.3.1、资产ID列表占位符，逗号分隔
                    .WHERE(SPECIAL_CONDITION_PLACEHOLDER)// 1.3.2、DNS服务器占位符
                    .GROUP_BY(fieldName);// 1.4、统计的字段
            String[] parts = condition.split(" aggregation ");// 2、评级条件拆分为查询和统计两部分
            BaasResponse ckQuery = iBaaSCKServiceApi.getClickhouseQuery(ImmutableMap.of(
                    "dataSource", "security_alarms",
                    "queryStr", parts[0]));
            if (null != ckQuery && null != ckQuery.getData()) {// 组合where 条件
                JSONObject data = ckQuery.getData();
                sql.WHERE("(" + data.getString("queryCK") + ")");// 2.1、查询语法AiQL
                fields.addAll(data.getJSONArray("useField").toJavaList(String.class));
            }
            if (parts.length == 2) {// 2.2、统计条件，在数据库中使用统计函数
                sql.SELECT(parseAndCombineAggregationQL(parts[1], fields));// 2.3、先做逻辑或的分割，再做逻辑与的分割
            } else {// 2.4、默认查到即为满足条件
                sql.SELECT("'1' AS satisfied");
            }
            sql.FROM(assetRatingRule.tableName(fields.toArray(new String[0])));// 1.5、表名（来源）
            assetRatingRule.setSql(sql.toString());// 3、拼接sql完成
            log("评级查询语句组装结果：{}", sql);
            return true;
        } catch (Exception e) {
            logError("资产评级语句组装异常", e);
            return false;
        }
    }

    private final static String COLON_REGEX = ":";                       // 正则-冒号连接符
    private final static String COMPARISON_OPERATOR_REGEX = "[><=]=?";   // 正则-比较操作符
    private final static String logicConnector_and = "\\s+(AND|and)\\s+";// 统计条件-逻辑与连接符
    private final static String logicConnector_or = "\\s+(OR|or)\\s+";   // 统计条件-逻辑或连接符

    /**
     * 解析并组合聚合条件语句
     *
     * @param aggQL
     * @param fields
     * @return
     */
    private String parseAndCombineAggregationQL(String aggQL, List<String> fields) {
        return Stream.of(aggQL.split(logicConnector_or))// 2.3、先做逻辑或的分割
                .filter(StringUtils::isNotBlank)
                .map(orConnectorCondition ->
                        Stream.of(orConnectorCondition.split(logicConnector_and))// 2.4、再做逻辑与的分割
                                .filter(StringUtils::isNotBlank)
                                .map(ql -> this.parseAggregationQL(ql, fields))
                                .collect(Collectors.joining(" AND ", "(", ")")))
                .collect(Collectors.joining(" OR ", "(", ") AS satisfied"));
    }

    /**
     * 解析评级聚合语法
     *
     * @param aggQL
     * @param fields
     * @return
     */
    private String parseAggregationQL(String aggQL, List<String> fields) {
        if (StringUtils.isBlank(aggQL)) {
            throw new IllegalArgumentException("评级语法错误：" + aggQL);
        }
        String[] parts = aggQL.split(COLON_REGEX);
        if (parts.length != 2) {
            throw new IllegalArgumentException("评级语法错误：" + aggQL);
        }
        String formula = parts[1];
        Pattern pattern = Pattern.compile(COMPARISON_OPERATOR_REGEX);
        Matcher matcher = pattern.matcher(formula);
        if (!matcher.find()) {
            throw new IllegalArgumentException("评级语法错误：" + aggQL);
        }
        String field = formula.substring(0, matcher.start()).trim();
        String operator = matcher.group();
        String total = formula.substring(matcher.end()).trim();
        String function = mappedToFunction(parts[0].trim(), field);
        fields.add(field);
        return function + operator + total;
    }

    /**
     * 映射统计方法
     *
     * @param functionName
     * @param fieldName
     * @return
     */
    private String mappedToFunction(String functionName, String fieldName) {
        switch (functionName.toLowerCase()) {
            case "count":
                return "count(" + fieldName + ")";
            case "cardinality":
            case "distinct":
                return "count(DISTINCT " + fieldName + ")";
            case "avg":
                return "avg(" + fieldName + ")";
            case "sum":
                return "sum(" + fieldName + ")";
            case "max":
                return "max(" + fieldName + ")";
            case "min":
                return "min(" + fieldName + ")";
            default:
                return "count(1)";
        }
    }

    /**
     * 打印日志，根据配置打印计算过程
     *
     * @param message 日志内容，使用'{}'作为占位符，占位符与params参数对应
     * @param params  如果内容中有占位符，则根据位置用params替换
     */
    private void log(String message, Object... params) {
        log.info("资产评级日志：" + message, params);
    }

    /**
     * 打印错误日志，必须打印不可关闭
     *
     * @param message
     * @param cause
     */
    private void logError(String message, Throwable cause) {
        log.error(message, cause);
    }

}
