package com.dbapp.extension.sync.model.ao;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dbapp.extension.sync.enums.DatabaseType;
import com.dbapp.extension.sync.util.SystemProperUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConfig {
    /**
     * 来源数据库类型
     */
    private DatabaseType sourceDatabaseType = DatabaseType.Postgresql;
    /**
     * 目标数据库类型
     */
    private DatabaseType targetDatabaseType = DatabaseType.Elasticsearch;
    /**
     * 数据库名
     */
    private String database;
    /**
     * 索引名
     */
    private String index;
    /**
     * 表属性配置
     */
    private List<SyncTableConfig> nodes;

    public Map<String, Object> template(SyncTableConfig syncTableConfig) {
        JSONObject template = new JSONObject();
        try {
            String templateSample = FileUtils.readFileToString(SystemProperUtil.getResourcesAsFile("sync" + SystemProperUtil.getFileSeparator() + "template_sample.json"), Charset.defaultCharset());
            if (StringUtils.isNotBlank(templateSample)) {
                template = JSON.parseObject(templateSample);
            }
        } catch (IOException e) {
            log.error("解析es template模板文件失败", e);
        }
        template.put("index_patterns", Collections.singletonList(index));
        Map<String, Object> mappings = template.getJSONObject("mappings");
        if (mappings == null) {
            mappings = new JSONObject();
        }
        mappings.put("dynamic", true);
        Map<String, Object> tableTemplate = syncTableConfig.template();
        if (CollUtil.isNotEmpty(tableTemplate)) {
            mappings.putAll(tableTemplate);
        }
        template.put("mappings", mappings);
        JSONObject settings;
        if (CollUtil.isEmpty(settings = template.getJSONObject("settings")) || !settings.containsKey("index")) {
            settings.put("index", JSON.parseObject("{\"indexing\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"index\":{\"info\":\"5s\"}}}},\"lifecycle\":{\"name\":\"common-policy-shard-1\",\"rollover_alias\":\"ailpha-securitylog-flow-20230512\"},\"mapping\":{\"ignore_malformed\":\"true\"},\"merge\":{\"policy\":{\"floor_segment\":\"10mb\",\"max_merge_at_once\":\"30\",\"max_merged_segment\":\"1gb\",\"segments_per_tier\":\"60\"},\"scheduler\":{\"auto_throttle\":\"true\",\"max_merge_count\":\"100\",\"max_thread_count\":\"1\"}},\"number_of_replicas\":\"0\",\"number_of_shards\":\"1\",\"refresh_interval\":\"60s\",\"search\":{\"slowlog\":{\"level\":\"info\",\"threshold\":{\"fetch\":{\"info\":\"5s\"},\"query\":{\"info\":\"10s\"}}}},\"translog\":{\"durability\":\"async\",\"flush_threshold_size\":\"1024mb\",\"generation_threshold_size\":\"128mb\",\"retention\":{\"age\":\"1h\",\"size\":\"32mb\"}},\"unassigned\":{\"node_left\":{\"delayed_timeout\":\"10m\"}}}"));
        }
        return template;
    }

}
