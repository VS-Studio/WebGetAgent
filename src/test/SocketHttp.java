package test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class SocketHttp {

	public static void main(String[] args) {
		try {
			String host = "www.baidu.com";
			//host = "127.0.0.1";
			Socket socket = new Socket(host, 80);
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			StringBuffer sb = new StringBuffer();
			sb.append("GET / HTTP/1.1\r\n");
			sb.append("Host: "+host+"\r\n");
//			sb.append("Connection: close\r\n");
//			sb.append("User-agent: Mozilla/5.0\r\n");
//			sb.append("Accept-language: zh-cn\r\n");
			sb.append("\r\n");
			
			//sb.append("{\"name\":\"scofier\"}");
			
			out.write(sb.toString().getBytes());
			out.flush();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String tmp = "";
			System.out.println("response:\r\n");
			while ((tmp = reader.readLine()) != null) {
				System.out.println(tmp);
			}
                        
                        System.out.println(readBuffer(in));
			
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
        
        public static String readBuffer(InputStream is) {
		// 此处读入请求数据并做相应的处理
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		try {
			int len;
			while ((len = is.read()) != -1) {
				buff.write(len);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new String(buff.toByteArray());
	}

}
