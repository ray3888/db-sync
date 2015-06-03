package com.hp.dbsync.util;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author dcuser256
 * 
 */
public class DateTimeUtil {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(DateTimeUtil.class);
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static void main(String[] args) throws InterruptedException {
		Timestamp maxSqlTimestamp = getMaxSqlTimestamp();
		System.out.println(maxSqlTimestamp);
	}

	public static java.sql.Timestamp getMaxSqlTimestamp() {
//		return new java.sql.Timestamp(Long.MAX_VALUE);
		return str2SqlTimestamp("9999-00-00 00:00:00");
	}

	public static java.sql.Timestamp str2SqlTimestamp(String arg0) {
		return str2SqlTimestamp(arg0, DEFAULT_FORMAT);
	}
	
	public static java.sql.Timestamp str2SqlTimestamp(String arg0, String format) {
		try {
			 Date d = new SimpleDateFormat(format).parse(arg0);
			 return new java.sql.Timestamp( d.getTime());
		} catch (Exception exc) {
			throw new RuntimeException("can not convert this string: " + arg0 + " to timestamp.");
		}
	}

	public static Date str2Date(String arg0) {
		return str2Date(arg0, DEFAULT_FORMAT);
	}

	public static Date str2Date(String arg0, String format) {
		try {
			return new SimpleDateFormat(format).parse(arg0);
		} catch (Exception exc) {
			throw new RuntimeException("can not convert this string: " + arg0 + " to date.");
		}
	}

	public static String date2Str(Date date) {
		return date2Str(date, DEFAULT_FORMAT);
	}

	public static String date2Str(Date date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}

	public static String formatDate(String date, String formatFrom, String formatTo) {
		return date2Str(str2Date(date, formatFrom), formatTo);
	}

	public static String getCurrentDateTime(String format) {
		return date2Str(getCurDateTime(), format);
	}

	public static Date getCurDateTime() {
		return Calendar.getInstance().getTime();
	}

	public static java.sql.Timestamp getCurSqlTimestamp() {
		return new java.sql.Timestamp(getCurDateTime().getTime());
	}

	public static java.sql.Date getCurSqlDateTime() {
		return new java.sql.Date(getCurDateTime().getTime());
	}

	public static int getLastDayOfMonth(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static long getDifDays(Date d1, Date d2) {
		return (d2.getTime() - d1.getTime()) / (24 * 60 * 60 * 1000);
	}

	public static int getDifMonths(Date d1, Date d2) {
		int difMonths = new Integer(date2Str(d2, "MM")) - new Integer(date2Str(d1, "MM"));
		difMonths += (new Integer(date2Str(d2, "yy")) - new Integer(date2Str(d1, "yy"))) * 12;
		return difMonths;
	}

	// public static Date getCurrentDateTime(Locale loc) {
	// return Calendar.getInstance(loc).getTime();
	// }

	public static Date addSeconds(Date d, int seconds) {
		return new Date(d.getTime() + seconds * 1000);
	}

	public static Date addMinutes(Date d, int minutes) {
		return new Date(d.getTime() + minutes * 60 * 1000);
	}

	public static Date addHours(Date d, int hours) {
		return new Date(d.getTime() + hours * 60 * 60 * 1000);
	}

	public static Date addDays(Date d, int days) {
		return new Date(d.getTime() + days * 24 * 60 * 60 * 1000);
	}

	public static Date addMonths(Date d, int months) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.add(Calendar.MONTH, months);
		return calendar.getTime();
	}

	public static Date addYears(Date d, int years) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.add(Calendar.YEAR, years);
		return calendar.getTime();
	}

	public static long curTimeMillis() {
		return System.currentTimeMillis();
	}

	public static long printDiffTimeOfms(long t1, long t2) {
		String s="cost " + (t2 - t1)/1000.0 + " s";
//		System.out.println(s); // 1s=1000ms
		logger.info(s);
		return t2 - t1;
	}

	public static long curNanoTime() {
		return System.nanoTime();
	}

	public static long mktime(long year0, long mon0, long day, long hour, long min, long sec) {
		long mon = mon0, year = year0;
		if (0 > (mon -= 2)) {
			mon += 12;
			year -= 1;
		}
		return (long) ((((year / 4 - year / 100 + year / 400 + 367 * mon / 12 + day) + year * 365 - 719499) * 24 + hour) * 60 + min) * 60 + sec;
	}

	static Map<String,java.sql.Date> m_partition_dm=new ConcurrentHashMap<String,java.sql.Date>();
	public static java.sql.Date get_partition_dm(String stamp_link){
		if(m_partition_dm.containsKey(stamp_link)){
			return m_partition_dm.get(stamp_link);
		}
		try {
			String t=stamp_link.split("\\.")[1];
			String MM=t.substring(0, 1);
			String dd=t.substring(1, 3);
			String yyyy="20"+t.substring(4,6);
			if(MM.equals("a")){
				MM="10";
			}else if(MM.equals("b")){
				MM="11";
			}else if(MM.equals("c")){
				MM="12";
			}else{
				MM="0"+MM;
			}
			java.sql.Date r=new java.sql.Date(DateTimeUtil.str2Date(yyyy+MM+dd, "yyyyMMdd").getTime());
			m_partition_dm.put(stamp_link, r);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static Map<String,String> m_partition_dm2=new ConcurrentHashMap<String,String>();
	public static String get_partition_dm2(String stamp_link){
		if(m_partition_dm2.containsKey(stamp_link)){
			return m_partition_dm2.get(stamp_link);
		}
		try {
			String t=stamp_link.split("\\.")[1];
			String MM=t.substring(0, 1);
			String dd=t.substring(1, 3);
			String yyyy="20"+t.substring(4,6);
			if(MM.equals("a")){
				MM="10";
			}else if(MM.equals("b")){
				MM="11";
			}else if(MM.equals("c")){
				MM="12";
			}else{
				MM="0"+MM;
			}
			String r=yyyy+"-"+MM+"-"+dd;
			m_partition_dm2.put(stamp_link, r);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String get_partition_dm_pspw(String stamp_link){
		if(m_partition_dm2.containsKey(stamp_link)){
			return m_partition_dm2.get(stamp_link);
		}
		try {
			String t=stamp_link.split("_")[1];
//			String MM=t.substring(0, 1);
//			String dd=t.substring(1, 3);
//			String yyyy="20"+t.substring(4,6);
//			if(MM.equals("a")){
//				MM="10";
//			}else if(MM.equals("b")){
//				MM="11";
//			}else if(MM.equals("c")){
//				MM="12";
//			}else{
//				MM="0"+MM;
//			}
			String r="20"+t.substring(0, 2)+"-"+t.substring(2,4)+"-"+t.substring(4, 6);
			m_partition_dm2.put(stamp_link, r);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static java.sql.Date get_pwst_partition_dm(String stamp_link){
	try {
		String t=stamp_link.split("_")[1];
//		String MM=t.substring(0, 1);
//		String dd=t.substring(1, 3);
//		String yyyy="20"+t.substring(4,6);
//		if(MM.equals("a")){
//			MM="10";
//		}else if(MM.equals("b")){
//			MM="11";
//		}else if(MM.equals("c")){
//			MM="12";
//		}else{
//			MM="0"+MM;
//		}
		java.sql.Date r=new java.sql.Date(DateTimeUtil.str2Date("20"+t, "yyyyMMdd").getTime());
		return r;
	} catch (Exception e) {
		e.printStackTrace();
	}
	return null;
}
	
}
