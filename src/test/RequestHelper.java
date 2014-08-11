package test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class RequestHelper {

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param params
	 *            请求参数，请求参数应该是name1=value1&name2=value2的形式。
	 * @return URL所代表远程资源的响应
	 */
	public static String sendGet(String url, String params) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlName = url + "?" + params;
			URL realUrl = new URL(urlName);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

			// 建立实际的连接
			conn.connect();
			// 获取所有响应头字段

			Map<String, List<String>> map = conn.getHeaderFields();
			// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}

			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += "\n" + line;
			}
		} catch (Exception e) {
			System.out.println("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 向指定URL发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param params
	 *            请求参数，请求参数应该是name1=value1&name2=value2的形式。
	 * @return URL所代表远程资源的响应
	 */
	public static String sendPost(String url, String params) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(params);
			// flush输出流的缓冲
			out.flush();

//			// 定义BufferedReader输入流来读取URL的响应
//			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//			String line;
//			while ((line = in.readLine()) != null) {
//				result += line + "\n";
//			}
			int info;
			InputStream is = conn.getInputStream();
			while((info = is.read()) != -1)
			{
				result +=(""+(char)info);
			}
			
		} catch (Exception e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	
	
	public static void testSendGet() {
		String url = "http://127.0.0.1:8988/web_test/a.jsp";
		String str = RequestHelper.sendGet(url, null);
		System.out.println(str);
	}
	
	public static void testSendPost(String data) {
		String url = "http://127.0.0.1:8988/web_test/UserLoginServlet";
		
		//url = "http://www.scofier.com";
		
		String params = "{\"url_hash\":\"admin_"+data+"\",\"last_modified_time\":\"123456\",\"content\":\""+data+"\"}";
		String str = RequestHelper.sendPost(url, params);

//		JSONObject jo = JSONObject.parseObject(str);
		System.out.println(str);
//		
//		System.out.println(jo.get("username"));
//		System.out.println(jo.get("password"));
	}
	
	public static void post()
	{
		
		try {
			URL url = new URL("http://localhost:8988"); 
			HttpURLConnection huc = (HttpURLConnection) url.openConnection(); 
			//设置允许output 
			huc.setDoOutput(true); 
			//设置为post方式 
			huc.setRequestMethod("POST"); 
			huc.addRequestProperty("user-agent","mozilla/4.7 [en] (win98; i)"); 
			StringBuffer sb = new StringBuffer(); 
			sb.append("username=scofier"); 
			sb.append("&password=password"); 

			//post信息 
			OutputStream os = huc.getOutputStream(); 
			os.write(sb.toString().getBytes("utf-8")); 
			os.close(); 

			BufferedReader br;
			
			br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
			huc.connect(); 

			String line = br.readLine(); 

			while(line != null){ 



			System.out.println(line); 


			line = br.readLine(); 

			} 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}
	
	public static void main(String args[])
	{
		

		
		for(int i=0;i<1;i++)
		{
			testSendPost("data_"+i);
		}
		
	}
}
