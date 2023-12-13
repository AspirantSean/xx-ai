package com.dbapp.extension.ai.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import lombok.extern.slf4j.Slf4j;
import com.dbapp.flexsdk.nativees.action.admin.indices.alias.IndicesAliasesRequest;
import com.dbapp.flexsdk.nativees.action.admin.indices.refresh.RefreshResponse;
import com.dbapp.flexsdk.nativees.action.admin.indices.settings.get.GetSettingsResponse;
import com.dbapp.flexsdk.nativees.action.admin.indices.settings.put.UpdateSettingsRequest;
import com.dbapp.flexsdk.nativees.action.bulk.BulkResponse;
import com.dbapp.flexsdk.nativees.action.delete.DeleteResponse;
import com.dbapp.flexsdk.nativees.action.get.GetResponse;
import com.dbapp.flexsdk.nativees.action.index.IndexResponse;
import com.dbapp.flexsdk.nativees.action.search.ClearScrollResponse;
import com.dbapp.flexsdk.nativees.action.search.SearchResponse;
import com.dbapp.flexsdk.nativees.action.update.UpdateResponse;
import com.dbapp.flexsdk.nativees.client.core.AcknowledgedResponse;
import com.dbapp.flexsdk.nativees.common.settings.Settings;
import com.dbapp.flexsdk.nativees.index.reindex.BulkByScrollResponse;
import com.dbapp.flexsdk.nativees.plugins.SearchPlugin;
import com.dbapp.flexsdk.nativees.script.Script;
import com.dbapp.flexsdk.nativees.search.SearchModule;
import com.dbapp.flexsdk.nativees.search.aggregations.Aggregation;
import com.dbapp.flexsdk.nativees.search.aggregations.ParsedAggregation;
import com.dbapp.flexsdk.nativees.search.builder.SearchSourceBuilder;
import com.dbapp.flexsdk.nativees.xcontent.ContextParser;
import com.dbapp.flexsdk.nativees.xcontent.DeprecationHandler;
import com.dbapp.flexsdk.nativees.xcontent.NamedXContentRegistry;
import com.dbapp.flexsdk.nativees.xcontent.ParseField;
import com.dbapp.flexsdk.nativees.xcontent.json.JsonXContentParser;
import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;


@Slf4j
@Component
public class FlexSdkParseUtils implements InitializingBean {
    private NamedXContentRegistry xContentRegistry;

    @Override
    public void afterPropertiesSet() throws Exception {
        Settings settings = Settings.builder().build();
        List<SearchPlugin> searchPlugins = new ArrayList<>();
        SearchModule searchModule = new SearchModule(settings, false, searchPlugins);

        List<NamedXContentRegistry.Entry> entries = new ArrayList<>(searchModule.getNamedXContents());
        //增加满足聚合查询信息的数据解析器
        entries.addAll(getAggregationXContentRegistry());

        xContentRegistry = new NamedXContentRegistry(entries);
    }

    /**
     * 把json转化为SearchResponse
     * @param jsonStr json字符串
     * @return SearchResponse
     * @throws IOException 异常
     */
    public SearchResponse convertJsonToSearchResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return SearchResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为SearchSourceBuilder
     * @param jsonStr json字符串
     * @return SearchSourceBuilder
     * @throws IOException 异常
     */
    public SearchSourceBuilder convertJsonToSearchSourceBuilder(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.parseXContent(jsonXContentParser);

        return searchSourceBuilder;
    }


    /**
     * 获取JsonXContentParser
     * @param jsonStr json字符串
     * @return JsonXContentParser
     * @throws IOException 异常
     */
    private JsonXContentParser getJsonXContentParser(String jsonStr) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(jsonStr);

