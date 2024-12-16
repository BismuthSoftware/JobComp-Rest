package com.bismuth.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class PropertyUtil {
	public static XMLConfiguration config = null;

	public static void loadDbConfig() throws Exception {
		try {
			StringBuilder sb = new StringBuilder();
			String fileName = sb.append((new File("")).getCanonicalPath()).append(File.separator).append("conf").append(File.separator).append("jcompRest.xml").toString();
			File file = new File(fileName);
			
			if(!file.exists()) {
				throw new Exception();
			}
			
			config = new XMLConfiguration(file);			
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getProperties(String key) {
		Map<String, String> configMap = new HashMap<String, String>();
		List<Object> rootFolderKey = null;
		List<Object> rootFolderValue = null;
		List<HierarchicalConfiguration> configList = config.configurationsAt(key);

		for(HierarchicalConfiguration target : configList) {
	    	rootFolderKey = target.getList("add[@key]");
	    	rootFolderValue = target.getList("add[@value]");
	    }

		if((rootFolderKey == null) || (rootFolderValue == null)) {
	    	return configMap;
	    }

	    for(int i = 0; i < rootFolderKey.size(); i++) {
	    	String folderKey = (String)rootFolderKey.get(i);
	      	String foldervalue = (String)rootFolderValue.get(i);

	      	configMap.put(folderKey, foldervalue);
	    }
	    
	    return configMap;
	}

	public static String getProperty(String key) {
		return config.getString(key);
	}
}