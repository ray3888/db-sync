package com.hp.dbsync.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ����
 * @version 1.0
 * @date 2008-9-2
 * @class_displayName PtintUtil
 */

public class PrintUtil {
	
	static public int setSystemOut(String file) throws Exception{
		PrintStream myout = new PrintStream(new FileOutputStream(new File(file)));       
		System.setOut(myout);   
		return 1;
	}

	static public void printStr(byte[] str){
		for (int i = 0; i < str.length; i++) {
			int intTmp = str[i]&0xFF;
			System.out.print(intTmp+"=");
		}
		System.out.print("\n");
	}
	
	static public void printArray(Object[][] array){
		for(int i=0;i<array.length;i++){
			for(int j=0;j<array[i].length;j++){
				if(j!=0){
					System.out.print(",");
				}
				System.out.print(array[i][j]);
			}
			System.out.print("\n");
		}
	}
	
	static public void printArray(int[][] array){
		for(int i=0;i<array.length;i++){
			for(int j=0;j<array[i].length;j++){
				if(j!=0){
					System.out.print(",");
				}
				System.out.print(array[i][j]);
			}
			System.out.print("\n");
		}
	}

	
	static public int printObject(Object in_obj){
		System.out.print("\n");
		return printObject(in_obj,"","");
	}

	static public int printObject(Object in_obj,String in_tab,String prex){
		String tab="\t";
		String sep="";
		if(in_obj==null || in_obj instanceof String){
			System.out.print(prex);//
			System.out.print(in_obj);
		}else if(in_obj instanceof Map){
			System.out.print("\n"+in_tab+prex+"(Map){");
			for(Iterator it=((Map)in_obj).keySet().iterator();it.hasNext();){
				String key=(String)it.next();
				System.out.print(sep);
				printObject(((Map)in_obj).get(key),tab+in_tab,key+":");
				sep=",";
			}
			System.out.print("}");
		}else if(in_obj instanceof List){
			System.out.print("\n"+in_tab+prex+"(List){");
			for(Iterator it=((List)in_obj).iterator();it.hasNext();){
				System.out.print(sep);
				printObject(it.next(),tab+in_tab,"");
				sep=",";
			}
			System.out.print("}");
		}else if(in_obj instanceof Object[]){
			System.out.print("\n"+in_tab+prex+"(Array){");
			for(int i=0;i<((Object[])in_obj).length;i++){
				System.out.print(sep);
				printObject(((Object[])in_obj)[i],tab+in_tab,"");
				sep=",";
			}
			System.out.print("}");
		}else if(in_obj instanceof Object){
			System.out.print(prex+"{Object}"+in_obj);
		}else{
			System.out.print(in_obj);
		}
		return 1;
	}

	
}