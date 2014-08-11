/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db;

/**
 *
 * @author Administrator
 */
public interface IDao {
    public PageEntity findPage(String url);
    public boolean updatePageData(String url_hash, byte[] data);
    public boolean add(String url, byte[] req, byte[] data);
    
}
