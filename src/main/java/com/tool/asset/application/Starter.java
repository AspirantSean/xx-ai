package com.tool.asset.application;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.tool.asset.dao.AssetInformationDao;
import com.tool.asset.entities.AssetRatingRule;
import com.tool.asset.handler.AssetRatingHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ytm
 * @version 2.0
 * @since 2023/5/30 16:32
 */
public class Starter {

    public static Logger logger = LoggerFactory.getLogger("ratingAssetLog");

    public static boolean saveResult = false;

    public static void main(String[] args) {
        List<String> ratingTimes = new ArrayList<>();
        String assetIds;
        String baasUrl;
        if (ArrayUtil.isEmpty(args)) {
            ratingTimes.add(LocalDateTime.now().format(AssetRatingRule.dateTimeFormatter));
            assetIds = null;
            baasUrl = "http://1.flink1:8999";
        } else {
            Map<String, String> map = Arrays.stream(args)
                    .map(arg -> {
                        String[] splits = arg.split("=");
                        if (splits.length > 1) {
                            return Pair.of(splits[0], splits[1]);
                        } else {
                            return Pair.of(arg, "");
                        }
                    })
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            if (map.containsKey("-help")) {
                String tip = "\n执行命令：java -jar (tool path) [-ratingTimes=yyyy-MM-dd[ -assetIds={id1}[,{id2}[,{id3}...]][ -baasUrl=http://1.flink1:8999[ -save=true]]]]\n"
                        + "    示    例：java -jar tool.jar -ratingTimes=2023-04-01,2023-04-02 -assetIds=asset_1,asset_2,asset_3 -baasUrl=http://1.flink1:8999 -save=true\n"
                        + "    参数解释：\n"
                        + "         -ratingTimes    评级时间，需评级当天日期，可逗号分隔输入多天，会自动将评级起止时间定位到ratingTime的当天0时0分0秒-23时59分59秒\n"
                        + "         -assetIds       待评级资产id，英文逗号分隔，不可有空格\n"
                        + "         -baasUrl        baas服务地址\n"
                        + "         -save           评级结果是否存入数据库";
                System.out.println(tip);
                return;
            }
            System.out.println("参数：" + JSON.toJSONString(map));
            String _ratingTimes = map.get("-ratingTimes");
            assetIds = map.get("-assetIds");
            baasUrl = map.get("-baasUrl");
            saveResult = "true".equals(map.get("-save"));
            if (StringUtils.isNotBlank(_ratingTimes)) {
                for (String _ratingTime : _ratingTimes.split(",")) {
                    LocalDateTime date = LocalDate.parse(_ratingTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
                    ratingTimes.add(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 23, 59, 59).format(AssetRatingRule.dateTimeFormatter));// 设为当天最后的时间点
                }
            } else {
                ratingTimes.add(LocalDateTime.now().format(AssetRatingRule.dateTimeFormatter));
            }
        }
        AssetRatingHandler assetRatingHandler = AssetRatingHandler.getInstance(baasUrl);
        if (StringUtils.isBlank(assetIds)) {
            String ratingTaskExecTime = AssetInformationDao.getInstance().getRatingTaskExecTime();
            if (StringUtils.isNotBlank(ratingTaskExecTime) && Long.parseLong(ratingTaskExecTime) > 10 * 60 * 1000) {
                System.out.println("全量资产上次评级时间：" + ratingTaskExecTime + "ms，是否继续评级？");
                System.out.println("请输入\"是\"继续执行，\"否\"停止执行");
                try (Scanner scanner = new Scanner(System.in)) {
                    String input = scanner.nextLine();
                    if ("否".equals(input)) {
                        return;
                    }
                }
            }
        }
        for (String ratingTime : ratingTimes) {
            if (StringUtils.isBlank(assetIds)) {
                assetRatingHandler.calculate(ratingTime);
            } else {
                assetRatingHandler.pushRatingAsset(ratingTime, assetIds.split(","));
            }
        }
    }
}
