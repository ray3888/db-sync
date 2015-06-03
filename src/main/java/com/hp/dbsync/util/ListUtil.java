package com.hp.dbsync.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUtil {
	
	public static <T> List<T> deepCopy(List<T> src) {
		List<T> dest = null;
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(src);
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in = new ObjectInputStream(byteIn);
			dest = (List<T>) in.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dest;
	}

	
	public static Map mapArrayToMap(Map[] mapArray,String key)throws Exception{
		Map ret=new HashMap();
		for(int i=0;i<mapArray.length;i++){
			ret.put(mapArray[i].get(key), mapArray[i]);
		}
		return ret;
	}

	public static Map mergeMap(Map map1,Map map2) throws Exception {
		Map retMap=new HashMap();
		retMap.putAll(map1);
		retMap.putAll(map2);
		return retMap;
	}
	
	public static String repeatStr(String s,int num)throws Exception{
		StringBuffer ret = new StringBuffer(s);
		for(int i=1;i<num;i++){
			ret.append(s);
		}
		return ret.toString();
	}
	
	public static String join(Object[] objs,String sep)throws Exception{
		String ret="";
		for(int i=0;i<objs.length;i++){
			ret+=sep+objs[i];
		}
		if(ret.length()>0){
			ret=ret.substring(sep.length());
		}
		return ret;
	}
	
	public static String join(List lt,String sep)throws Exception{
		return join(lt.toArray(new Object[0]),sep);
	}


}
