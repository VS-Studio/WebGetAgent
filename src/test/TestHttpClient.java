package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestHttpClient {
	
	public static void main(String args[])
	{
		get("http://www.baidu.com");
	}
	
	public static String get(String _url)
	{
		String s = null;
		
		try {
			URL url = new URL(_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
			conn.connect();

			InputStream in;
			in = conn.getInputStream();
			//in = new GZIPInputStream(in);
			
			System.out.println(conn.getHeaderFields());

			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			
			while((s=bin.readLine())!=null){
				System.out.println(s);
			}
			bin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return s;
	}

}
