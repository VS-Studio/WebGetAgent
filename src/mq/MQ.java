package mq;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import utils.Log;
import db.PageEntity;
import utils.Common;

public class MQ {
    
    private static int queueMaxSize = Integer.parseInt(Common.getConfig("mq_max_size"));

    private static Queue<ProxyCel> queue = new ConcurrentLinkedQueue<ProxyCel>();

    public synchronized boolean insert(ProxyCel pc) {
        push(pc);
        return true;
    }

    public synchronized boolean insert(String str) {
        push(new ProxyCel(str));
        return true;
    }

    public synchronized boolean insert(PageEntity pe) {
        ProxyCel pc = new ProxyCel(pe.url_hash, pe.last_modified_time, 1);
        pc.headers = pe.headers;
        pc.url = pe.url;
        pc.last_fetch_time = pe.last_fetch_time;

        push(pc);
        return true;
    }

    public boolean push(ProxyCel pc) {
        if (queue.size() > queueMaxSize) {
            // do some thing
            Log.error("mq is full. max size:20000");
            try {
                this.wait();
            } catch (Exception e) {
            }
        }else{
            //插入排重
            if (!queue.contains(pc)) {
                queue.offer(pc);
            } else {
                Log.info("url already in mq: " + pc.url);
            }
        }
        Log.info("mq size: " + queue.size());
        this.notifyAll();
        return true;
    }

    public synchronized ProxyCel get() {
        if (queue.isEmpty()) {
            try {
                this.wait();
            } catch (Exception e) {
            }
        }else{
            this.notifyAll();
        }
        int size = queue.size();
        if(size > 0)
        {
            Log.info("mq size: " + size);
        }
        return queue.poll();
    }
}
