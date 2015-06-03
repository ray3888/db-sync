package com.hp.dbsync.util;


import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class StringUtil {
	public static List<String> stringToList(String param, String seperater) {
		String[] array = StringUtils.split(param, seperater);
		return Arrays.asList(array);
	}

	public static String ntLogin2LowerCase(String ntLogin) {
		if(ntLogin==null){
			return null;
		}
		String nt=ntLogin.substring(ntLogin.indexOf(":")+1);
		return ntLogin.replaceAll(nt, nt.toLowerCase());
	}
	
	static public boolean isBlank(String s) {
		if (StringUtils.isBlank(s) || s.equals("undefined")) {
			return true;
		}
		return false;
	}

	public static boolean isInteger(String s) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(s).matches();
	}

	public static boolean isFloatOrDouble(String s) {
		Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
		return pattern.matcher(s).matches();
	}

	public static String covertToString(Object o) {
		if (o == null) {
			return null;
		} else {
			return (String) o;
		}
	}
}