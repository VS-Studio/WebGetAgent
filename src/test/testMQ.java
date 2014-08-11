package test;
import com.alibaba.fastjson.JSON;

import mq.ProxyCel;

public class testMQ {
	
	public static void main(String args[])
	{
		String arg = "sss";
		try{
			System.out.println(Integer.parseInt(arg));
		}catch(Exception e)
		{
			System.out.println(e.getCause());
		}
	}
	
	public static void test(String args[])
	{
		ProxyCel pc = new ProxyCel("aaa","www",1);
		pc.headers="heeee";
		pc.last_fetch_time="last_fetch_time";
		pc.last_modified_time="last_modified_time";

		String jsonString = JSON.toJSONString(pc);

		ProxyCel person =JSON.parseObject(jsonString,ProxyCel.class);
		
		System.out.println(person);
	}
}
