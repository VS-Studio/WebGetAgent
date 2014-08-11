package test;

import mq.MQ;
import mq.ProxyCel;
import utils.Log;

public class readMQ {
	protected static Object flat = new Object();
	
	public static void main(String args[])
	{
		
		
		run(new MQ());
		
	}
	
	public static void insert()
	{
		flat.notify();
	}
	
	public static void run(MQ mq) {
		ProxyCel pcel = null;
		Log.info("mq started.");
		while (true) {
			pcel = mq.get();
			if (pcel == null) {
				Log.info("队列为空.");
				try {
					Thread.sleep(5 * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				
				Log.debug("expire time: " + pcel);

			}
		}
	}

}

