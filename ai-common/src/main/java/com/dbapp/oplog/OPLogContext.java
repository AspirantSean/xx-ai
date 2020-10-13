package com.dbapp.oplog;

import java.util.HashMap;
import java.util.Map;

public class OPLogContext {

	static ThreadLocal<Map<String, Object>> local = new ThreadLocal<Map<String, Object>>();


	public static ThreadLocal<Map<String, Object>> getLocal() {
		return local;
	}

	public static void put(String key, Object value) {
		if(local.get() !=null){
			local.get().put(key, value);
		}else{
			Map<String, Object> map = new HashMap<>();
			map.put(key, value);
			local.set(map);
		}
	}

	public static Object get(String key) {
		if (local.get() == null || !local.get().containsKey(key)) {
			return null;
		}
		return local.get().get(key);
	}
}
