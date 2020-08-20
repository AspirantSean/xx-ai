package com.dbapp.model.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchModule;

import java.io.IOException;
import java.util.Collections;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricInfo {
    @ApiParam("指标")
    private Metric[] metricInfo;
    @ApiParam("查询条件，新老索引兼容后的boolQueryBuilder")
    private BoolQueryBuilder query;

    public void setQuery(String query) {
        try {
            SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
            XContentParser contentParser = XContentFactory.xContent(XContentType.JSON)
                    .createParser(new NamedXContentRegistry(searchModule.getNamedXContents()), null, query);
            this.query = (BoolQueryBuilder) AbstractQueryBuilder.parseInnerQueryBuilder(contentParser);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metric {
        @ApiParam("索引")
        private String index;
        @ApiParam("算子")
        private String action;
        @ApiParam("聚合字段")
        private String aliasName;
        @ApiParam("间隔")
        private int window;
        @ApiParam("单位")
        private String timeUnit;
    }
}
