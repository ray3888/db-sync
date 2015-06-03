package com.hp.dbsync.util;

import java.text.SimpleDateFormat;   
import java.util.Date;   
import java.util.Locale;   

import org.apache.commons.lang.StringUtils;
  
import net.sf.json.JsonConfig;   
import net.sf.json.processors.JsonValueProcessor;   
  
public class JsonDateValueProcessor implements JsonValueProcessor {   
  
    private String formatForTimeStamp ="yyyy-MM-dd HH:mm:ss";
    
    public Object processArrayValue(Object value, JsonConfig config) {   
        return process(value);   
    }   
  
    public Object processObjectValue(String key, Object value, JsonConfig config) {   
        return process(value);   
    }   
       
    private Object process(Object value){   
           
        if(value instanceof Date){  
            SimpleDateFormat sdf = new SimpleDateFormat(formatForTimeStamp,Locale.US);   
            String dateValue  = sdf.format(value);   
            if(dateValue.endsWith("00:00:00")){
        	     return dateValue.substring(0, 10);
            }else{
            	 return dateValue;	
            }
           
        } 
        if(value instanceof String){
        	return StringUtils.trim((String) value);     
        }
        return value == null ? "" : value;   
    }   
}