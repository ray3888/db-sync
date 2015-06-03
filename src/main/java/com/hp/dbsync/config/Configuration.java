package com.hp.dbsync.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Configuration {
	
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
	private static final Properties properties = new Properties();
	private static final String propertiesFile = "sqlsync-config.properties";
	
	private static Configuration configuration = new Configuration();
	
	public static Configuration getConfiguration(){
		return configuration;
	}
	
	public Configuration(){
		try {
			properties.load(Configuration.class.getClassLoader().getResourceAsStream(propertiesFile));
		} catch (Exception e) {
			throw new RuntimeException("can not load the properties file:" + propertiesFile, e);
		}
		
		logger.info("load properties file " + propertiesFile);
	}
	
	public Configuration(String propertiesFile){
		try {
			properties.load(Configuration.class.getClassLoader().getResourceAsStream(propertiesFile));
		} catch (Exception e) {
			throw new RuntimeException("can not load the properties file:" + propertiesFile, e);
		}
		
		logger.info("load properties file " + propertiesFile);
	}
	
	public int getInt(String key) {
		return Integer.valueOf(getString(key.toString())).intValue();
	}
	
	public boolean getBoolean(Enum<?> key) {
		return Boolean.valueOf(getString(key.toString())).booleanValue();
	}
	
	public String getString(String key) {
		if (!properties.containsKey(key)) {
			String t="No such property [" + key + " ] " + "in " + properties.getClass();
			logger.warn(t);
			throw new RuntimeException(t);
		}
		logger.info(String.format("load config: %s=%s", key,properties.getProperty(key)));
		return properties.getProperty(key);
	}
	
}
