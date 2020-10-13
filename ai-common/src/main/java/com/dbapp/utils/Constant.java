package com.dbapp.utils;

/**
 * Created by stefan.ji on 2017/3/5.
 */
public interface Constant {

    interface  TOPIC {
        //规则告警主题
        String TOPIC_WEB_ALERT="com.dbapp.stormsoc.topic.securityevent";
        //有效告警数据拆分主题
        String TOPIC_SECURITY_EVENT_INDEX="com.dbapp.storm.realtime.warning";
        //有效告警临时主题，做之后聚合用
        String TOPIC_SECURITY_EVENT_TMP="com.dbapp.storm.realtime.tmp.warning";
        //有效告警聚合主题
        String TOPIC_SECURITY_EVENT_AGG="com.dbapp.storm.realtime.agg.warning";
        //apt主题
        String TOPIC_APT="apt.logstash.dock.3";
        //es2kafka 主题，现在放三种数据
        String TOPIC_ES_KAFKA="com.dbapp.mirror.es2kafka";

        String TOPIC_AGG_TMP = "com.dbapp.storm.realtime.tmp.warning";
    }

    interface ALERT_TYPE{
        String SCENE = "scene";
        String VUL = "vulnerability";
        String APT = "apt";
        String ASS = "association";
        String RULE = "rule";
        String DEVICE = "device";
    }

    interface CLOUMNS {
         String TOPN = "TOPN";
         String COUNT = "COUNT";
         String SUMMARY = "SUMMARY";
         String TIMELINE = "TIMELINE";
         String SUMALL = "SUMALL";
         String KEYCOUNTLINE = "KEYCOUNTLINE";
    }

    interface RequestClientPattern {
         String TOOL = "requestClientPattern_tool";
         String SOFT = "requestClientPattern_soft";
         String BOT = "requestClientPattern_bot";
         String SCAN = "requestClientPattern_scan";
    }

    interface ReportColumn {

         String[] countDataCfs = new String[]{
                "srcAddress-srcAddress|COUNT",
                "BL_srcAddress-BL_srcAddress|COUNT"
//                "BL_srcGeoCountrySUMMARY-BL_srcGeoCountry|SUMMARY",
//                "srcAddressSUMMARY-srcAddress|SUMMARY",
        };

         String[] listDataCfs = new String[]{
                "bytesIn_TL-srcAddress~bytesIn|TIMELINE",
                "bytesOut_TL-srcAddress~bytesOut|TIMELINE",
                "BL_srcAddress_TL-BL_srcAddress|TIMELINE",
                "srcAddress_TL-srcAddress|TIMELINE",
                "BL_srcGeoCountry-BL_srcGeoCountry|TOPN",
                "srcGeoCountry-srcGeoCountry|TOPN",
                "BL_srcGeoRegion-BL_srcGeoRegion|TOPN",
                "srcGeoRegion-srcGeoRegion|TOPN",
                "BL_srcAddress-BL_srcAddress|TOPN",
                "srcAddress-srcAddress|TOPN",
                "BL_name-BL_name|TOPN",
                "requestUrl-requestUrl|TOPN",
                "clientOperatingSystem-clientOperatingSystem|TOPN",
                "requestClientApplication-requestClientApplication|TOPN",
                "responseCode-responseCode|TOPN",
                "requestClientPattern-requestClientPattern|TOPN",
                "responseCode~requestUrl-responseCode~requestUrl|TOPN",
                "BL_name~ruelId-BL_name~ruelId|TOPN",
                "BL_srcGeoCountrySUMMARY-BL_srcGeoCountry|SUMMARY",
                "srcAddressSUMMARY-srcAddress|SUMMARY"
        };
    }

    interface Method{
        String Search = "/_search";
    }

    interface EsIndex{
        String ORIGIN_INDEX = "xwd-securityevent-index";
        String AILPHA_SECURITY_ALARM= "ailpha-securityalarm";
        String AILPHA_SECURITY_EVENT= "ailpha-securityevent-*";
        String AILPHA_SECURITY_LOG= "ailpha-securitylog-*";
        String ORGIN_ALL_TYPE = null; //"securityevent,web_alert,apt,flood,data_flood";
        String INCIDENT_INDEX = "xwd-securityevent-incident";
        String PRE_AILPHA_SECURITY_LOG= "ailpha-securitylog-";
        String AILPHA_METRIC = "statistics-*";
    }

    interface WafEsIndex{
        String AGG_INDEX = "xwd-securityevent-index-agg";
        String AGG_TYPE = null;
        String WAF_TYPE = "waf";
        String ORIGIN_INDEX = "xwd-securityevent-index";
        String ORIGIN_TYPE = "securityevent";
        String ORGIN_ALERT_TYPE = "web_alert";
        String ORGIN_ALL_TYPE = null; //"securityevent,web_alert,apt,flood,data_flood";
        String TMP_INDEX = "xwd-securityevent-index-tmp";
        String TMP_TYPE = null;
        int MAX_SIZE = 10000;
    }

    interface AptEsIndex{
        String AGG_INDEX = "xwd-securityevent-index-agg";
        String AGG_TYPE = null;
        String APT_TYPE = "apt";
        String ORIGIN_INDEX = "xwd-securityevent-index";
        String ORIGIN_TYPE = "securityevent";
        String ORGIN_ALERT_TYPE = "web_alert";
        String ORGIN_ALL_TYPE = null; //"securityevent,web_alert,apt,flood,data_flood";
        String TMP_INDEX = "xwd-securityevent-index-tmp";
        String TMP_TYPE = null;
        int MAX_SIZE = 10000;
    }

    interface Stiff{
//        String DATA_ES_INDEX = "xwd-index-*";
        String DATA_ES_INDEX = "ailpha-securitylog-*";
        String DATA_ES_TYPE = "";
    }

    final class HdfsPath {
        private HdfsPath() {
            throw new IllegalStateException("Utility class");
        }
        public static String LOGS_SOURCE_PATH = "source/com.dbapp.topic.rawevent.json";
        public static String EVENTS_SOURCE_PATH = "source/com.dbapp.ailpha.topic.securityevent.json";
        public static String ALARMS_SOURCE_PATH = "source/com.dbapp.ailpha.topic.securityalarm.json";

        public static String LOGS_STREAM_PATH = "stream/logStream.json";
        public static String METRIC_LOGS_STREAM_PATH = "stream/statLogStream.json";
        public static String EVENTS_STREAM_PATH = "stream/securityEvent.json";
        public static String ALARMS_STREAM_PATH = "stream/securityAlarm.json";
        public static String METRICS_STREAM_PATH = "stream/statisticsMetric.json";
        public static String CEP_METRICS_LOG_STREAM_PATH = GlobalAttribute.getPropertyString("hdfs.cep.path", "/dbapp/cep/") + METRIC_LOGS_STREAM_PATH;
        public static String LOGS_METRICS_CUSTOM_DISTINCTCOUNT_STREAM_PATH = "stream/statLogStreamDistinct.json";
        public static String EVENTS_METRICS_CUSTOM_DISTINCTCOUNT_STREAM_PATH = "stream/securityEventDistinct.json";
        public static String ALARMS_METRICS_CUSTOM_DISTINCTCOUNT_STREAM_PATH = "stream/securityAlarmDistinct.json";
}

}
