/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db;

import db.ssdb.SSDBDao;
import utils.Common;

/**
 *
 * @author Administrator
 */
public class DBClient {
    
    private static IDao idao;
    
    static{
        String dbo = Common.getConfig("data_engine");
        if(dbo.equals("ssdb"))
        {
            idao = new SSDBDao();
        }
        else
        {
            idao = new DbMysql();
        }
        
    }
    
    public static PageEntity findPage(String url) {
        return idao.findPage(url);
    }
    
    public static boolean updatePageData(String url_hash, byte[] data) {
        return idao.updatePageData(url_hash, data);
    }
    
    
    public static boolean add(String url, byte[] req, byte[] data) {
        return idao.add(url, req, data);
    }
    
}
