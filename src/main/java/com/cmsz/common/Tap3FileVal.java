package com.cmsz.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.cmsz.entity.Ftp;

/**
 * 文件级校验
 * @author Administrator
 *
 */
public class Tap3FileVal {
	
	private static Logger logger = Logger.getLogger(Tap3FileVal.class);
	
	private static String IpAddr = FtpTransfer.IpAddr;
	private static String UserName = FtpTransfer.UserName;
	private static String Pwd = FtpTransfer.Pwd;
	private static String Path = FtpTransfer.Path;
	private static String LocalPath = FtpTransfer.LocalPath;
	private static String RemotePath = FtpTransfer.RemotePath;
	
	private static String InputPath;//解码输入目录
	private static String WrongPath;//错单目录
	static {
		Properties p = new Properties();
		try {
			p.load(new InputStreamReader(Tap3FileVal.class.getClassLoader().getResourceAsStream("ftp.properties"),"UTF-8"));
			InputPath = p.getProperty("InputPath");
			WrongPath = p.getProperty("WrongPath");
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException("加载配置文件失败", e);
		}
	}
	
	/**
	 * 从ftp服务器采集tap3格式文件
	 * @param f
	 */
	public static void getTap3Files(){
		Ftp f = new Ftp();
		f.setIpAddr(IpAddr);
		f.setUserName(UserName);
		f.setPwd(Pwd);
		f.setPath(Path);
		try {
			FtpTransfer.connectFtp(f);
			FtpTransfer.startDown(f, LocalPath, RemotePath);
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException("获取文件失败");
		} finally {
			FtpTransfer.closeFtp();
		}
	}
	
	private static List<String> nameList = new ArrayList<String>();
	/**
	 * 获取文件名
	 * @param path
	 * @return
	 */
	public static List<String> getFileName(String path){
		File file = new File(path);
		File[] files = file.listFiles();
		for(File f:files) {
			if(f.isFile()) {
				String fileName = f.getName();
				nameList.add(fileName);
			}else if(f.isDirectory()) {
				getFileName(f.getPath());
			}else {
				throw new RuntimeException("没有文件！");
			}
		}
		return nameList;
	}
	
	/**
	 * 文件校验
	 */
	public static void checkFiles() {
		List<String> list = getFileName(LocalPath);
		List<String> mvnoList = getMvnoId();//获取转售商的缩写编码
//		for(String name:list) {
//			System.out.println(name);
//		}
//		System.out.println(list.size());
//		int count = 0;//存放连续性编码
		for(int i=0;i<list.size();i++) {
			if(list.get(i).length()==25) {
				System.out.println(list.get(i).length());
				//截取文件名对应位置的字符串
				String name1 = list.get(i).substring(0, 7);
				String name2 = list.get(i).substring(7, 12);
//				String name3 = list.get(i).substring(12, 17);
				String name4 = list.get(i).substring(17, 25);
				if(name1.equals("CDCMCC1") && mvnoList.contains(name2) && isValidDate(name4)) {//校验前缀、转售商缩写和日期合法性
					/*if(i==0) {//第一条不校验连续性
						//都校验成功时，输出到解码输入目录，否则输出到错单目录
						writeFileToDir(LocalPath+File.separator+list.get(i),InputPath+File.separator+list.get(i));
						count = Integer.parseInt(name3);
					}else if(i>0 && Integer.parseInt(name3)-count==1) {//从第二条校验是否连续
						writeFileToDir(LocalPath+File.separator+list.get(i),InputPath+File.separator+list.get(i));
						count = Integer.parseInt(name3);
					}else {
						writeFileToDir(LocalPath+File.separator+list.get(i),WrongPath+File.separator+list.get(i));
						count = Integer.parseInt(list.get(i-1).substring(12, 17));
					}*/
					writeFileToDir(LocalPath+File.separator+list.get(i),InputPath+File.separator+list.get(i));
				}else {
					writeFileToDir(LocalPath+File.separator+list.get(i),WrongPath+File.separator+list.get(i));
//					count = Integer.parseInt(list.get(i-1).substring(12, 17));
				}
			}else {
				writeFileToDir(LocalPath+File.separator+list.get(i),WrongPath+File.separator+list.get(i));
			}
		}
	}
	
	/**
	 * 获得转售商缩写编码
	 * @return
	 */
	public static List<String> getMvnoId(){
		List<String> mvnoList = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select MVNO_ID from mvno";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				mvnoList.add(rs.getString(1));
			}
		} catch (SQLException e) {
			logger.error(e);
		}finally {
			DBUtil.close(conn);
		}
		return mvnoList;
	}
	
	/**
	 * 将文件写出到相应目录
	 * @param oldPath 获取到的文件路径
	 * @param newPath 文件写出的路径
	 */
	public static void writeFileToDir(String oldPath, String newPath) {
		File files = new File(oldPath);
		File wrFies = new File(newPath);
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(files);
			fos = new FileOutputStream(wrFies);
			byte[] buffer = new byte[1024];
			if((fis.read(buffer))!=-1) {
				fos.write(buffer);
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	/**
	 * 检验字符串是否是合法日期
	 * @param str
	 * @return
	 */
	public static boolean isValidDate(String str) {
	      boolean convertSuccess=true;
	       // 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
	       SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	       try {
	    	  // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
	          format.setLenient(false);
	          format.parse(str);
	       } catch (ParseException e) {
	          logger.error(e);
	          // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
	          convertSuccess=false;
	       } 
	       return convertSuccess;
	}
}
