package com.dbapp.extension.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AIAnomalyAnalysisMapper {

    int deleteAiAnalysisData(@Param("modelId") String modelId);

    int saveAiAnalysisData(@Param("list") List<Map<String, Object>> dataList);

    int deleteAIAnalysisDataNotInModelIds(@Param("modelIds") List<String> modelIds);

}
