package com.cmsz.common;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;


/**
 * 数据库连接：
 * 	连接数据库，获取转售商缩写
 * @author Administrator
 *
 */
public class DBUtil {
	private static Logger logger = Logger.getLogger(DBUtil.class);
	private static BasicDataSource ds;
	
	static{
		Properties p = new Properties();
		try {
			//读取参数
			p.load(DBUtil.class.getClassLoader().getResourceAsStream("db.properties"));
			String driver = p.getProperty("driver");
			String url = p.getProperty("url");
			String user = p.getProperty("userName");
			String pwd = p.getProperty("password");
			String initsize = p.getProperty("initialSize");
			String maxsize = p.getProperty("maxActive");
			//创建连接池
			ds = new BasicDataSource();
			//设置参数
			ds.setDriverClassName(driver);
			ds.setUrl(url);
			ds.setUsername(user);
			ds.setPassword(pwd);
			ds.setInitialSize(new Integer(initsize));
			ds.setMaxActive(new Integer(maxsize));
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException("加载配置文件失败.", e);
		}
	}
	/**
	 * 建立连接
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException{
		return ds.getConnection();
	}
	/**
	 * 关闭连接
	 * @param conn
	 */
	public static void close(Connection conn){
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
				throw new RuntimeException("关闭连接失败", e);
			}
		}
	}
	/**
	 * 事务回滚
	 * @param conn
	 */
	public static void rollback(Connection conn){
		if(conn != null){
			try {
				conn.rollback();
			} catch (SQLException e) {
				logger.error(e);
				throw new RuntimeException("事物回滚失败", e); 
			}
		}
	}
}
