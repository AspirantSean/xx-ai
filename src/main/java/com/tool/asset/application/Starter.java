package com.tool.asset.application;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.tool.asset.entities.AssetRatingRule;
import com.tool.asset.handler.AssetRatingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ytm
 * @version 2.0
 * @since 2023/5/30 16:32
 */
@Slf4j
public class Starter {

    public static Logger logger = LoggerFactory.getLogger("ratingAssetLog");

    public static boolean saveResult = false;

    public static void main(String[] args) {
        String ratingTime;
        String assetIds;
        String baasUrl;
        if (ArrayUtil.isEmpty(args)) {
            ratingTime = LocalDateTime.now().format(AssetRatingRule.dateTimeFormatter);
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
                String tip = "\n执行命令：java -jar (tool path) [-ratingTime=yyyy-MM-dd_HH:mm:ss[ -assetIds={id1}[,{id2}[,{id3}...]][ -baasUrl=http://1.flink1:8999]]]\n"
                        + "    示    例：java -jar tool.jar -ratingTime=2023-04-01_23:59:59 -assetIds=asset_1,asset_2,asset_3 -baasUrl=http://1.flink1:8999\n"
                        + "    参数解释：\n"
                        + "         -ratingTime     评级时间，需评级当天任意时间，会自动将评级起止时间定位到ratingTime的当天0时0分0秒-23时59分59秒\n"
                        + "         -assetIds       待评级资产id，英文逗号分隔，不可有空格\n"
                        + "         -baasUrl        baas服务地址\n"
                        + "         -save           评级结果是否存入数据库";
                System.out.println(tip);
                log.warn(tip);
                return;
            }
            log.info("参数：" + JSON.toJSONString(map));
            String _ratingTime = map.get("-ratingTime");
            assetIds = map.get("-assetIds");
            baasUrl = map.get("-baasUrl");
            saveResult = "true".equals(map.get("-save"));
            if (StringUtils.isNotBlank(_ratingTime)) {
                LocalDateTime date = LocalDateTime.parse(_ratingTime, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"));
                ratingTime = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 23, 59, 59).format(AssetRatingRule.dateTimeFormatter);// 设为当天最后的时间点
            } else {
                ratingTime = LocalDateTime.now().format(AssetRatingRule.dateTimeFormatter);
            }
        }
        AssetRatingHandler assetRatingHandler = AssetRatingHandler.getInstance(baasUrl);
        if (StringUtils.isBlank(assetIds)) {
            assetRatingHandler.calculate(ratingTime);
        } else {
            assetRatingHandler.pushRatingAsset(ratingTime, assetIds.split(","));
        }
    }
}
