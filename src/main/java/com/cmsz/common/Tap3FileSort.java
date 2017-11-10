package com.cmsz.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 分拣：
 * 将话单中不同业务类型的信息进行分类
 * @author Administrator
 *
 */
public class Tap3FileSort {
	
	private static Logger logger = Logger.getLogger(Tap3FileSort.class);
	
	private static String sortPath;
	static {
		Properties p = new Properties();
		try {
			p.load(new InputStreamReader(Tap3FileSort.class.getClassLoader().getResourceAsStream("ftp.properties"),"UTF-8"));
			sortPath = p.getProperty("SortPath");
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException("加载配置文件失败", e);
		}
	}
	
	/**
	 * 分拣
	 * @param path
	 * @throws FileNotFoundException
	 */
	public static void doSort(String path) throws FileNotFoundException {
		List<String> nameList = Tap3FileVal.getFileName(path);
		for(String name:nameList) {
//			String name = file.getName();
//			System.out.println(name);
			String name2 = name.substring(7, 12);
			String sorPath = sortPath+name2;
			File files = new File(sorPath);
			if(!files.exists() && !files.isDirectory()) {
				files.mkdir();
			}
			Map<String, String> map = DupCheck.check(new File(path+File.separator+name));
			Set<Entry<String, String>> entrySet = map.entrySet();
			for(Entry<String, String> entry:entrySet) {
				String comMethod = entry.getKey();
				String line = entry.getValue();
				doWriteFiles(sorPath+File.separator+name+comMethod, line);
			}
		}
	}
	
	/**
	 * 逐行读取文件
	 * @param file
	 * @return
	 */
	public static List<String> doReadFiles(File file){
		List<String> list = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line = br.readLine())!=null){
				list.add(line);
			}
		} catch (Exception e){
			logger.error(e);
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		return list;
	}
	
	/**
	 * 将不同业务类型的信息写入文件
	 * @param path
	 * @param line
	 * @throws FileNotFoundException
	 */
	public static void doWriteFiles(String path,String line) throws FileNotFoundException {
		FileOutputStream fos = new FileOutputStream(path);
		PrintWriter pw = new PrintWriter(fos,true);
		pw.println(line);
		pw.close();
	}
}
