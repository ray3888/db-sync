package com.hp.dbsync.util;


import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class JSONUtil {

	public static JSONArray getJSONArrayByFormat(Object obj) {
		JsonConfig jsonConfig = new JsonConfig();
		JsonDateValueProcessor jsonDateValueProcessor = new JsonDateValueProcessor();
		jsonConfig.registerJsonValueProcessor(Date.class, jsonDateValueProcessor);
		jsonConfig.registerJsonValueProcessor(String.class, jsonDateValueProcessor);
		return JSONArray.fromObject(obj, jsonConfig);
	}

	public static JSONObject getJSONObjectByFormat(Object obj) {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		return JSONObject.fromObject(obj, jsonConfig);
	}

	public static JSONArray getJSONArray(Object obj) {
		return JSONArray.fromObject(obj);
	}

	public static JSONObject getJSONObject(Object obj) {
		return JSONObject.fromObject(obj);
	}

	public static String getJSONArrayByFormatToString(Object obj) {
		return getJSONArrayByFormat(obj).toString();
	}

	public static String getJSONObjectByFormatToString(Object obj) {
		return getJSONObjectByFormat(obj).toString();
	}

	public static String getJSONArrayToString(Object obj) {
		return getJSONArray(obj).toString();
	}

	public static String getJSONObjectToString(Object obj) {
		return getJSONObject(obj).toString();
	}

	public static String formatDate(JSONObject jo,String name) {
		if(jo==null){
			return null;
		}
		Date d=new Date(jo.getJSONObject(name).getInt("date"));
		jo.getJSONObject(name).put("ts", DateTimeUtil.date2Str(d));
		return jo.toString();
	}

	public static String formatDate(JSONArray ja,String name) {
		if(ja==null){
			return null;
		}
		for(Object o:ja){
			formatDate((JSONObject)o,name);
		}
		return ja.toString();
	}
	
}