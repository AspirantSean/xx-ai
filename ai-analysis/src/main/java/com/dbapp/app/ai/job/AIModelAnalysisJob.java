package com.dbapp.app.ai.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbapp.app.ai.api.service.IBigdataService;
import com.dbapp.app.ai.management.runtime.process.AIModelProcess;
import com.dbapp.app.ai.mapper.AIAnomalyAnalysisMapper;
import com.dbapp.app.ai.utils.MetricEsUtil;
import com.dbapp.app.ai.utils.GlobalAttribute;
import com.dbapp.app.ai.utils.SystemProperUtil;
import com.dbapp.app.mirror.dto.MetricInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
public class AIModelAnalysisJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(AIModelAnalysisJob.class);

    /*
     * 文件/目录路径
     */
    private static final String ALGORITHM_CONF_PATH = String.format("%s/ai_model_ailpha/config/ai_algorithm.properties", SystemProperUtil.getResourcesPath());
    private static final String PYTHON_PATH = String.format("%s/ai_model_ailpha/Analysis.py", SystemProperUtil.getResourcesPath());
    private static final String CACHE_PATH = GlobalAttribute.getPropertyString("tmp.dir", "/data/tmp");

    @Resource
    private AIAnomalyAnalysisMapper aiAnomalyAnalysisMapper;
    @Resource
    private IBigdataService iBigdataService;
    @Resource
    private MetricEsUtil metricEsUtil;

    /**********无需每次任务重新获取**********/
    private AIModelProcess aiModelProcess;
    private MetricInfo metricInfo;
    private String pythonDataCache;// 算法缓存目录
    /**********每次任务需要重新获取**********/
    private List<Map<String, Object>> originalMetricData;// 指标数据
    private Properties algorithmProperties;// 算法配置
    private List<String> hasAnalysedAlgorithm = new ArrayList<>();// 分析过的算法
    private long createTime;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        beforeAnalysis(jobExecutionContext);// 1、准备数据
        analysis();// 2、分析指标数据
        processData();// 3、分析结果数据转换并入库
    }


    /**
     * 准备工作数据，若获取过模型和指标数据，再次运行时不再获取
     *
     * @param jobExecutionContext 任务执行上下文
     */
    private void beforeAnalysis(JobExecutionContext jobExecutionContext) {
        try {
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();// 任务数据
            long endTime = System.currentTimeMillis();// 当前日期
            this.createTime = endTime;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(endTime));
            calendar.add(Calendar.DATE, -60);
            long startTime = calendar.getTimeInMillis();// 60天前
            // 1、设置AI模型信息
            if (this.aiModelProcess == null) {
                this.aiModelProcess = (AIModelProcess) jobDataMap.get("aiModelProcess");
            }
            // 2、获取指标信息
            if (this.metricInfo == null) {
                this.metricInfo = this.iBigdataService.getMetricInfo(this.aiModelProcess.getAiModel().getDetectionParams().getMetric(), startTime, endTime);
            }
            // 3、获取指标数据
            this.originalMetricData = this.metricEsUtil.metricHistogramSubCountList(this.metricInfo);
            // 4、准备算法配置
            if (this.algorithmProperties == null || this.algorithmProperties.isEmpty()) {
                this.algorithmProperties = new Properties();
                this.algorithmProperties.load(new FileInputStream(ALGORITHM_CONF_PATH));
            }
            // 5、清空已分析算法
            this.hasAnalysedAlgorithm.clear();
            // 6、创建存放计算结果的缓存文件夹，位置/data/tmp/analysis_data_cache/jobGroupName-jobName
            if (StringUtils.isBlank(this.pythonDataCache)) {
                this.pythonDataCache = String.format("%s/analysis_data_cache/%s-%s", CACHE_PATH, this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName());
            }
            File file = new File(this.pythonDataCache);
            if (file.exists()) {
                FileUtils.deleteQuietly(file);
            }
            boolean success = file.mkdirs();
            LOG.info("AI模型任务 {}-{} 数据准备完成，缓存文件目录：{}，创建{}。", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), this.pythonDataCache, success ? "成功" : "失败");
        } catch (IOException ioe) {
            LOG.error(String.format("AI模型任务 %s-%s 准备数据异常", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName()), ioe);
        }
    }

    /**
     * 校验数据，根据算法执行脚本
     */
    private void analysis() {
        // 1、清理原有未结束的进程
        this.aiModelProcess.destroy();
        // 2、准备命令
        String metricDataString = this.originalMetricData.stream()
                .map(metricData -> String.valueOf(metricData.get("value")))
                .collect(Collectors.joining(" "));// 将指标数据转为字符串参数
        List<String> algorithmIds = this.aiModelProcess.getAiModel().getDetectionParams().getAlgorithm();
        StringBuilder analysisResponse = new StringBuilder();
        for (String algorithmId : algorithmIds) {
            if (!validate(algorithmId)) {// 校验数据
                continue;
            }
            this.hasAnalysedAlgorithm.add(algorithmId);
            try {
                Process process = Runtime.getRuntime().exec(
                        String.format("%s --limit %s %s %s %s %s %s %s %s",
                                GlobalAttribute.getPropertyString("cpulimit_path",
                                        String.format("%s/../cpulimit-master/src/cpulimit", SystemProperUtil.getResourcesPath())),// cpulimit命令执行路径
                                GlobalAttribute.getPropertyString("ai_cpulimit", "100"),// cpulimit 限制大小
                                GlobalAttribute.getPropertyString("python2_executable", "python"),// Python执行路径
                                PYTHON_PATH,// 算法脚本地址
                                algorithmId,// 算法ID
                                this.pythonDataCache,// 分析结果缓存地址
                                this.originalMetricData.get(0).get("time"),// 指标统计的起始时间
                                this.originalMetricData.size(),// 指标数据数量
                                metricDataString));
                this.aiModelProcess.putProcess(algorithmId, process);
                process.waitFor();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String buffer;
                    String prefix = String.format("%n算法(%s)执行成功：", algorithmId);
                    while ((buffer = reader.readLine()) != null) {
                        analysisResponse.append(prefix).append(buffer);
                        prefix = "\n";
                    }
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String buffer;
                    String prefix = String.format("%n算法(%s)执行失败：", algorithmId);
                    while ((buffer = reader.readLine()) != null) {
                        analysisResponse.append(prefix).append(buffer);
                        prefix = "\n";
                    }
                }
                this.aiModelProcess.removeAndDestroyProcess(algorithmId);
            } catch (Exception e) {
                LOG.error(String.format("AI模型任务 %s-%s 算法(%s)调用异常", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), algorithmId), e);
            }
        }
        LOG.info("AI模型任务 {}-{} 算法调用结果：{}", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), analysisResponse.toString());
    }

    /**
     * 校验数据是否符合算法
     *
     * @param algorithmId 算法id
     * @return 校验结果
     */
    private boolean validate(String algorithmId) {
        if (StringUtils.isBlank(algorithmId)) {
            LOG.info("AI模型任务 {}-{} 算法id为空。", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName());
            return false;
        }
        String periodMin = this.algorithmProperties.getProperty(String.format("%s.periodMin", algorithmId),
                "WeeklyGaussianEstimation".equals(algorithmId) ? "21d" : "2h");
        String periodMax = this.algorithmProperties.getProperty(String.format("%s.periodMax", algorithmId), "60d");
        // 默认间隔为10m，后续修改为动态值：this.aiModelProcess.getAiModel().getDetectionParams().getWindow()+this.aiModelProcess.getAiModel().getDetectionParams().getTimeUnit()
        Double min = rangeOfData(periodMin, "10m");
        Double max = rangeOfData(periodMax, "10m");
        if (this.originalMetricData.size() <= min || this.originalMetricData.size() > max) {
            LOG.info("AI模型任务 {}-{} 指标数据不在算法({})计算区间内，指标数据数量为{}，算法数据范围为[{},{}]。当前指标暂时不能使用此算法。",
                    this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), algorithmId, this.originalMetricData.size(), min, max);
            return false;
        }
        return true;
    }

    /**
     * 处理数据
     */
    private void processData() {
        if (this.hasAnalysedAlgorithm.isEmpty()) {
            return;
        }
        List<Map<String, Object>> data = new ArrayList<>();
        this.hasAnalysedAlgorithm.forEach(algorithmId -> {
            try {
                Map<String, Object> storeData = storeData(algorithmId);
                if (storeData != null) {
                    data.add(storeData);
                }
            } catch (IOException e) {
                LOG.error(String.format("AI模型任务 %s-%s 算法(%s)缓存文件读取异常", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), algorithmId), e);
            }
        });
        // 删除模型运行结果历史数据
        int deleteNum = aiAnomalyAnalysisMapper.deleteAiAnalysisData(this.aiModelProcess.getAiModel().getRuleId());
        int insertNum = 0;
        if (!data.isEmpty()) {
            insertNum = aiAnomalyAnalysisMapper.saveAiAnalysisData(data);
        }
        boolean isDelete = FileUtils.deleteQuietly(new File(this.pythonDataCache));
        LOG.info("AI模型任务 {}-{} 分析完成，删除历史计算数据{}条，插入新数据{}条，删除缓存文件{}", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), deleteNum, insertNum, isDelete ? "成功" : "失败");
    }

    /**
     * 封装需插入数据库的分析结果数据
     *
     * @param algorithmId 算法id
     * @return
     * @throws IOException
     */
    private Map<String, Object> storeData(String algorithmId) throws IOException {
        // 1、缓存文件校验
        String dataPath = String.format("%s/%s.json", this.pythonDataCache, algorithmId);
        File file = new File(dataPath);
        if (!file.exists()) {
            LOG.warn("AI模型任务 {}-{} 算法({})运行异常，分析结果缓存文件{}不存在", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), algorithmId, dataPath);
            return null;
        }
        String jsonStr = FileUtils.readFileToString(file, "utf-8");// 文件中的数据
        if ("false".equals(jsonStr)) {
            JSONObject judgeObject = new JSONObject();
            judgeObject.put(algorithmId, Collections.singletonMap("prejudgeResult", Boolean.FALSE));
            jsonStr = judgeObject.toJSONString();
        } else if (StringUtils.isBlank(jsonStr) || !jsonStr.trim().startsWith("{")) {
            LOG.warn("AI模型任务 {}-{} 算法({})运行异常，分析结果格式不正确", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), algorithmId, dataPath);
            return null;
        }
        // 2、分析数据转为json，org.json.JSONObject容错的范围更大
        JSONObject jsonObj = JSON.parseObject(new org.json.JSONObject(jsonStr).toString());
        // 3、重新封装分析数据
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("id", UUID.randomUUID().toString());
        analysisData.put("originalData", jsonObj.toJSONString());

        JSONObject uiData = new JSONObject();
        uiData.put("name", this.aiModelProcess.getAiModel().getRuleName() + "-" + algorithmId);
        uiData.put("algorithmId", algorithmId);
        JSONObject jsonData = jsonObj.getJSONObject(algorithmId);
        // 判断是否适合算法计算
        if (jsonData == null) {
            uiData.put("suitable", false);
        } else if (jsonData.containsKey("prejudgeResult") && !jsonData.getBooleanValue("prejudgeResult")) {
            uiData.put("suitable", false);
            uiData.put("timestampData", jsonData.getJSONArray("timestampData"));
            uiData.put("realData", jsonData.getJSONArray("realData"));
        } else {
            uiData.put("suitable", true);
            uiData.put("timestampData", jsonData.getJSONArray("timestampData"));
            uiData.put("realData", jsonData.getJSONArray("realData"));
            uiData.put("fitMaxData", jsonData.getJSONArray("fitMaxData"));
            uiData.put("fitMinData", jsonData.getJSONArray("fitMinData"));
            uiData.put("referenceData", jsonData.getJSONArray("referenceData"));
            uiData.put("levelData", jsonData.getJSONArray("levelData"));
            uiData.put("anomalies", new JSONArray(new ArrayList<>(jsonData.getJSONObject("anomalies").values())));
            uiData.put("config", jsonData.get("config"));
        }
        analysisData.put("uiData", uiData.toJSONString());
        analysisData.put("createTime", this.createTime);
        analysisData.put("modelId", this.aiModelProcess.getAiModel().getRuleId());
        analysisData.put("algorithmId", algorithmId);
        LOG.info("AI模型任务 {}-{} 算法({})分析完成，数据ID：{}", this.aiModelProcess.getJobGroupName(), this.aiModelProcess.getJobName(), algorithmId, analysisData.get("id"));
        return analysisData;
    }

    /**
     * 通过数据周期与间隔计算数据个数
     *
     * @param period   周期
     * @param interval 间隔
     */
    private double rangeOfData(String period, String interval) {
        double periodQuantifier = Double.parseDouble(period.substring(0, period.length() - 1));
        String unit = period.substring(period.length() - 1);
        double periodUnit = transferUnit(unit);
        double intervalQuantifier = Double.parseDouble(interval.substring(0, interval.length() - 1));
        unit = interval.substring(interval.length() - 1);
        double intervalUnit = transferUnit(unit);
        return periodQuantifier * periodUnit / (intervalQuantifier * intervalUnit);
    }

    /**
     * d-天;h-时;m-分;其他-月
     *
     * @param unit 单位
     * @return 转为秒的系数
     */
    private Double transferUnit(String unit) {
        return "d".equals(unit) ? 1.0 * 24 * 60 * 60 :
                "h".equals(unit) ? 1.0 * 60 * 60 :
                        "m".equals(unit) ? 1.0 * 60 : 1.0 * 30 * 24 * 60 * 60;
    }

}
