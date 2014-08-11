/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db.ssdb;

import db.DbMysql;
import db.PageEntity;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Common;
import utils.Log;

/**
 *
 * @author Administrator
 */
public class TransportMysqlData {
    
    public static void main(String[] args)
    {
        try {
            Log.level(0);
//        DbMysql dbmysql =new DbMysql();
//        
//        for(int i=0;i<15;i++)
//        {
//            new Thread(new Worker(dbmysql,i)).start();
//        }
            
            SSDB ssdb = new SSDB("10.1.72.154", 8889);
            
            String url_hash = "slogowgwelhlwglw";
            String url_hash1 = url_hash + "\r\n";
            
            ssdb.hset(url_hash, "url_hash", url_hash.getBytes());
            ssdb.hset(url_hash, "headers", url_hash1.getBytes());
            ssdb.hset(url_hash, "contents", url_hash1.getBytes());

            Response response = ssdb.hscan(url_hash, "", "", 10);
            
            response.print();
            
            //testSSDB();
        } catch (Exception ex) {
            Logger.getLogger(TransportMysqlData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public static void testSSDB()
    {
        SSDBDao sd = new SSDBDao();
        PageEntity pe = sd.findPage("http://m.mysmartprice.com/product/3633/price");
        
        System.out.println(pe);
    }
}

class Worker implements Runnable{
    private DbMysql dm;
    private int i;
    public Worker(DbMysql dm, int index)
    {
        this.dm = dm;
        this.i = index;
    }
    @Override
    public void run() {
        try {
            //假设是150w，一个线程处理10w数据
            int index = i * 100000;
            
            ResultSet rs = dm._query("select * from page_data limit "+index+",1");
            
            SSDBDao sd = new SSDBDao();
            
            if (rs.next()) {
                Blob blhead = rs.getBlob("headers");
                Blob blcontent = rs.getBlob("content");
                String url = rs.getString("url");
                
                byte[] head = Common.unzip(blhead.getBytes(1, (int)blhead.length()));
                byte[] content = Common.unzip(blcontent.getBytes(1, (int)blcontent.length()));
                //System.out.println(new String(head));
                //System.out.println(new String(content));
                boolean ret = sd.add(url, head, content);
                if(!ret)
                {
                    Log.error("add Error: " + url);
                }else{
                    Log.debug("finish: " + "index: " + index + ",url:" + url);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
}
