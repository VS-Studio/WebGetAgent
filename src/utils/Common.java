package utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Common {
    private static long startTimestamp = System.currentTimeMillis();
    private static String configPath = "config.properties";
    private static Properties pros = new Properties();
    
    static{
        try {
            pros.load(new BufferedInputStream(new FileInputStream(configPath)));
        } catch (IOException ex) {
            Log.error("config file is not exist : " + configPath);
        }
    }

    public static String[] strHost(String request) {
        Log.debug("RequestHeader: \n" + request);
        int n;
        String port = "80";
        String url = "";
        String[] _tmp = request.split(" ");
        if (_tmp.length > 1) {
            url = request = _tmp[1];
        } else {
            url = request = _tmp[0];
        }

        n = request.indexOf("//");
        if (n != -1) {
            request = request.substring(n + 2);
        }
        n = request.indexOf('/');
        if (n != -1) {
            request = request.substring(0, n);
        }
        // 分析可能存在的端口号
        n = request.indexOf(":");
        if (n != -1) {
            port = (request.substring(n + 1));
            request = request.substring(0, n);
        }

        return new String[]{request, port, url};
    }

    public static String md5(byte[] source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);
            StringBuffer buf = new StringBuffer();
            for (byte b : md.digest()) {
                buf.append(String.format("%02x", b & 0xff));
            }
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] zip(byte[] in) {
        boolean open = true; // 关闭压缩
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            OutputStream os = new GZIPOutputStream(bos);
            os.write(in);
            os.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (open) ? bos.toByteArray() : in;
    }

    public static byte[] unzip(InputStream in) throws Exception {
        boolean open = true;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is;
        is = (open) ? new GZIPInputStream(in) : in;
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        is.close();
        in.close();
        bos.close();

        return bos.toByteArray();
    }

    public static byte[] unzip(byte[] in) {
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        try {
            return unzip(bais);
        } catch (Exception e) {
            Log.error(e.getMessage());
            return in;
        }
    }

    public static String time() {
        return time("yyyy-MM-dd HH:mm:ss", new Date());
    }

    public static String time(String format, Date date) {
        java.text.DateFormat format1 = new java.text.SimpleDateFormat(format);
        return format1.format(date);
    }

    public static long timestamp(String time) {
        long ret = 0;
        try {
            java.text.DateFormat format1 = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date d = format1.parse(time);
            ret = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getConfig(String s) {
        return getConfig(s, "");
    }

    public static String getConfig(String s, String defaultValue) {
        return pros.getProperty(s, defaultValue);
    }

    public static void pl(String s) {
        String time = "[" + time() + "]";
        System.out.println(time + s);
    }
    
    
    public static void logCostTime()
    {
        Log.debug("Cost-time: " + (System.currentTimeMillis() - startTimestamp));
    }

    public static void main(String args[]) {
        long a1 = new Date().getTime();
        long a2 = timestamp("2014-04-22 03:54:00");
        long s = a1 / 1000 - a2 / 1000;
        pl("a1:" + a1);
        pl("a2:" + a2);
        pl("time:" + s);
    }

    /**
     * 用先进先出队列，检查连续出现的字符
     *
     * @param a
     * @param size
     * @param al
     * @return
     */
    public static String Fifo(String a, int size, ArrayList<String> al) {
        if (al.size() >= size) {
            al.remove(0);
        }
        al.add(a);
        return al.toString();
    }

    public static void printTime(String pos, long start) {
        long end = System.currentTimeMillis() - start;
        Log.info("[" + pos + "] cost time : " + end + " \n");
    }

    /**
     * java 合并两个byte数组
     *
     * @param byte_1
     * @param byte_2
     * @return
     */
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    public static void writeResponseBodyToFile(byte[] in, String filename) {
        File f = new File(filename);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(in);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
    }

    public static Integer getCurrentThreadID()
    {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        return Integer.parseInt(name.substring(0, name.indexOf("@")));
    }

    public static void mkdir(String dir)
    {
        File file = new File(dir);
        file.mkdirs();
        file = null;
    }
    
    public static byte[] requestWebkit(String url)
    {
        try {
            String webKitProcessCmd = Common.getConfig("webkit_process_cmd");
            String request = webKitProcessCmd + " " + url + "";
            Log.info("[requestWebkit]" + request);
            Process p = Runtime.getRuntime().exec(request);
            // 取得命令结果的输出流
            InputStream fis = p.getInputStream();
            byte[] out = HttpUtils.getOutData(fis);
            fis.close();
            if(out.length > 0)
            {
                //构造一个http头输出
                String header = "HTTP/1.0 200 OK\r\n"
                        + "Content_Type:text/html\r\n"
                        + "Content_Length:" + out.length + "\r\n"
                        + "\r\n";
                return byteMerger(header.getBytes(), out);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
