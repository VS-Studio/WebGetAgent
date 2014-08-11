/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db.ssdb;

import db.IDao;
import db.PageEntity;
import java.util.Map;
import utils.Common;
import utils.HttpUtils;
import utils.Log;

/**
 *
 * @author Administrator
 */
public class SSDBDao implements IDao{
    private static SSDB ssdb ;
    private static String checkTag = "WebGetAgent-ssdb";
    
    public SSDBDao()
    {
        reConnect();
    }
    
    private static void reConnect()
    {
        if(ssdb != null && checkTag()) return;
        String host = Common.getConfig("ssdb_host");
        int port = Integer.parseInt(Common.getConfig("ssdb_port"));
        try {
            ssdb = new SSDB(host, port);
            ssdb.set(checkTag, "ok");
            Log.debug("[SSDBDao reConnect] reconnect success.");
        } catch (Exception ex) {
            Log.error("[SSDB Connection Failed] host: " + host + ", port " + port + ex.toString());
        }
    }
    

    @Override
    public PageEntity findPage(String url) {
        PageEntity pe = null;
        try {
            String url_hash = Common.md5(url.getBytes());
            Response response = ssdb.hscan(url_hash, "", "", 10);
            
            Map<String,byte[]> obj = response.getItems();
            
            if(obj != null && obj.size() > 6 )
            {
                pe = new PageEntity();
                pe.url = new String(obj.get("url"));
                pe.create_time = new String(obj.get("create_time"));
                pe.last_fetch_time = new String(obj.get("last_fetch_time"));
                pe.last_modified_time = new String(obj.get("last_modified_time"));
                pe.url_hash = new String(obj.get("url_hash"));
                pe.content = Common.unzip(obj.get("content"));
                pe.headers = new String(Common.unzip(obj.get("headers")));
                //更新访问时间
                update_field_time(pe.url_hash, "last_visit_time");
            }

        } catch (Exception ex) {
            reConnect();
            ex.printStackTrace();
            Log.error("[SSDBDao findPage] error . url: " + url + ", " + ex.toString());
        }
        return pe;
    }
    
    private void update_field_time(String url_hash, String field)
    {
        try {
            ssdb.hset(url_hash, field, Common.time().getBytes());
        } catch (Exception ex) {
            Log.error("[SSDBDao update_field_time] error. " + ex.toString());
        }
    }

    @Override
    public boolean updatePageData(String url_hash, byte[] data) {
        boolean ret = false;
        try {
            
            if(url_hash == null || "".equals(url_hash) || data == null)
            {
                Log.error("Empty url_hash or data before update page data.");
                return false;
            }
            String last_modified = HttpUtils.getHeaderItem(new String(data), "Last-Modified", null);
            
            if(last_modified != null && !last_modified.equals(""))
            {
                ssdb.hset(url_hash, "last_modified_time", last_modified.getBytes());
            }
            ssdb.hset(url_hash, "last_fetch_time", Common.time().getBytes());
            ssdb.hset(url_hash, "last_update_time", Common.time().getBytes());
            ssdb.hset(url_hash, "content", Common.zip(data));
            ret = true;
            
        } catch (Exception ex) {
            reConnect();
            ret = false;
            ex.printStackTrace();
            Log.error("[SSDBDao updatePageData] failed. " + ex.toString());
        }
        return ret;
    }

    @Override
    public boolean add(String url, byte[] req, byte[] data) {
        try {
            if(url == null || "".equals(url) || req == null || data == null)
            {
                Log.error("Data empty before insert to mysql.");
                return false;
            }
            
            //解析返回状态码
            int code = HttpUtils.checkHttpCode(data);
            if(code != 200)
            {
                Log.info("Response code invalid, " + code);
                return false;
            }
            
            String url_hash = Common.md5(url.getBytes());
            String create_time = Common.time();
            //解析输出头
            String last_modified_time  = HttpUtils.getHeaderItem(new String(data), "Last-Modified", "");
            
            byte[] contents = Common.zip(data);
            byte[] headers = Common.zip(req);
            System.out.println("REQ: " + new String(req));
            ssdb.hset(url_hash, "url_hash", url_hash.getBytes());
            ssdb.hset(url_hash, "url", url.getBytes());
            ssdb.hset(url_hash, "create_time", create_time.getBytes());
            ssdb.hset(url_hash, "last_fetch_time", "".getBytes());
            ssdb.hset(url_hash, "last_modified_time", last_modified_time.getBytes());
            ssdb.hset(url_hash, "content", contents);
            ssdb.hset(url_hash, "headers", headers);
            ssdb.hset(url_hash, "last_update_time", create_time.getBytes());
            
        } catch (Exception ex) {
            reConnect();
            ex.printStackTrace();
            Log.error("[SSDBDao add] failed. url: " + url + ", " + ex.toString());
            return false;
        }
        return true;
    }
    
    //测试访问
    private static boolean checkTag()
    {
        boolean ret = false;
        if(ssdb != null)
        {
            try {
                if(new String(ssdb.get(checkTag)).equals("ok"))
                {
                    ret = true;
                }
            } catch (Exception ex) {
                ret = false;
            }
        }
        return ret;
    }
}
