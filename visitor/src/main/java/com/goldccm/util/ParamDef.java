package com.goldccm.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Properties;

public class ParamDef {

	protected static Properties dirProp = null;

	protected static Properties aliProp = null;
	public static String findDirByName(String key){
		String value = null;
		try{
			if (dirProp==null){
				//载入
				dirProp = new Properties();
				Resource resource = new ClassPathResource("dirConfig.properties");
				InputStream in = resource.getInputStream();//ClassLoader.getSystemResourceAsStream(propPath);
				dirProp.load(in);
			}
			value = dirProp.getProperty(key);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			return value;
		}
	}

	public static String findAliByName(String key){
		String value = null;
		try{
			if (aliProp==null){
				//载入
				aliProp = new Properties();
				Resource resource = new ClassPathResource("aliPayConfig.properties");
				InputStream in = resource.getInputStream();//ClassLoader.getSystemResourceAsStream(propPath);
				aliProp.load(in);
			}
			value = aliProp.getProperty(key);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			return value;
		}
	}
}
