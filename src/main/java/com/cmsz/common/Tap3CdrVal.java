package com.cmsz.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 话单级校验
 * @author Administrator
 *
 */
public class Tap3CdrVal {
	
	private static Logger logger = Logger.getLogger(Tap3CdrVal.class);
	
	public static String SourPath;
	private static String RcdrPath;
	private static String WcdrPath;
	static {
		Properties p = new Properties();
		try {
			p.load(new InputStreamReader(Tap3FileVal.class.getClassLoader().getResourceAsStream("ftp.properties"),"UTF-8"));
			SourPath = p.getProperty("SourPath");
			RcdrPath = p.getProperty("RcdrPath");
			WcdrPath = p.getProperty("WcdrPath");
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException("加载配置文件失败", e);
		}
	}
	
	/**
	 * 话单校验：校验话单是否符合相应标准
	 * @param path
	 * @throws FileNotFoundException
	 */
	public static void Tap3CdrVal(String path) throws IOException {
		//解码后存放话单的文件
		File file = new File(path);
		File[] files = file.listFiles();
		for(File f:files) {
			if(f.exists() && f.isFile()) {
				//读取文件
				String name = f.getName();
				BufferedReader br=null;
				String line = null;
				StringBuffer sb1 = new StringBuffer("");
				StringBuffer sb2 = new StringBuffer("");
				try {
					 br =new BufferedReader(new InputStreamReader(new FileInputStream(f)));
					 
					 while((line = br.readLine()) != null) {
						List<String> list = splitLine(line);
					 	String str0 = null;
					 	String str1 = null;
						String str2 = null;
						String str3 = null;
						for(int i = 0;i<list.size();i++) {
							str0 = list.get(0);
							str1 = list.get(1);
							str2 = list.get(2);
							str3 = list.get(3);
						}
						//System.out.println(str0.substring(0,2));
						if("46".equals(str0.substring(0,2))) {
							System.out.println("0号："+str0);
							//进行时间和电话号码是否符合要求的判断
							 if(isNumLega1(str1)&&timeIslegal(str3)&&(isNumLega1(str2)||isNumLega2(str2)||isNumLega3(str2)||isNumLega4(str2))) {
								 //写入到正确文件
								sb1 = sb1.append(line+"\n");
								Tap3FileSort.doWriteFiles(RcdrPath+File.separator+name,sb1.toString());
							 }else {
								 //写入到错误文件
								sb2 = sb2.append(line+"\n");
								Tap3FileSort.doWriteFiles(WcdrPath+File.separator+name,sb2.toString());
							 }
						}else if("17".equals(str0.substring(0,2))){
							System.out.println("0号："+str0);
							if(isNumLega1(str0)&&timeIslegal(str2)&&(isNumLega1(str1)||isNumLega2(str1)||isNumLega3(str1)||isNumLega4(str1))) {
								 //写入到正确文件
								sb1 = sb1.append(line+"\n");
								Tap3FileSort.doWriteFiles(RcdrPath+File.separator+name,sb1.toString());
							 }else {
								 //写入到错误文件
								sb2 = sb2.append(line+"\n");
								Tap3FileSort.doWriteFiles(WcdrPath+File.separator+name,sb2.toString());
							 }
						}				
					 }
				} catch (Exception e) {
					logger.error(e);
				}finally {
					if(br!=null) {
						br.close();
					}
				}
			}else if(f.exists() && f.isDirectory()){
				Tap3CdrVal(f.getPath());
			}else {
				throw new RuntimeException("文件不存在");
			}
		}
	}
	//判断字符串是否是电话号码
	public static boolean isNumLega1(String str) throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }
	//判断字符串是否是00开头的电话号码
	public static boolean isNumLega2(String str) throws PatternSyntaxException {
        String regExp = "[0][0][1-9]+\\d{11}";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }
	
	//判断字符串是否是0086开头的电话号码008613266638358
	public static boolean isNumLega3(String str) throws PatternSyntaxException {
	        String regExp = "[0][0][8][6]0?(13|14|15|18)[0-9]{9}";
	        Pattern p = Pattern.compile(regExp);
	        Matcher m = p.matcher(str);
	        return m.matches();
	    }
		//判断字符串是否是955开头的电话号码
	public static boolean isNumLega4(String str) throws PatternSyntaxException {
	        String regExp = "[9][5][5]\\d{2}";
	        Pattern p = Pattern.compile(regExp);
	        Matcher m = p.matcher(str);
	        return m.matches();
	    }
	
	//判断字符串是否是日期
	public static boolean timeIslegal(String time) {
		boolean flag = true;
		// 指定日期格式
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			// 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2017/10/1会被接受，并转换成2010/10/01
			format.setLenient(false);
			format.parse(time);
		} catch (ParseException e) {
			// e.printStackTrace();
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			flag = false;
		}
		return flag;

	}
	//截取逗号，剩下的字符串放入String数组中
	public static List<String> splitLine(String str) {
		//String str= "abc,,,123,,45,67,,567788";
		String[] args=str.trim().split(",");
		List<String> list = new ArrayList<String>();
		for(int i = 0;i<args.length;i++) {
			String reg="[0-9]+";
			if(args[i].matches(reg)) {
				list.add(args[i]);
			}
		}
		return list;
	}
}
