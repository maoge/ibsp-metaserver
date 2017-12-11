package benchtest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;


public class SingleSqlBenchTest {
	public static String DB_URL="jdbc:mysql://"
			+ "192.168.14.209:5000/test?characterEncoding=utf8&useSSL=true";
	public static String user = "root";
	public static String pwd = "";
	/*public static String DB_URL="jdbc:mysql://"
			+ "192.168.14.206:9306/ctgmq?characterEncoding=utf8";
	public static String user = "mq";
	public static String pwd = "amqp";*/
	public static String DRIVER_NAME = "com.mysql.jdbc.Driver";
	public static String sql = "insert into test(a,b,c,d,e) values(?,?,?,?,?)";
	public static int loop = 30;
	Connection conn = null ;
	PreparedStatement ps = null;
	
	public static void test(int totalCnt) {
		SingleSqlBenchTest test = new SingleSqlBenchTest();
		try {
			test.init();
			long start = System.currentTimeMillis();	
			long lastTS = start;
			long currTS = start;
			long lastCnt = 0, currCnt = 0;
			while (currCnt < totalCnt) {
				test.testInsert();
				if (++currCnt % 100 == 0) {
					currTS = System.currentTimeMillis();
					long diffTS = currTS - lastTS;
					long avgTPS = currCnt * 1000 / (currTS - start);
					long lastTPS = (currCnt - lastCnt) * 1000 / diffTS;
					
					String info = String.format("send sql count:%d, TPS:%d, avgTPS:%d", currCnt * loop, lastTPS * loop, avgTPS * loop);
					System.out.println(info);
					
					lastTS = currTS;
					lastCnt = currCnt;
				}
			}
		} finally {
			try {
				test.conn.close();
				test.ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void init() {
		try {
			Class.forName(DRIVER_NAME);
			conn = DriverManager.getConnection(DB_URL, user, pwd);
			ps = conn.prepareStatement(sql);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void testInsert() {
		String context = new Date().toString();
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);
			for(int i=0;i<loop;i++) {
				ps.setString(1, context);
				ps.setString(2, context);
				ps.setString(3, context);
				ps.setString(4, context);
				ps.setString(5, context);
				ps.addBatch();   
			}
			ps.executeBatch();
			conn.commit();
			ps.clearBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		test(10000);
	}
}
