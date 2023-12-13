package com.dbapp.extension.ai.es.client;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class SimpleESResultVO {
	private long total;
	private String scrollid;
	private List<JSONObject> documents;
	private JSONObject aggregation;
}
