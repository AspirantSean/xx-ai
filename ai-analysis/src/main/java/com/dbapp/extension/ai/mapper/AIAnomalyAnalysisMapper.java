package com.dbapp.extension.ai.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AIAnomalyAnalysisMapper {

    @Delete({
            "delete from ailpha_aiAnalysis_data where modelId = #{modelId} ",
    })
    int deleteAiAnalysisData(@Param("modelId") String modelId);

    @Insert({
            "<script>",
            "   insert into ailpha_aiAnalysis_data (id, originalData, uiData, createTime, modelId, algorithmId) ",
            "   value ",
            "   <foreach collection='list' item='data' index='index' separator=', '> ",
            "       ( #{data.id, jdbcType=VARCHAR}, #{data.originalData, jdbcType=LONGVARCHAR}, ",
            "       #{data.uiData, jdbcType=LONGVARCHAR}, #{data.createTime, jdbcType=BIGINT}, ",
            "       #{data.modelId, jdbcType=VARCHAR}, #{data.algorithmId, jdbcType=VARCHAR} ) ",
            "   </foreach> ",
            "</script>"
    })
    int saveAiAnalysisData(@Param("list") List<Map<String, Object>> dataList);

    @Delete({
            "<script>",
            "   DELETE FROM ailpha_aiAnalysis_data",
            "   WHERE 1=1 ",
            "   <if test='modelIds != null and modelIds.size() > 0'>",
            "       AND modelId NOT IN ",
            "       <foreach collection='modelIds' item='modelId' index='index' open='(' close=')' separator=','> ",
            "           #{modelId} ",
            "       </foreach> ",
            "   </if>",
            "</script>"
    })
    int deleteAIAnalysisDataNotInModelIds(@Param("modelIds") List<String> modelIds);

}
