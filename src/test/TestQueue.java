package test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestQueue{
	
	public static void main(String args[])
	{
		Pro pro = new Pro();
		Cus cus = new Cus();
		
		new Thread(pro).start();
		new Thread(pro).start();
		
		new Thread(cus).start();
		new Thread(cus).start();
		new Thread(cus).start();
		new Thread(cus).start();
	}
}

class Pro implements Runnable{
	@Override
	public void run() {
		for(int i=0;i<1000;i++)
		{
			MQueue.push("foods"+i);
			try{Thread.sleep(1000);}catch(Exception e){}
		}
	}
}
class Cus implements Runnable{
	@Override
	public void run() {
		for(int i=0;i<1000;i++)
		{
			MQueue.pop();
			try{Thread.sleep(1000);}catch(Exception e){}
		}
	}
}

class MQueue {
	private static Queue<String> queue = new ConcurrentLinkedQueue<String>();
	
	public synchronized static void push(String str)
	{
		try{
			if(queue.size()>10)
			{
				queue.wait();
			}
			queue.offer(str);
			System.out.println(Thread.currentThread().getName() + "push:"+str);
			queue.notifyAll();
		}catch(Exception e){}
	}
	
	public synchronized static String pop()
	{
		String str = null;
		try{
			if(queue.size()<=0)
			{
				queue.wait();
			}
			str = queue.poll();
			System.out.println(Thread.currentThread().getName() + "pop:"+str);
		}catch(Exception e){}
		return str;
	}

}
