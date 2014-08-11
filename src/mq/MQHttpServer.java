package mq;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.alibaba.fastjson.JSON;
import java.io.OutputStream;

import utils.Common;
import utils.HttpUtils;
import utils.Log;

public class MQHttpServer {

    public static void main(String[] args) {
        Log.level(Integer.parseInt(Common.getConfig("log_level")));

        Socket socket = null;
        try {
            //启动JobServers
            MQ mq = new MQ();
            new JobServers(mq).run();
            //启动web服务，接受用户请求
            int mq_port = Integer.parseInt(Common.getConfig("mq_port"));
            ServerSocket s = new ServerSocket(mq_port, 3);
            Log.info("MQWebServer已经启动: " + mq_port);
            while (true) {
                socket = s.accept();
                Log.debug("连接已建立, 端口号：" + socket.getPort());
                new ServerThread(socket, mq).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ServerThread extends Thread {

    private Socket socket;
    private MQ mq;

    ServerThread(Socket socket, MQ mq) {
        this.socket = socket;
        this.mq = mq;
    }

    @Override
    public void run() {
        InputStream is = null;
        OutputStream out = null;
        try {
            socket.setSoTimeout(30);
            is = socket.getInputStream();
            out = socket.getOutputStream();

            byte[] msg = HttpUtils.readBuffer(is);
            if(msg == null)
            {
                echo("error", out);
                return;
            }
            //截取用户数据
            String jsonString = HttpUtils.getHttpContent(new String(msg));
            if(!jsonString.equals(""))
            {
                ProxyCel person = JSON.parseObject(jsonString, ProxyCel.class);
                //插入队列
                mq.insert(person);
                //输出数据
                echo("ok", out);
            }else{
                echo("error", out);
            }
            
            Log.debug(Thread.currentThread().getName() + " close");
            
        } catch (IOException e) {
            //e.printStackTrace();
            this.echo("error", out);
        } finally {
            try {
                is.close();
                out.close();
                socket.close();
                socket = null;
            } catch (IOException ex) {
                Log.error("ServerThread close error" + ex.getMessage());
            }
        }
    }

    public void echo(String str,OutputStream os) {
        // 1、首先向浏览器输出响应头信息
        PrintStream out = null;
        
        out = new PrintStream(os);
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type:text/html;charset=UTF-8");
        out.println();
        // 2、输出主页信息
        out.println(str);
        out.flush();
    }
}
