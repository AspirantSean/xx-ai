package com.dbapp.extension.ai.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbapp.extension.ai.api.service.IBigdataService;
import com.dbapp.extension.ai.baas.dto.entity.model.ai.ModelAI;
import com.dbapp.extension.ai.mapper.AiModelMapper;
import com.dbapp.extension.ai.utils.SystemProperUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 数据处理辅助类
 */
public class DataProcessRunable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DataProcessRunable.class);

    /**
     * 算法配置
     */
    private static final String algorithm_conf_path = SystemProperUtil.getResourcesPath() +
            SystemProperUtil.getFileSeparator() + "ai_model_ailpha" +
            SystemProperUtil.getFileSeparator() + "config" +
            SystemProperUtil.getFileSeparator() + "ai_algorithm.properties";

    private IBigdataService iBigdataService;

    /**
     * 数据库返回数据集合
     */
    private Map<String, Object> map;
    /**
     * 模型id
     */
    private String modelId;
    /**
     * 算法id
     */
    private String algorithmId;
    /**
     * 场景号
     */
    private Integer sceneNo;
    /**
     * 数据处理结果集合
     */
    private JSONObject analysisData;
    /**
     * 起始时间
     */
    private String startTime;
    /**
     * 截止时间
     */
    private String endTime;
    /**
     * 时间间隔(单位/m)
     */
    private Integer interval = 10;

    /**
     * 构造器
     *
     * @param map          当前数据处理所在场景的modelId, algorithmId
     * @param analysisData 场景返回数据集
     * @param startTime    起始时间
     * @param endTime      截止时间
     */
    public DataProcessRunable(Map<String, Object> map, JSONObject analysisData, AiModelMapper aiModelMapper, IBigdataService iBigdataService, String startTime, String endTime) {
        this.modelId = (String) map.get("modelId");
        this.algorithmId = (String) map.get("algorithmId");
        this.sceneNo = (Integer) map.get("sceneNo");
        this.map = aiModelMapper.findAiAnalysisDataByModelIdAndAlgorithmId(this.modelId, this.algorithmId);
        this.analysisData = analysisData;
        this.startTime = startTime;
        this.endTime = endTime;
        this.iBigdataService = iBigdataService;
    }

    @Override
    public void run() {
        LOG.info("{}-正在处理ai异常数据", Thread.currentThread().getName());
        this.process();
    }

    /**
     * 数据处理方法
     */
    private void process() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject result = new JSONObject();
        JSONObject uiData = null;
        try {
            ModelAI aiModel = iBigdataService.getModelMetric(this.modelId);
            String name = aiModel.getRuleName();
            if (StringUtils.isBlank(name)) {
                name = aiModel.getRuleId();
            }
            result.put("name", name + "-" + this.algorithmId);
            result.put("algorithmId", this.algorithmId);
            result.put("timestampData", null);

            if (map != null && StringUtils.isNotBlank((String) map.get("uiData"))) {
                uiData = JSON.parseObject((String) map.get("uiData"));
            }
            // 补全数据并处理
            boolean isExist = supplementDataFromStartToEnd(uiData);
            if (!isExist) {
                analysisData.put("scene" + this.sceneNo, result);
                return;
            }
            // 异常点
            JSONArray anomalies = (JSONArray) uiData.remove("anomalies");
            JSONArray anomaliesResult = new JSONArray();
            Calendar cal_start = Calendar.getInstance();
            cal_start.setTime(sdf.parse(this.startTime));
            Calendar cal_end = Calendar.getInstance();
            cal_end.setTime(sdf.parse(this.endTime));
            if (anomalies != null && !anomalies.isEmpty()) {
                boolean isWeeklyGaussianEstimation = "WeeklyGaussianEstimation".equals(this.algorithmId);
                if (isWeeklyGaussianEstimation) {
                    cal_start.setTime(cal_end.getTime());
                    cal_start.add(Calendar.DATE, -6);// 往前推6天, 算上本日就是7天
                    cal_start.set(Calendar.HOUR_OF_DAY, 0);// 时设置为00
                    cal_start.set(Calendar.MINUTE, 0);// 分设置为00
                    cal_start.set(Calendar.SECOND, 0);// 秒设置为00
                    cal_start.set(Calendar.MILLISECOND, 0);// 毫秒设置为0000
                }
                dealAnomalies(cal_start, cal_end, anomalies, anomaliesResult, isWeeklyGaussianEstimation);
            }
            uiData.put("anomalies", anomaliesResult);
            uiData.put("name", name + "-" + this.algorithmId);
            // 返回数据
            analysisData.put("scene" + this.sceneNo, uiData);

        } catch (Exception e) {
            if (uiData == null) {
                result.put("timestampData", null);
            } else {
                result = uiData;
                result.put("suitable", false);
            }
            analysisData.put("scene" + this.sceneNo, result);
            LOG.error("****************场景结果计算异常:\n {}", getErrorTrace(e));
        }
    }

    /**
     * 异常点处理
     *
     * @param start
     * @param end
     * @param anomalies
     * @param anomaliesResult
     */
    public void dealAnomalies(Calendar start, Calendar end, JSONArray anomalies, JSONArray anomaliesResult, boolean isWeeklyGaussianEstimation) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (anomalies != null && !anomalies.isEmpty()) {
            anomalies.forEach(anomaly -> {
                try {
                    Date timestamp = sdf.parse(((JSONObject) anomaly).getString("timestamp"));
                    if (timestamp.getTime() >= start.getTimeInMillis()
                            && timestamp.getTime() <= end.getTimeInMillis())// 小于等于开始时间且大于等于结束时间的保留
                    {
                        anomaliesResult.add(anomaly);
                    }
                } catch (Exception e) {
                    LOG.error("[com.dbapp.mirror.service.impl.AnomaliesAnalysisServiceImpl]:异常点处理异常\n {}", getErrorTrace(e));
                }
            });
        }
        if (isWeeklyGaussianEstimation) {
            Calendar monday = Calendar.getInstance();
            monday.setTime(end.getTime());
            int day_of_week = monday.get(Calendar.DAY_OF_WEEK);
            if (day_of_week == Calendar.SUNDAY)// 如果是周日, 减一天, 外国以周日为一周起始
            {
                monday.add(Calendar.DATE, -1);
            }
            monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// 日设置为周一
            monday.set(Calendar.HOUR_OF_DAY, 0);// 时设置为00
            monday.set(Calendar.MINUTE, 0);// 分设置为00
            monday.set(Calendar.SECOND, 0);// 秒设置为00
            monday.set(Calendar.MILLISECOND, 0);// 毫秒设置为0000
            JSONArray anomalies0 = new JSONArray();
            JSONArray anomalies1 = new JSONArray();
            anomaliesResult.forEach(anomaly -> {
                try {
                    Date timestamp = sdf.parse(((JSONObject) anomaly).getString("timestamp"));
                    if (timestamp.getTime() < monday.getTimeInMillis()) {
                        anomalies0.add(anomaly);
                    } else {
                        anomalies1.add(anomaly);
                    }
                } catch (ParseException e) {
                    LOG.error("[com.dbapp.mirror.service.impl.AnomaliesAnalysisServiceImpl]:异常点处理异常\n {}", getErrorTrace(e));
                }
            });
            anomaliesResult.clear();
            anomaliesResult.add(anomalies0);
            anomaliesResult.add(anomalies1);
        }
    }

    /**
     * 根据选取时间截取或补充数据
     * *************当前数据时间间隔为10min不变**************
     *
     * @param uiData 原始数据集
     * @return true-处理完成; false-数据不存在
     * @throws ParseException
     */
    private boolean supplementDataFromStartToEnd(JSONObject uiData) throws ParseException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (uiData == null) {
            return false;
        }
        JSONArray timestampData = (JSONArray) uiData.remove("timestampData");
        if (timestampData == null || timestampData.size() < 1) {
            return false;
        }
        JSONArray realData = (JSONArray) uiData.remove("realData");
        JSONArray fitMaxData = (JSONArray) uiData.remove("fitMaxData");
        JSONArray fitMinData = (JSONArray) uiData.remove("fitMinData");
        JSONArray referenceData = (JSONArray) uiData.remove("referenceData");
        JSONArray levelData = (JSONArray) uiData.remove("levelData");

        Calendar cal_start = Calendar.getInstance();
        cal_start.setTime(sdf.parse(timestampData.getString(0)));
        Date startTime = sdf.parse(this.startTime);

        Calendar cal_end = Calendar.getInstance();
        cal_end.setTime(sdf.parse(timestampData.getString(timestampData.size() - 1)));
        Date endTime = sdf.parse(this.endTime);

        /*
         * 1、原始数据的截止时间 比 查询起始时间早 则 返回无数据;
         * 2、原始数据的起始时间 比 查询截止时间晚 则 返回无数据。
         */
        if (startTime.after(cal_end.getTime()) || endTime.before(cal_start.getTime())) {
            return false;
        }

        // 处理起始时间
        int start_index = 0;
        List<Object> start_list = new ArrayList<>();
        List<Object> start_nan_list = new ArrayList<>();
        for (; cal_start.getTime().after(startTime); ) {// 首个时间晚于起始时间
            cal_start.add(Calendar.MINUTE, -this.interval);// 减去一个时间间隔后放入数组
            start_list.add(sdf.format(cal_start.getTime()));
            start_nan_list.add("NaN");
        }
        Collections.reverse(start_list);// important
        for (; cal_start.getTime().before(startTime) && start_index < start_list.size() + timestampData.size() - 1; ) {
            start_index++;
            cal_start.add(Calendar.MINUTE, this.interval);// 加上一个时间间隔再比较
        }
        // 处理截止时间
        List<Object> end_list = new ArrayList<>();
        List<Object> end_nan_list = new ArrayList<>();
        for (; cal_end.getTime().before(endTime); ) {// 末时间早于截止时间
            cal_end.add(Calendar.MINUTE, this.interval);// 加上一个时间间隔后放入数组
            end_list.add(sdf.format(cal_end.getTime()));
            end_nan_list.add("NaN");
        }
        int end_index = start_list.size() + end_list.size() + timestampData.size() - 1;
        for (; cal_end.getTime().after(endTime) && end_index > 0; ) {
            end_index--;
            cal_end.add(Calendar.MINUTE, -this.interval);
        }
        // 增删数据
        putIntoUiData(uiData, "timestampData", timestampData, start_index, end_index + 1, start_list, end_list);
        putIntoUiData(uiData, "realData", realData, start_index, end_index + 1, start_nan_list, end_nan_list);
        putIntoUiData(uiData, "fitMaxData", fitMaxData, start_index, end_index + 1, start_nan_list, end_nan_list);
        putIntoUiData(uiData, "fitMinData", fitMinData, start_index, end_index + 1, start_nan_list, end_nan_list);
        putIntoUiData(uiData, "referenceData", referenceData, start_index, end_index + 1, start_nan_list, end_nan_list);
        putIntoUiData(uiData, "levelData", levelData, start_index, end_index + 1, start_nan_list, end_nan_list);
        // 误差范围
        dealReferenceDataInfinity(uiData);
        // 处理levelData中时间粒度
        dealLevelData(uiData);
        // 处理weekly...算法的数据
        if ("WeeklyGaussianEstimation".equals(((String) map.get("algorithmId")).replace(" ", ""))) {
            return dealWeeklyGaussianEstimationData(uiData);
        }

        return true;
    }

    /**
     * 将数据放入uiData
     *
     * @param uiData    数据集
     * @param name      key
     * @param data      处理数据集
     * @param start     起始位置(含)
     * @param end       结束位置(不含)
     * @param startList 起始位置新增集合
     * @param endList   结束位置新增集合
     */
    private void putIntoUiData(
            JSONObject uiData, String name, JSONArray data, int start, int end, List<Object> startList, List<Object> endList) {
        if (data == null) {
            return;
        }
        data = new JSONArray(this.appendList(this.appendList(startList, data), endList).subList(start, end));
        uiData.put(name, data);
    }

    /**
     * 处理无限大值
     */
    public void dealReferenceDataInfinity(JSONObject uiData) throws IOException {
        Properties algorithm_prop = new Properties();
        algorithm_prop.load(new FileInputStream(algorithm_conf_path));
        String key = this.algorithmId + ".threshold";
        String threshold = algorithm_prop.getProperty(key, "WeeklyGaussianEstimation".equals(this.algorithmId) ? "[0,10]" : "[0,1]");
        JSONArray range = JSON.parseArray(threshold);
        uiData.put("minValue", range.getDoubleValue(0));
        uiData.put("maxValue", range.getDoubleValue(1));
    }

    /**
     * 聚合泳道图时间粒度
     *
     * @param uiData
     */
    private void dealLevelData(JSONObject uiData) {
        JSONArray timestampData = uiData.getJSONArray("timestampData");
        JSONArray levelData = uiData.getJSONArray("levelData");

        if (levelData == null || levelData.size() < 1) {
            return;
        }

        double interval = Math.ceil(timestampData.size() / 45.0);

        JSONObject new_levelData = new JSONObject();
        new_levelData.put("timestampData", new JSONArray());
        new_levelData.put("levelData", new JSONArray());

        for (int i = 0; i < timestampData.size(); i++) {
            if (i % interval == 0) {
                new_levelData.getJSONArray("timestampData").add(timestampData.get(i));
                int averageLevel = 0;// 等级均值
                int count_of_str = 0;// 统计每个时间间隔内字符串的个数
                int j = i;
                for (; j < i + interval && j < levelData.size(); j++) {
                    Object level = levelData.get(j);
                    if (!(level instanceof Number)) {// levelData非数值，如"NaN"等
                        count_of_str++;
                        continue;
                    }
                    averageLevel += levelData.getInteger(j);
                }
                if (count_of_str >= interval) {// 全为字符串
                    new_levelData.getJSONArray("levelData").add("NaN");
                } else {
                    averageLevel = (int) Math.ceil(averageLevel / (j - i + 1.0 - count_of_str));// 计算平均值
                    new_levelData.getJSONArray("levelData").add(averageLevel);
                }
            }
        }
        LOG.debug("{} {} AI analysis: levelData=\n {}\ndeal result=\n {}", this.modelId, this.algorithmId, levelData, new_levelData);
        uiData.put("levelData", new_levelData);
    }

    /**
     * 处理周期分析数据
     *
     * @param uiData
     * @throws ParseException
     */
    private boolean dealWeeklyGaussianEstimationData(JSONObject uiData) throws ParseException {
        JSONArray timestampData = uiData.getJSONArray("timestampData");
        if (timestampData.size() < Math.ceil(1.0 * 7 * 24 * 60 / this.interval))// WeeklyGaussianEstimation的数据如果少于一周则不展示
        {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray originalRealData = uiData.getJSONArray("realData");
        JSONArray realData = new JSONArray();// 真实数据
        JSONObject week_data = new JSONObject();// 周数据
        week_data.put("xAxis", new JSONArray());
        week_data.put("yAxis", new JSONArray());
        for (int i = 0; i < timestampData.size(); i++) {// 遍历时间节点
            String timestamp = timestampData.getString(i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(timestamp));
            week_data.getJSONArray("xAxis").add(timestamp);// 时间轴
            week_data.getJSONArray("yAxis").add(originalRealData.get(i));// value
            long compare_num = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60L + calendar.get(Calendar.MINUTE) * 60L + calendar.get(Calendar.SECOND);
            // 当day_of_week为周日且为最后一个时间点时 或者 当i取到最后一个数时，将周数据数组放入真实数据数组，并周数据数组变量指向新对象
            if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && compare_num >= 24 * 60 * 60 - this.interval * 60) || i == timestampData.size() - 1) {
                realData.add(week_data);
                week_data = new JSONObject();// 周数据
                week_data.put("xAxis", new JSONArray());
                week_data.put("yAxis", new JSONArray());
            }
        }
        // 补全数据
        fillAWeek(realData);
        uiData.put("realData", realData);
        // 只取一周的数据
        int size = 7 * 24 * 60 / this.interval;
        Calendar calendar = Calendar.getInstance();
        long compare_num;
        int startIndex = timestampData.size() - size;
        int firstIndex = startIndex;
        for (; firstIndex < timestampData.size(); firstIndex++) {
            String timestamp = timestampData.getString(firstIndex);
            calendar.setTime(sdf.parse(timestamp));
            compare_num = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60L + calendar.get(Calendar.SECOND);
            // 是周一且为周一第一个时间点数据
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && compare_num < this.interval * 60) {
                break;
            }
        }
        JSONArray originalFitMaxData = uiData.getJSONArray("fitMaxData");
        JSONArray originalFitMinData = uiData.getJSONArray("fitMinData");
        List<Object> recent_week =
                appendList(timestampData.subList(firstIndex, timestampData.size()), timestampData.subList(startIndex, firstIndex));
        List<Object> recent_week_fitMaxData =
                appendList(originalFitMaxData.subList(firstIndex, originalFitMaxData.size()), originalFitMaxData.subList(startIndex, firstIndex));
        List<Object> recent_week_fitMinData =
                appendList(originalFitMinData.subList(firstIndex, originalFitMinData.size()), originalFitMinData.subList(startIndex, firstIndex));
        JSONObject maxData = new JSONObject();
        maxData.put("xAxis", recent_week);
        maxData.put("yAxis", recent_week_fitMaxData);
        JSONObject minData = new JSONObject();
        minData.put("xAxis", recent_week);
        minData.put("yAxis", recent_week_fitMinData);
        uiData.put("fitMaxData", maxData);
        uiData.put("fitMinData", minData);
        return true;
    }

    /**
     * 在originalList后添加appendList组成新的List，原集合不改变
     *
     * @param originalList 原始集合
     * @param appendList   追加集合
     * @param <T>
     * @return 返回新集合
     */
    private <T> List<T> appendList(List<T> originalList, List<T> appendList) {
        List<T> new_List = new ArrayList<>(originalList);
        new_List.addAll(appendList);
        return new_List;
    }

    /**
     * 补全数据首尾周数据
     *
     * @param realData
     * @throws ParseException
     */
    private void fillAWeek(JSONArray realData) throws ParseException {
        // 补全第一周前半周
        JSONObject first_week_data = realData.getJSONObject(0);
        if (first_week_data == null || first_week_data.size() < 1)// 第一周不存在则退出
        {
            return;
        }
        JSONArray timestampData = first_week_data.getJSONArray("xAxis");
        String timestamp = timestampData.getString(0);// 首位的时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse(timestamp));// 第一个时间点
        long compare_num = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60L + calendar.get(Calendar.SECOND);
        // 不是星期一或不是一天的第一个时间点则说明缺数据
        for (; calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY || compare_num >= this.interval * 60L; ) {
            calendar.add(Calendar.MINUTE, -this.interval);
            compare_num = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60L + calendar.get(Calendar.SECOND);
            timestampData.add(0, sdf.format(calendar.getTime()));
            first_week_data.getJSONArray("yAxis").add(0, "NaN");
        }
        // 补全最后一周后半周
        JSONObject last_week_data = realData.getJSONObject(realData.size() - 1);
        if (last_week_data == null || last_week_data.size() < 1)// 最后一周不存在则退出
        {
            return;
        }
        timestampData = last_week_data.getJSONArray("xAxis");
        timestamp = timestampData.getString(timestampData.size() - 1);// 末位时间
        calendar.setTime(sdf.parse(timestamp));// 最后一个时间点
        compare_num = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60L + calendar.get(Calendar.MINUTE) * 60L + calendar.get(Calendar.SECOND);
        // 不是星期日或不是一天的最后一个时间点则说明缺数据
        for (; calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY || compare_num < 24 * 60 * 60L - this.interval * 60; ) {
            calendar.add(Calendar.MINUTE, this.interval);
            compare_num = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60L + calendar.get(Calendar.MINUTE) * 60L + calendar.get(Calendar.SECOND);
            timestampData.add(sdf.format(calendar.getTime()));
            last_week_data.getJSONArray("yAxis").add("NaN");
        }
    }

    /**
     * 打印exception堆栈信息
     *
     * @param t
     * @return
     */
    public static String getErrorTrace(Throwable t) {
        String errorTrace = "";
        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            t.printStackTrace(writer);
            writer.flush();
            errorTrace = stringWriter.getBuffer().toString();
            writer.close();
            stringWriter.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }
        return errorTrace;
    }
}
