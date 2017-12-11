package benchtest;

import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

public class MuliSqlBench {
	private static AtomicLong[] normalCntVec;
	private static AtomicLong maxTPS;
	
	private class SqlBench implements Runnable{
		
		private String threadName;
		private AtomicLong normalCnt;
		private boolean isRunning = true;
		
		public SqlBench(String threadName, AtomicLong normalCnt) {
			this.threadName = threadName;
			this.normalCnt = normalCnt;
		}
		
		@Override
		public void run() {
			SingleSqlBenchTest test = new SingleSqlBenchTest();
			try {
				test.init();
				while(isRunning) {
					test.testInsert();
					long cnt = normalCnt.incrementAndGet();
					if (cnt % 1000 == 0) {
						String info = String.format("%s send sql count:%d", threadName, cnt * SingleSqlBenchTest.loop);
						System.out.println(info);
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
		
		public void stopRunning() {
			isRunning = false;
		}
	}
	
	public void test(int threadCount, int totalTime) {
		normalCntVec = new AtomicLong[threadCount];
		for (int i = 0; i < threadCount; i++) {
			normalCntVec[i] = new AtomicLong(0);
		}
		maxTPS = new AtomicLong(0);

		Statistic stat = new Statistic(maxTPS, normalCntVec);
		Vector<SqlBench> theadVec = new Vector<SqlBench>(threadCount);
		int idx = 0,sIdx = 0;
		long start = System.currentTimeMillis();
		long totalDiff = 0;

		for (; idx < threadCount; idx++) {
			String threadName = String.format("%s%02d", "mysql-test-", idx);
			SqlBench sqlBench = new SqlBench(threadName, normalCntVec[idx]);
			Thread thread = new Thread(sqlBench);
			thread.start();
			theadVec.add(sqlBench);
		}

		while (totalDiff < totalTime) {
			long curr = System.currentTimeMillis();
			totalDiff = (curr - start) / 1000;

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (; sIdx < threadCount; sIdx++) {
			theadVec.get(sIdx).stopRunning();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		stat.StopRunning();
	}
	
	public static void main(String[] args) {
		//第一个字段是线程数量  第2个参数是执行时间
		MuliSqlBench test = new MuliSqlBench();
		test.test(1,10);
	}
}
