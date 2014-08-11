package network;

import com.alibaba.fastjson.JSONObject;
import db.DBClient;
import db.DbMysql;
import db.PageEntity;
import java.net.*;
import java.io.*;
import mq.ProxyCel;
import utils.Common;
import utils.HttpUtils;
import utils.Log;

/**
 * 一个简单的Socket实现的HTTP代理服务器。
 *
 * @author scofier
 */
public class WebGetAgent {

    public static void main(String[] args) {
        Socket socket = null;
        Log.level(Integer.parseInt(Common.getConfig("log_level")));      
        
        try {
            int port = Integer.parseInt(Common.getConfig("port"));
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            // 创建一个监听8000端口的服务器Socket
            ServerSocket s = new ServerSocket(port, 3);
            System.out.println("WebGetAgent已经在"+port+"端口启动\n");
            while (true) {
                socket = s.accept();
                System.out.println("连接已建立, 端口号：" + socket.getPort());
                new ProxyThread(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ProxyThread extends Thread {
    private Socket socket;

    ProxyThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream is = null;
        OutputStream out = null;
        try {
            socket.setSoTimeout(30);
            is = socket.getInputStream();
            out = socket.getOutputStream();
            //获取用户的访问请求
            byte[] header = HttpUtils.readHeader(is, -1);
            String strHeader = new String(header);
            String url = HttpUtils.getHeaderRequest(strHeader).get("url");
            if(url == null || "".equals(url))
            {
                Log.error("Not support proxy type: " + strHeader);
                return ;
            }
            //检索数据库的记录
            Log.debug("begin process url: " + url);
            byte[] ret = checkInDb(url);
            //网络请求数据返回并保存数据到数据库
            if(ret == null)
            {
                //用webkit解析页面
                if(url.indexOf("webgetagent_with=ajax") > 0)
                {
                    ret = Common.requestWebkit(url);
                }else{
                    ret = HttpUtils.requestWithRawHttpHead(strHeader);
                }
                
                DBClient.add(url, header, ret);
            }
            //输出数据给客户端
            out.write(ret);
            Log.debug("Output dataSize: " + ret.length);
            
        } catch (IOException e) {
            //e.printStackTrace();
            Log.error("ProxyThread : " + e.getMessage());
        } finally{
            try {
                out.close();
                is.close();
                socket.close();
            } catch (IOException ex) {
                //ex.printStackTrace();
                Log.error("ProxyThread : " + ex.getMessage());
            }
        }
    }

    private byte[] checkInDb(String url) {
        byte[] data = null;
        PageEntity pe = DBClient.findPage(url);
        if (pe != null && pe.content != null) {
            Log.info("get page from db " + pe.url);
            data = pe.content;
            //插入消息队列
            pe.content = null;
            sendMQ(pe);
            Log.debug("finish output page data , url: " + url);
            pe = null;
        }else{
            Log.info("no data return from db : " + url);
        }
        return data;
    }

    static void sendMQ(PageEntity pe) {
        try {
            String host = "http://" + Common.getConfig("host") + ":" + Common.getConfig("mq_port");
            String params = JSONObject.toJSONString(ProxyCel.parse(pe));
            Log.debug("PageEntity:" + params);
            HttpUtils.sendPost(host, params);
        } catch (Exception e) {
            Log.error("mq server is not start.");
        }
    }
    
}
