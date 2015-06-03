package com.hp.dbsync.util;


public class NumberUtil {

	static public int parseInt(String s) {
		if(StringUtil.isBlank(s)){
			return 0;
		}
		return Integer.parseInt(s);
	}

	static public float parseFloat(String s) {
		if(StringUtil.isBlank(s)){
			return 0;
		}
		return Float.parseFloat(s);
	}

	static public double parseDouble(String s) {
		if(StringUtil.isBlank(s)){
			return 0;
		}
		return Double.parseDouble(s);
	}

}
