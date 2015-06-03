package com.hp.dbsync.util;
/*
 * �������� 2008-9-4
 *
 * TODO Ҫ��Ĵ���ɵ��ļ���ģ�壬��ת��
 * ���� �� ��ѡ�� �� Java �� ������ʽ �� ����ģ��
 */


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author xul
 *
 */
public class RegexUtil {
	public static final String CHINESE_CHARACTER="[^x00-xff]"; 
	public static final String CHINESE_CHARACTER2="[^u4e00-u9fa5]"; 
	
	public static void main(String[] args) {
		String t="LOGN SGPTF DSTEAM OK";
		String[] t1= RegexUtil.getGroups("LOGN \\w+ \\w+ (\\w+)",t);
		System.out.println(t1.length);
		System.out.println(PrintUtil.printObject(t1));
	}

	public static boolean isInteger(String str) {
		if (str.matches("\\d+")) {
			return true;
		} else {
			return false;
		}
	}

	public static String getChineseCharacter(String str) throws FileNotFoundException,
			IOException {
		String regex="[u4e00-u9fa5]|\\s";
		regex+="|[\\{\\��\\`\\~\\!\\@\\#\\$\\%\\^\\&\\(\\)\\-\\_" +
				"\\=\\+\\]\\\\\\\\|\\:\\;\"\\\\\'\\<\\,\\?\\/" +
				"\\.\\*\\��\\��]";
		String ret=str.replaceAll(regex, "");
		return ret;
	}

	public static String[] getGroups(String regex,String str){
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			System.out.println("Pattern syntax error (PatternSyntaxException)");
		}
		Matcher m = pattern.matcher(str);
		List<String> al=new ArrayList<String>();
		m.find();
		for(int i=1;i<=m.groupCount();i++){
			al.add(m.group(i));
		}
		return (String[])al.toArray(new String[0]);
	}
	
	public static String[] getMatchResult(String regex,String str){
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			System.out.println("Pattern syntax error (PatternSyntaxException)");
		}
		Matcher m = pattern.matcher(str);
		List<String> al=new ArrayList<String>();
//		System.out.println("------------------");
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			String match = str.substring(start, end);
			al.add(match);
//			System.out.println("----"+match);
		}
		return (String[])al.toArray(new String[0]);
	}
}
