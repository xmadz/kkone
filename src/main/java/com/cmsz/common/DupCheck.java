package com.cmsz.common;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 查重
 * @author Administrator
 *
 */
public class DupCheck {
	
	public static Map<String, String> check(File file) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> list = Tap3FileSort.doReadFiles(file);
		StringBuffer sb1 = new StringBuffer("");
		StringBuffer sb2 = new StringBuffer("");
		StringBuffer sb3 = new StringBuffer("");
		Map<String, String> map1 = new HashMap<String, String>();
		Map<String, String> map2 = new HashMap<String, String>();
		Map<String, String> map3 = new HashMap<String, String>();
		for(String line:list) {
			String comMethod = line.substring(0, 4);
			String subLine = line.substring(5, 20) + "#" + line.substring(21, 32) + "#" + line.substring(33, 44) + "#" + line.substring(46, 60);
			if("GCMO".equals(comMethod)) {
				map1.put(subLine, line);
			}else if("GCMT".equals(comMethod)) {
				map2.put(subLine, line);
			}else {
				map3.put(subLine, line);
			}
		}
		Collection<String> c1 = map1.values();
		for(String str:c1) {
			sb1.append(str+"\n");
		}
		Collection<String> c2 = map2.values();
		for(String str:c2) {
			System.out.println(str);
			sb2.append(str+"\n");
		}
		Collection<String> c3 = map3.values();
		for(String str:c3) {
			sb3.append(str+"\n");
		}
		map.put("GCMO", sb1.toString());
		map.put("GCMT", sb2.toString());
		map.put("GCGP", sb3.toString());
		return map;
	}
}
