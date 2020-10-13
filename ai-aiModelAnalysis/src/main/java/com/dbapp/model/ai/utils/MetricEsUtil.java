package com.dbapp.model.ai.utils;

import com.dbapp.app.mirror.dto.MetricInfo;
import com.dbapp.es.IEsService;
import com.google.common.collect.ImmutableMap;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public final class MetricEsUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MetricEsUtil.class);

    @Resource
    private IEsService iEsService;

    /**
     * @param metricInfo 指标信息
     * @return 聚合结果
     */
    public List<Map<String, Object>> metricHistogramSubCountList(MetricInfo metricInfo) {
        AggregationBuilder aggregationBuilder;
        String action = metricInfo.getMetricInfo()[0].getAction();
        String aggField = metricInfo.getMetricInfo()[0].getAliasName();
        switch (action) {
            case "avg":
                aggregationBuilder = AggregationBuilders.avg(action).field(aggField).missing(0.0);
                break;
            case "max":
                aggregationBuilder = AggregationBuilders.max(action).field(aggField).missing(0.0);
                break;
            case "min":
                aggregationBuilder = AggregationBuilders.min(action).field(aggField).missing(0.0);
                break;
            default:
                aggregationBuilder = AggregationBuilders.sum(action).field(aggField).missing(0.0);
        }
        SearchRequest searchRequest = new SearchRequest()
                .indices(metricInfo.getMetricInfo()[0].getIndex().split(","))
                .source(new SearchSourceBuilder()
                        .size(0)
                        .query(metricInfo.getQuery())
                        .aggregation(AggregationBuilders.dateHistogram("dateHistogram")
                                .field("@timestamp")
                                .dateHistogramInterval(DateHistogramInterval.minutes(10))// 暂时默认写死为10min聚合一次，以后改为metricInfo.getMetric().getWindow()动态调整
                                .timeZone(DateTimeZone.getDefault())
                                .minDocCount(0)
                                .subAggregation(aggregationBuilder)));
        LOG.info("Metric histogram search request: {}", searchRequest);
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            SearchResponse searchResponse = iEsService.search(searchRequest);
            ParsedDateHistogram dateHistogram = searchResponse.getAggregations().get("dateHistogram");
            if (dateHistogram == null) {
                return result;
            }
            dateHistogram.getBuckets().forEach(
                    bucket -> result.add(ImmutableMap.of(
                            "time", bucket.getKeyAsString(),
                            "value", ((NumericMetricsAggregation.SingleValue) bucket.getAggregations().get(action)).value())));
        } catch (Exception e) {
            LOG.error("指标数据查询异常", e);
        }
        return result;
    }
}
