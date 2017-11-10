package com.cmsz.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.cmsz.entity.Ftp;

/**
 * 从ftp服务器获取文件
 * @author Administrator
 *
 */
public class FtpTransfer {

	private static Logger logger = Logger.getLogger(FtpTransfer.class);
	
	private static String encoding = System.getProperty("file.encoding");

	private static FTPClient ftp;

	/**
	 * 获取ftp连接
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public static boolean connectFtp(Ftp f) throws Exception {
		ftp = new FTPClient();
		ftp.setControlEncoding("GBK");
		boolean flag = false;
		int reply;
		if (f.getPort() == null) {
			ftp.connect(f.getIpAddr(),Integer.parseInt(Port));
		} else {
			ftp.connect(f.getIpAddr(), f.getPort());
		}
		ftp.login(f.getUserName(), f.getPwd());
		ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
		reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			return flag;
		} 
		ftp.changeWorkingDirectory(f.getPath());
		flag = true;
		return flag;
	}

	/**
	 * 关闭ftp连接
	 */
	public static void closeFtp() {
		if (ftp != null && ftp.isConnected()) {
			try {
				ftp.logout();
				ftp.disconnect();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * 下载链接配置
	 * 
	 * @param f
	 * @param localBaseDir
	 *            本地目录
	 * @param remoteBaseDir
	 *            远程目录
	 * @throws Exception
	 */
	public static void startDown(Ftp f, String localBaseDir, String remoteBaseDir) throws Exception {
		if (FtpTransfer.connectFtp(f)) {
			try {
				FTPFile[] files = null;
				//切换到远程目录
				boolean changedir = ftp.changeWorkingDirectory(remoteBaseDir);
				
				if (!changedir) {

					ftp.setControlEncoding(encoding);
					files = ftp.listFiles();
					for (int i = 0; i < files.length; i++) {
						try {
							downloadFile(files[i], localBaseDir, remoteBaseDir);
						} catch (Exception e) {
							logger.error(e);
							logger.error("<" + files[i].getName() + ">下载失败");
						}
					}

				}
			} catch (Exception e) {
				logger.error(e);
				logger.error("下载过程中出现异常");
			}
		} else {
			logger.error("链接失败！");
		}

	}

	/**
	 * 
	 * 下载FTP文件 当你需要下载FTP文件的时候， 调用此方法 根据<b>获取的文件名，本地地址，远程地址</b>进行下载
	 * 
	 * @param ftpFile
	 * @param relativeLocalPath
	 * @param relativeRemotePath
	 */
	private static void downloadFile(FTPFile ftpFile, String relativeLocalPath, String relativeRemotePath) {
		if (ftpFile.isFile()) {
			if (ftpFile.getName().indexOf("?") == -1) {
				OutputStream outputStream = null;
				try {
					File locaFile = new File(relativeLocalPath + ftpFile.getName());
					// 判断文件是否存在，存在则返回
					if (locaFile.exists()) {
						return;
					} else {
						outputStream = new FileOutputStream(relativeLocalPath + ftpFile.getName());
						// 从服务器检索命名文件并将文件写入给定的OutputStream中
						ftp.retrieveFile(ftpFile.getName(), outputStream);
						outputStream.flush();
						outputStream.close();
					}
				} catch (Exception e) {
					logger.error(e);
				} finally {
					try {
						if (outputStream != null) {
							outputStream.close();
						}
					} catch (IOException e) {
						logger.error("输出文件流异常");
					}
				}
			}
		} else {
			String newlocalRelatePath = relativeLocalPath + ftpFile.getName();
			String newRemote = new String(relativeRemotePath + ftpFile.getName().toString());
			File fl = new File(newlocalRelatePath);
			if (!fl.exists()) {
				fl.mkdirs();
			}
			try {
				newlocalRelatePath = newlocalRelatePath + '/';
				newRemote = newRemote + "/";
				String currentWorkDir = ftpFile.getName().toString();
				boolean changedir = ftp.changeWorkingDirectory(currentWorkDir);
				if (changedir) {
					FTPFile[] files = null;
					files = ftp.listFiles();
					for (int i = 0; i < files.length; i++) {
						downloadFile(files[i], newlocalRelatePath, newRemote);
					}
				}
				if (changedir) {
					ftp.changeToParentDirectory();
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	// 读取配置文件
	public static String IpAddr;//ftp服务器IP地址ַ
	public static String UserName;//登录用户名
	public static String Pwd;//登录密码
	public static String Path;
	public static String Port;//ftp服务器端口号
	public static String LocalPath;//本地路径
	public static String RemotePath;//远端路径
	
	static {
		Properties p = new Properties();

		// 读取参数
		try {
			p.load(new InputStreamReader(FtpTransfer.class.getClassLoader().getResourceAsStream("ftp.properties"),"UTF-8"));
			IpAddr = p.getProperty("IpAddr");
			UserName = p.getProperty("UserName");
			Pwd = p.getProperty("Pwd");
			Path = p.getProperty("Path");
			Port = p.getProperty("Port");
			LocalPath = p.getProperty("LocalPath");
			RemotePath = p.getProperty("RemotePath");
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException("加载配置文件失败", e);
		}

	}

}
