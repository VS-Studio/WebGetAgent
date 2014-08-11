package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpUtils {

    public static String getHttpContent(String str) {
        String ret = "";
        int pos = str.indexOf("\r\n\r\n");
        if (pos != -1) {
            ret = str.substring(pos + 4);
        }
        return ret;
    }
    
    public static HashMap<String,String> getHeaderRequest(String header)
    {
        HashMap<String,String> rets = new HashMap<String,String>();
        String[] headers = header.split("\r\n");
        if(headers != null && !"".equals(headers[0]))
        {
            String[] requests = headers[0].split(" ");
            rets.put("method", requests[0]);
            rets.put("port", "80");//默认80端口
            rets.put("protocol", "http");
            if(requests[1] != null)
            {
                rets.put("url", requests[1]);
                try{
                    URL url = new URL(requests[1]);
                    rets.put("host", url.getHost());
                    rets.put("port", "" + ((url.getPort() == -1) ? url.getDefaultPort() : url.getPort()));
                    rets.put("protocol", url.getProtocol());
                }catch(Exception e){
                    for(String h: headers)
                    {
                        if(h.startsWith("Host:"))
                        {
                            String host = h.split(":")[1].trim();
                            rets.put("host", host);
                            rets.put("url", "http://" + host + requests[1]);
                            break;
                        }
                    }
                }
            }
        }
        return rets;
    }

   
    public static String getHeaderItem(String header, String item, String def_value) {
        String ret = def_value;
        HashMap<String, String> headers = getHeader(header);
        if (headers.containsKey(item)) {
            ret = headers.get(item);
        }
        return ret;
    }
    
     /**
     * 获取消息头
     *
     * @param header
     * @return
     */
    public static HashMap<String, String> getHeader(String header) {
        HashMap<String, String> ret = new HashMap<String, String>();
        String headers[] = header.split("\r\n");
        if (headers != null) {
            for (String head : headers) {
                String[] kv = head.split(":");
                if (kv.length > 1) {
                    ret.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
        return ret;
    }
    /**
     * 以原始的http头发起http请求
     * @param head
     * @return 
     */
    public static byte[] requestWithRawHttpHead(String head) {
        Log.debug("Proxy request head: \n" + head);
        byte[] ret = null;
        //解析http host地址
        HashMap<String,String> headerRequest = getHeaderRequest(head);
        String host = headerRequest.get("host");
        String port = headerRequest.get("port");
        Socket socket = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            socket = new Socket(host, Integer.parseInt(port));
            is = socket.getInputStream();
            os = socket.getOutputStream();
            //代理发送网络请求
            os.write(head.getBytes());
            os.flush();
            ret = readBuffer(is);
            Log.debug("Proxy return allSize: " + ret.length);
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("requestWithRawHttpHead : " + e.toString());
        } 
        finally
        {
            try {
                os.close();
                is.close();
                socket.close();
                socket = null;
                os = null;
                is = null;
            } catch (IOException ex) {
                Log.error("requestWithRawHttpHead close error : " + ex.toString());
            }
        }
        return ret;
    }

    /**
     * 字节读取http头，字符串方式读取在合并编码的时候有问题
     *
     * @param is
     * @param length
     * @return
     */
    public static byte[] readHeader(InputStream is, int length) {
        // 此处读入请求数据并做相应的处理
        byte[] ret = null;
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        try {
            int len;
            ArrayList<String> al = new ArrayList<String>();
            while ((len = is.read()) != -1) {
                buff.write(len);
                String tmp = Common.Fifo("" + len, 4, al);
                if (tmp.equals("[13, 10, 13, 10]")) {
                    Log.debug("[http head finished]");
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.error("readHeader failed. " + ex.toString());
        }
        ret = buff.toByteArray();

        return ret;
    }

    /**
     * 读取http响应数据体
     *
     * @param in
     * @param header
     * @return
     */
    public static byte[] readBody(InputStream in, byte[] header) {
        //解析输出头
        HashMap<String, String> headers = HttpUtils.getHeader(new String(header));
        int content_length = -1;
        if (headers.containsKey("Content-Length")) {
            content_length = Integer.parseInt(headers.get("Content-Length"));
        } else {
            //解析chunked
            if (headers.containsKey("Transfer-Encoding") && headers.get("Transfer-Encoding").equals("chunked")) {
                return readChunkData(in);
            }
        }
        //解析gzip
//        if(headers.containsKey("Content-Encoding") && headers.get("Content-Encoding").equals("gzip"))
//        {
//            //ret = Common.unzip(ret);
//        }

        return readBuffer(in, content_length);
    }
    


    /**
     * 读取chunked格式数据
     *
     * @param in
     * @return
     */
    public static byte[] readChunkData(InputStream in) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int ChuckSize = 0;
            byte[] tmp = null;
            while ((ChuckSize = getChuckSize(in)) > 0) // 多个Chucked
            {
                baos.write(Integer.toHexString(ChuckSize).getBytes());
                baos.write("\r\n".getBytes());
                tmp = readBuffer(in, ChuckSize + 2);// 读取定长数据
                baos.write(tmp);
                //Log.debug("cksize: " + ChuckSize +", hex: " + Integer.toHexString(ChuckSize));
            }
            //注意这里是字符串0，hex=30
            baos.write("0".getBytes());
            baos.write("\r\n\r\n".getBytes());
            Log.info("[readChunkData] chunked body Size: " + baos.size());
        } catch (Exception e) {
            //e.printStackTrace();
            Log.error("readChunkData : " + e.toString());
        }
        return baos.toByteArray();
    }

    /**
     * 获取chunked格式数据的大小
     *
     * @param input
     * @return
     */
    public static int getChuckSize(InputStream input) // Chuck大小
    {
        byte[] crlf = new byte[1];
        StringBuffer sb1 = new StringBuffer();
        int crlfNum = 0; // 已经连接的回车换行数 crlfNum=4为头部结束
        try {
            while (input.read(crlf) != -1) // 读取头部
            {
                if (crlf[0] == 13 || crlf[0] == 10) {
                    crlfNum++;
                } else {
                    crlfNum = 0;
                }
                sb1.append((char) crlf[0]);
                if (crlfNum == 2) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Read Http Package Error!");
            return 0;
        }
        if (sb1.toString().equals("")) {
            return 0;
        }
        return Integer.parseInt((sb1.toString()).trim(), 16); // 16进制  
    }

    public static byte[] addLastModifiedTime(String headers,
            String last_modified_time) {

        String line = null;
        ArrayList<String> al = new ArrayList<String>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            BufferedReader dr = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(headers.getBytes())));
            while (((line = dr.readLine()) != null)) {
                al.add(line);
            }

            al.add(al.size() - 2, "If-Modified-Since: " + last_modified_time);

            for (String tmp : al) {
                tmp = tmp + "\r\n";
                baos.write(tmp.getBytes());
            }

        } catch (IOException e) {
            //e.printStackTrace();
            Log.error("addLastModifiedTime : " + e.toString());
        }

        return baos.toByteArray();
    }

    public static int checkHttpCode(byte[] data) {
        int ret = 404;
        BufferedReader dr = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(data)));
        try {
            String line = dr.readLine();
            Log.debug("checkHttpCode : " + line);
            String[] items = line.split(" ");
            if (items.length > 2) {
                ret = Integer.parseInt(items[1]);
            }
        } catch (IOException e) {
            ret = 404;
            Log.error("Integer.parseInt" + e.toString());
        }
        return ret;
    }

    /**
     * 读取指定长度的数据
     *
     * @param is
     * @param length -1为读取全部
     * @return
     */
    public static byte[] readBuffer(InputStream is, int length) {
        // 此处读入请求数据并做相应的处理
        byte[] ret = null;
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        Log.debug("read-limit-len: " + length);
        try {
            int len;
            byte[] buf = new byte[1];
            while (((len = is.read(buf)) != -1)) {
                buff.write(buf, 0, len);
                if (length > -1 && buff.size() == length) {
                    //Log.debug("[Content-Length limit] length: " + length);
                    break;
                }
            }
            ret = buff.toByteArray();
        } catch (Exception ex) {
            Log.error("readBuffer : " + ex.toString());
        }
        return ret;
    }
    
     /**
     * 读取整个Http响应数据,处理Content-Length
     * @param in
     * @return 
     */
    public static byte[] readBuffer(InputStream is)
    {
        byte[] ret = null;
        //读取返回数据
        byte[] header = readHeader(is, -1);
        if(header == null)
        {
            Log.error("ReadHeader failed, null return.");
            return null;
        }
        String strHeader = new String(header);
        Log.debug("Proxy return head: \n" + strHeader);
        Log.info("Proxy return headSize: " + header.length);
        //GET请求没有实体
        String method = getHeaderRequest(strHeader).get("method").toLowerCase();

        if(!"get".equals(method))
        {
            Log.info("Read http entity.");
            ret = readBody(is, header);
            //合并响应头和实体
            ret = Common.byteMerger(header, ret);
        }else{
            ret = header;
        }
        
        return ret;
    }

  
    public static byte[] getOutData(InputStream is1)
            throws IOException {
        ByteArrayOutputStream baos_out_data = new ByteArrayOutputStream();
        int ir;
        byte bytes[] = new byte[2048];
        while (true) {
            try {
                if ((ir = is1.read(bytes)) > 0) {
                    baos_out_data.write(bytes, 0, ir);
                } else if (ir < 0) {
                    break;
                }
            } catch (InterruptedIOException e) {
            }
        }

        baos_out_data.flush();
        return baos_out_data.toByteArray();
    }
    
     /**
     * 向指定URL发送POST方法的请求
     *
     * @param url 发送请求的URL
     * @param params 请求参数，请求参数应该是name1=value1&name2=value2的形式。
     * @return URL所代表远程资源的响应
     */
    public static String sendPost(String url, String params) {
        PrintWriter out = null;
        URLConnection conn = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(params);
            // flush输出流的缓冲
            out.flush();
            out.close();
            int info;
            InputStream is = conn.getInputStream();
            while ((info = is.read()) != -1) {
                result += ("" + (char) info);
            }
            conn = null;

        } catch (Exception e) {
            Log.error("发送POST请求出现异常！" + e.toString());
            e.printStackTrace();
        } // 使用finally块来关闭输出流、输入流
        finally {
            
        }
        return result;
    }

}
