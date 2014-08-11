/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mq;

import java.util.HashMap;
import utils.Log;

/**
 * @author Administrator
 */

 //监控当前Worker线程情况
public class Monitor implements Runnable{
    //1分钟检测一次
    private static int interval = 60000;
    //一次补充30个线程
    private static int minThread = 30;
    JobServers o;
    Thread t;
    public Monitor(JobServers o, Thread t)
    {
        this.t = t;
        this.o = o;
    }
    
    @Override
    public void run() {
        while(true)
        {
            try {
                Thread[] threads = this.getAllThread(t);
                printLog(threads);
                if(threads.length < minThread)
                {
                    Log.info("[Revoke new Thread] thread-num: " + threads.length);
                    o.createThread(minThread);
                }
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    public Thread[] getAllThread(Thread t)
    {
        ThreadGroup group = t.getThreadGroup();
        ThreadGroup topGroup = group;
        // 遍历线程组树，获取根线程组
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        // 激活的线程数加倍
        int estimatedSize = topGroup.activeCount() * 2;
        Thread[] slackList = new Thread[estimatedSize];
        // 获取根线程组的所有线程
        int actualSize = topGroup.enumerate(slackList);
        // copy into a list that is the exact size
        Thread[] list = new Thread[actualSize];
        System.arraycopy(slackList, 0, list, 0, actualSize);
        
        return list;
    }
    
    
    public void printLog(Thread[] threads)
    {
        HashMap<String,Integer> hm = new HashMap<String,Integer>();
        
        for (Thread thread : threads) {
            if(thread.getName().startsWith("Thread-"))
            {
                int old = hm.get(thread.getState().name()) == null ? 0 : hm.get(thread.getState().name());
                hm.put(thread.getState().name(), new Integer(old + 1));
            }
        }
        Log.info("[Monitor] " + hm);
        System.out.println(hm);
    }
}
