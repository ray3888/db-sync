package com.hp.dbsync.config;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.dbsync.util.DateTimeUtil;
import com.hp.dbsync.util.PrintUtil;

/**
 * Configurable value for application.
 * 
 * All the configurable value should read from this utility.
 * 
 * @version 1.0
 */
public class ConfigData {

	private static final Configuration config = Configuration.getConfiguration();

	public static class db {
		public static class source {
			public static final String classname = config.getString("db.source.classname");
			public static final String url = config.getString("db.source.url");
			public static final String username = config.getString("db.source.username");
			public static final String password = config.getString("db.source.password");
		}
		
		public static class target {
			public static final String classname = config.getString("db.target.classname");
			public static final String url = config.getString("db.target.url");
			public static final String username = config.getString("db.target.username");
			public static final String password = config.getString("db.target.password");
		}
	}
	
	public static final int batchSize = config.getInt("job.batch.size");
	public static final int jobInterval = config.getInt("job.interval");
	public static final Timestamp jobStartTs =DateTimeUtil.str2SqlTimestamp(config.getString("job.start.ts"));
	

	public static class TableConfig{
		public TableConfig(String name, String tsCol) {
			super();
			this.name = name;
			this.tsCol = tsCol;
		}
		public String name;
		public String tsCol;
	}
	
	public static List<TableConfig> tablesConfig=new ArrayList<TableConfig>();
	
	static{
		init();
	}
	
	public static void init(){
		for(String t:config.getString("tables").split(";")){
			TableConfig table=new TableConfig(t.split(":")[0].trim(),t.split(":")[1].trim());
			tablesConfig.add(table);
		}
	}
	
	public static void main(String[] args) {
		PrintUtil.printObject(tablesConfig);
	} 
}

