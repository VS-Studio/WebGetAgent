package test;

import java.io.IOException;
import java.io.InputStream;

public class TestSockRequest {
	private StringBuffer request = new StringBuffer(); // 用于保存所有内容
	InputStream input;
	private byte crlf13 = (byte) 13; //
	private byte crlf10 = (byte) 10;
	


	public static void main(String args[])
	{
		
		System.out.println((char)13);
		get("http://www.vip.com");
	}
	
	public static String get(String _url)
	{
		String s = null;
		return s;
	}
	
	public void setInputStream(InputStream in)
	{
		this.input = in;
	}
	
	public void readData() { // 解析 获得InputStream的数据

		int ChuckSize = 0;
		while ((ChuckSize = getChuckSize()) > 0) // 多个Chucked
		{
			readLenData(ChuckSize + 2);// 读取定长数据
		}
		readLenData(2); // 最后的2位
		
	}
	
	public String getData() {
		return request.toString();
	}
	
	private int getChuckSize() // Chuck大小
	{
		byte[] crlf = new byte[1];
		StringBuffer sb1 = new StringBuffer();
		int crlfNum = 0; // 已经连接的回车换行数 crlfNum=4为头部结束

		try {
			while (input.read(crlf) != -1) // 读取头部
			{
				if (crlf[0] == crlf13 || crlf[0] == crlf10) {
					crlfNum++;
				} else {
					crlfNum = 0;
				} // 不是则清
				sb1.append((char) crlf[0]);
				request = request.append(new String(crlf, 0, 1)); // byte数组相加
				if (crlfNum == 2)
					break;
			}
		} catch (IOException e) {
			System.out.println("Read Http Package Error!");
			return 0;
		}

		return Integer.parseInt((sb1.toString()).trim(), 16); // 16进控制
	}
	
	private void readLenData(int size) // 读取定长数据
	{
		int readed = 0; // 已经读取数
		try {
			int available = 0;// input.available(); //可读数
			if (available > (size - readed))
				available = size - readed;
			while (readed < size) {
				while (available == 0) { // 等到有数据可读
					available = input.available(); // 可读数
				}
				if (available > (size - readed))
					available = size - readed; // size-readed--剩余数
				if (available > 2048)
					available = 2048; // size-readed--剩余数
				byte[] buffer = new byte[available];
				int reading = input.read(buffer);
				request = request.append(new String(buffer, 0, reading)); // byte数组相加
				readed += reading; // 已读字符
			}
		} catch (IOException e) {
			System.out.println("Read readLenData Error!");
		}
	}
}