        return new JsonXContentParser(xContentRegistry, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, parser);
    }



    /**
     * 获取满足聚合查询信息的数据解析器
     * @return 数据解析器
     */
    private List<NamedXContentRegistry.Entry> getAggregationXContentRegistry() {
        List<NamedXContentRegistry.Entry> entries = new ArrayList<>();

        List<String> packages = List.of("com.dbapp.flexsdk.nativees.search.aggregations");

        Set<Class<?>> parsedAggregationClasses = new HashSet<>();
        for(String p: packages) {
            //获取目标路径下继承了ParsedAggregation的所有类
            Reflections reflections = new Reflections(p);

            Queue<Class<?>> queue = new LinkedList<>();
            queue.add(ParsedAggregation.class);
            while (!queue.isEmpty()) {
                Class<?> superClass = queue.poll();
                Set<Class<?>> subClasses = (Set<Class<?>>) reflections.getSubTypesOf(superClass);
                for(Class<?> subClass: subClasses) {
                    if(Modifier.isAbstract(subClass.getModifiers())) {
                        //如果是抽象类，需要继续找其对应的子类
                        queue.add(subClass);
                    } else {
                        parsedAggregationClasses.add(subClass);
                    }
                }
            }
        }


        for(Class<?> clazz: parsedAggregationClasses) {
            try {
                NamedXContentRegistry.Entry entry = getAggregationNamedXContentRegistry(clazz);
                if(Objects.nonNull(entry)) {
                    entries.add(entry);
                }
            } catch (Exception e) {
                log.error("getAggregationNamedXContentRegistry error, class:{}", clazz.getName(), e);
            }
        }

        return entries;
    }


    /**
     * 根据类数据解析类对象，获取对应类型数据解析器
     * @param clazz 数据解析类对象
     * @return 数据解析器
     */
    private NamedXContentRegistry.Entry getAggregationNamedXContentRegistry(Class<?> clazz) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Constructor<?> constructor1 = clazz.getDeclaredConstructor();
        Aggregation aggregation = (Aggregation)constructor1.newInstance();

        Method[] methods = clazz.getMethods();

        for(Method method: methods) {
            //每个类型数据解析器转换数据的方法名 fromXContent
            if(method.getName().equals("fromXContent")) {
                ContextParser<Object, ? extends Aggregation> contextParser =
                        (p, c) -> {
                            try {
                                return (Aggregation) method.invoke(aggregation, p, c);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        };
                return new NamedXContentRegistry.Entry(Aggregation.class, new ParseField(aggregation.getType()), contextParser);
            }
        }

        return null;
    }

    /**
     * 把SearchResponse转化成json字符串
     * @param searchResponse es响应结果
     * @return json字符串
     * @throws IOException 异常
     */
    public String parseSearchResponseToJson(SearchResponse searchResponse) throws IOException {
        return searchResponse.toString();
    }


    /**
     * 把json转化为BulkResponse
     * @param jsonStr json字符串
     * @return BulkResponse
     * @throws IOException 异常
     */
    public BulkResponse convertJsonToBulkResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return BulkResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为DeleteResponse
     * @param jsonStr json字符串
     * @return DeleteResponse
     * @throws IOException 异常
     */
    public DeleteResponse convertJsonToDeleteResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return DeleteResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为UpdateResponse
     * @param jsonStr json字符串
     * @return UpdateResponse
     * @throws IOException 异常
     */
    public UpdateResponse convertJsonToUpdateResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return UpdateResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为IndexResponse
     * @param jsonStr json字符串
     * @return IndexResponse
     * @throws IOException 异常
     */
    public IndexResponse convertJsonToIndexResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return IndexResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为GetResponse
     * @param jsonStr json字符串
     * @return GetResponse
     * @throws IOException 异常
     */
    public GetResponse convertJsonToGetResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return GetResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为GetSettingsResponse
     * @param jsonStr json字符串
     * @return GetSettingsResponse
     * @throws IOException 异常
     */
    public GetSettingsResponse convertJsonToGetSettingsResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return GetSettingsResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为Script
     * @param jsonStr json字符串
     * @return Script
     * @throws IOException 异常
     */
    public Script convertJsonToScript(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return Script.parse(jsonXContentParser);
    }

    /**
     * 把json转化为BulkByScrollResponse
     * @param jsonStr json字符串
     * @return BulkByScrollResponse
     * @throws IOException 异常
     */
    public BulkByScrollResponse convertJsonToBulkByScrollResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return BulkByScrollResponse.fromXContent(jsonXContentParser);
    }

    /**
     * 把json转化为ClearScrollResponse
     * @param jsonStr json字符串
     * @return ClearScrollResponse
     * @throws IOException 异常
     */
    public ClearScrollResponse convertJsonToClearScrollResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return ClearScrollResponse.fromXContent(jsonXContentParser);
    }

    /**
     * AcknowledgedResponse
     * @param jsonStr json字符串
     * @return AcknowledgedResponse
     * @throws IOException 异常
     */
    public AcknowledgedResponse convertJsonToAcknowledgedResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return AcknowledgedResponse.fromXContent(jsonXContentParser);
    }

    /**
     * AcknowledgedResponse
     * @param jsonStr json字符串
     * @return AcknowledgedResponse
     * @throws IOException 异常
     */
    public com.dbapp.flexsdk.nativees.action.support.master.AcknowledgedResponse convertJsonToAcknowledgedResponseX(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return com.dbapp.flexsdk.nativees.action.support.master.AcknowledgedResponse.fromXContent(jsonXContentParser);
    }

    /**
     * IndicesAliasesRequest
     * @param jsonStr json字符串
     * @return IndicesAliasesRequest
     * @throws IOException 异常
     */
    public IndicesAliasesRequest convertJsonToIndicesAliasesRequest(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return IndicesAliasesRequest.fromXContent(jsonXContentParser);
    }

    /**
     * UpdateSettingsRequest
     * @param jsonStr json字符串
     * @return UpdateSettingsRequest
     * @throws IOException 异常
     */
    public UpdateSettingsRequest convertJsonToUpdateSettingsRequest(String jsonStr, List<String> indices) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(indices.toArray(new String[indices.size()]));
        return updateSettingsRequest.fromXContent(jsonXContentParser);
    }

    /**
     * RefreshResponse
     * @param jsonStr json字符串
     * @return RefreshResponse
     * @throws IOException 异常
     */
    public RefreshResponse convertJsonToRefreshResponse(String jsonStr) throws IOException {
        JsonXContentParser jsonXContentParser = getJsonXContentParser(jsonStr);
        return RefreshResponse.fromXContent(jsonXContentParser);
    }

}

