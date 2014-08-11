package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import utils.Common;
import utils.HttpUtils;

public class TestString {

	public static String TestMd5(byte[] s) {
		String ret = new String(Common.md5(s));
		return ret;
	}
	
	public static String testCharset()
	{
		String str = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\" />";
		
		str = str.split("charset=")[1].split("\"")[0];
		System.out.println(str);
		return str;
	}

	public static void testLastModified()
	{
		String host = "www.baidu.com";
		String hd = "";
		hd +="GET http://www.baidu.com HTTP/1.0\r\n";
		//hd +="Accept: */*\n";
		//hd +="If-Modified-Since: Wed, 17 Oct 2007 02:15:55 GMT\n";
		hd +="Host: "+host+"\r\n";
		hd +="\r\n";
		
		try {
			Socket sock = new Socket(host,80);
			sock.setSoTimeout(50);
			OutputStream os = sock.getOutputStream();
			InputStream is = sock.getInputStream();
			
			os.write(hd.getBytes());
			os.flush();
			
			byte[] baos = HttpUtils.getOutData(is);
			
			Common.pl("----" + new String(baos));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void getProxy()
	{
		try {
			ServerSocket ssock = new ServerSocket(8989,3);
			
			InputStream is = null;
			//ByteArrayOutputStream baos = null;
			while(true)
			{
				Socket cs = ssock.accept();
				cs.setSoTimeout(50);
				is = cs.getInputStream();
				//ByteArrayOutputStream baos = Common.getOutData(is);
				int i;
				while(true)
				{
					try{
						i = is.read();
						if(i == -1) break;
						System.out.print(""+(char)i);
					}catch(Exception e)
					{
						
					}
					
				}
				
				System.out.println("===="+ cs.getInetAddress());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String bytetoString(byte[] digest) {
		String str = "";
		String tempStr = "";

		for (int i = 0; i < digest.length; i++) {
			tempStr = (Integer.toHexString(digest[i] & 0xff));
			if (tempStr.length() == 1) {
				str = str + "0" + tempStr;
			} else {
				str = str + tempStr;
			}
		}
		return str.toLowerCase();
	}
	
	public static String Testsha1(String inStr) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] digest = md.digest(inStr.getBytes());
		String outStr = bytetoString(digest);
		return outStr;
	}
        
        
        public static void testStreamReset()
        {
            String str = "abcdefghijklmnopqrstuvwxyz";
            
            ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
            byte[] buff = new byte[5];
            
            try{
                bais.read(buff);
                
                System.out.println(new String(buff));
                
                bais.reset();
            
                bais.read(buff);
                
                System.out.println(new String(buff));
                
            }catch(Exception e){}
            
            
            
        }
        
        public static String Fifo(String a,int size, ArrayList<String> al)
        {
            //PriorityQueue<String> al= new PriorityQueue<String>(size);
            if(al.size() >= size)
            {
                al.remove(0);
            }
            al.add(a);
            return al.toString();
        }
        
        public static void testalist()
        {
            ArrayList<String> al= new ArrayList<String>();
            
            String str = "l\r\n\r\nw";
            byte[] a = str.getBytes();
            for(int i=0;i<a.length;i++)
            {
                String ret = Fifo(""+a[i], 4 ,al);
                System.out.println(ret);
                if(ret.equals("[13, 10, 13, 10]"))
                {
                    System.out.print("-----");
                }
                
                
            }
            
           
            
        }

	public static void main(String[] args) {
	StringBuilder sb = new StringBuilder();
	sb.append("25\r\n");
	sb.append("This is the data in the first chunk\r\n");
	sb.append("\r\n1A\r\n");
	sb.append("and this is the second one");
	sb.append("\r\n0\r\n\r\n");
	ByteBuffer in = ByteBuffer.allocate(1024);
	in.put(sb.toString().getBytes());
	in.flip();
	int start = in.position();
	int end = in.limit();
	ByteBuffer content = ByteBuffer.allocate(1024);
	while (true) { // 封包循环
		for (int i = start; i < end - 1; i++) {
			if (in.get(i) == 0x0D && in.get(i + 1) == 0x0A) {
				byte[] nums = new byte[i - start];
				in.get(nums);
				// 丢弃\r\n
				in.get(new byte[2]);
				int num = Integer.parseInt(new String(nums), 16);
				byte[] strs = new byte[num];
				in.get(strs);
				content.put(strs);
				// 丢弃\r\n
				in.get(new byte[2]);
				start = i + 4 + num;
				break;
			}
		}
		if (in.get(start) == 0x30 && in.get(start + 1) == 0x0D && in.get(start + 2) == 0x0A && in.get(start + 3) == 0x0D && in.get(start + 4) == 0x0A) {
			content.flip();
			in.get(new byte[5]);
			break;
		}
	}
	System.out.println(new String(content.array(), 0, content.limit()));
}

}
