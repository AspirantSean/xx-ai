package com.dbapp.extension.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiModelMapper {

    List<Map<String, Object>> listAiAnalysisAlgorithm(@Param("list") List<String> algorithmIds);

    Map<String, Object> findAISceneAndAlgorithmBySceneId(@Param("sceneId") String sceneId);

    List<Map<String, Object>> findSceneBySceneIds(@Param("list") List<String> sceneIds);

    Map<String, Object> findSceneBySceneId(@Param("sceneId") String sceneId);

    List<Map<String, Object>> listAiScene();

    int clearAiScene();

    int saveOrUpdateScene(@Param("list") List<Map<String, Object>> sceneList);

    Map<String, Object> findAiAnalysisDataByModelIdAndAlgorithmId(@Param("modelId") String modelId,
                                                                  @Param("algorithmId") String algorithmId);

}