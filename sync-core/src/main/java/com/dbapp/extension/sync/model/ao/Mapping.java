package com.dbapp.extension.sync.model.ao;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.dbapp.extension.sync.enums.RelationType;
import lombok.Data;
import org.postgresql.util.PGobject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Mapping {
    /**
     * 主键，包含路径
     */
    private String key;
    /**
     * 主键别名，elasticsearch中存储的名字
     */
    private String keyAlias;
    /**
     * key=field; value=rename
     */
    private Map<String, String> map;
    /**
     * 子属性映射
     */
    private Map<String, Mapping> mappings;
    /**
     * 关联关系
     */
    private RelationType relationType;


    public LinkedList<Map<String, Object>> mapping(List<Map<String, Object>> rows) {
        return rows.stream()
                .filter(row -> row.get(key) != null)
                .collect(Collectors.groupingBy(row -> row.get(key)))
                .values()
                .stream()
                .filter(CollUtil::isNotEmpty)
                .map(rowList -> {
                    Map<String, Object> datum = new HashMap<>();
                    Map<String, Object> row = rowList.get(0);
                    // 本级数据
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        Object value = row.get(entry.getKey());
                        datum.put(entry.getValue(),
                                value instanceof PGobject
                                        ? JSON.parseObject(((PGobject) value).getValue())
                                        : value);
                    }
                    // 子级数据
                    if (CollUtil.isNotEmpty(mappings))
                        for (Map.Entry<String, Mapping> entry : mappings.entrySet()) {
                            Mapping subMapping = entry.getValue();
                            LinkedList<Map<String, Object>> subDatum = subMapping.mapping(rowList);
                            if (RelationType.one_to_one == subMapping.relationType) {
                                datum.put(entry.getKey(), subDatum.peek());
                            } else if (RelationType.one_to_many == subMapping.relationType) {
                                datum.put(entry.getKey(), subDatum);
                            }
                        }
                    return datum;
                })
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
