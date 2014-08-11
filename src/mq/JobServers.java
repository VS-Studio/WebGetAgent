package mq;

import db.DBClient;
import java.util.Date;

import utils.Common;
import utils.HttpUtils;
import utils.Log;

public class JobServers {
    private MQ mq;
    public JobServers(MQ mq)
    {
        this.mq = mq;
    }
    
    public void run() {
        
        createThread(Integer.parseInt(Common.getConfig("mq_thread_num", "5")));
        
        //启动监控线程
        new Thread(new Monitor(this, Thread.currentThread())).start();
        
    }
    
    public void createThread(int num)
    {
        for (int i = 0; i < num; i++) {
            new ConcumerThread(mq).start();
        }
    }
    
}

class ConcumerThread extends Thread {

    private MQ mq;

    public ConcumerThread(MQ mq) {
        this.mq = mq;
    }

    public void run() {
        ProxyCel pcel = null;
        String thread = Thread.currentThread().getName();
        Log.info("mq thread started : " + thread);
        while (true) {
            //pcel为空会阻塞当前线程
            if((pcel = mq.get()) != null)
            {
                Log.info(thread + " get request, url:  " + pcel.url);
                if (pcel != null && pcel.last_fetch_time != null && !pcel.last_fetch_time.equals("")) {
                    long expire = new Date().getTime() - Common.timestamp(pcel.last_fetch_time);
                    Log.debug("expire time: " + String.valueOf(expire));
                    if (expire > (300 * 1000)) {
                        new SendRequest(pcel).start();
                    }
                } else {
                    new SendRequest(pcel).start();
                }
            }
        }
    }
}

class SendRequest {

    private ProxyCel pc;

    public SendRequest(ProxyCel pc) {
        this.pc = pc;
    }

    public void start() {
        byte[] headers = null;
        try {
            headers = this.pc.headers.getBytes();
            if(headers == null)
            {
                Log.error("[JobServers] header null, " + this.pc.url);
                return;
            }
            String thread = Thread.currentThread().getName();
            //加上Last_modified头
            if (this.pc.last_modified_time != null && !this.pc.last_modified_time.equals("")) {
                Log.info(thread + " request with last_modified: " + this.pc.last_modified_time);
                headers = HttpUtils.addLastModifiedTime(this.pc.headers, this.pc.last_modified_time);
                Log.debug("===header\n" + new String(headers));
            }
            Log.info(thread + " request with url: " + this.pc.url);
            
            byte[] msg;
            //webkit解析
            if(this.pc.url.indexOf("webgetagent_with=ajax") > 0)
            {
                msg = Common.requestWebkit(this.pc.url);
            }else{
                msg = HttpUtils.requestWithRawHttpHead(new String(headers));
            }
            
            DBClient.updatePageData(Common.md5(this.pc.url.getBytes()), msg);

            Log.info(thread + " get data return, size:  " + msg.length);

            
        } catch (NumberFormatException e) {
            Log.error("[JobServers] " + e.toString() + ", url : " + this.pc.url + ", headers: " + new String(headers));
            e.printStackTrace();
        }
    }

}