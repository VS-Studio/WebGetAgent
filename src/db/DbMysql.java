package db;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.Common;
import utils.HttpUtils;
import utils.Log;

public class DbMysql implements IDao{

    private static DbMysql dbmysql = new DbMysql();
    
    protected Connection conn() {
        Connection conn = null;
        // 驱动程序名
        String driver = "com.mysql.jdbc.Driver";

        // URL指向要访问的数据库名scutcs
        //String url = "jdbc:mysql://10.1.72.154:3306/liujf_db";
        String host = Common.getConfig("db_host");
        String port = Common.getConfig("db_port");
        String dbase = Common.getConfig("db_database");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbase + "?characterEncoding=utf-8";

        // MySQL配置时的用户名
        String user = Common.getConfig("db_user");

        // MySQL配置时的密码
        String password = Common.getConfig("db_pass");

        try {
            // 加载驱动程序
            Class.forName(driver);
            // 连续数据库
            conn = DriverManager.getConnection(url, user, password);

            if (conn.isClosed()) {
                Log.error("failed connecting to the Database!");
            }
        } catch (ClassNotFoundException e) {
            Log.error("Sorry,can`t find the Driver!");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.error("Sorry,mysql error!");
        }

        return conn;
    }

    public  PageEntity findPage(String url) {
        String url_hash = Common.md5(url.getBytes());
        PageEntity pe = null;

        String sql = "select * from page_data where url_hash='" + url_hash + "'";
        try {
            ResultSet rs = _query(sql);
            if (rs.next()) {

                pe = new PageEntity();
                pe.url = rs.getString("url");
                pe.create_time = rs.getString("create_time");
                pe.last_fetch_time = rs.getString("last_fetch_time");
                pe.last_modified_time = rs.getString("last_modified_time");
                pe.url_hash = rs.getString("url_hash");
                pe.content = Common.unzip(rs.getBlob("content").getBinaryStream());
                pe.headers = new String(Common.unzip(rs.getBlob("headers").getBinaryStream()));
                //更新访问时间
                update_field_time(pe.url_hash, "last_visit_time");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pe;
    }

    public static ResultSet _query(String sql) {
        Connection conn = dbmysql.conn();
        ResultSet rs = null;
        try {
            Statement statement = conn.createStatement();
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public boolean updatePageData(String url_hash, byte[] data) {
        boolean ret = false;
        if(url_hash == null || "".equals(url_hash) || data == null)
        {
            Log.error("Empty url_hash or data before update page data.");
            return false;
        }
        Connection conn = dbmysql.conn();
        try {
            //判断data返回数据，如果是非200，则不保存
            int code = HttpUtils.checkHttpCode(data);
            Log.info("updatePageData http code : " + code);
            String sql = "update page_data set `last_fetch_time`='" + Common.time() + "' ";
            //判断是否存在lastmodified时间
            String last_modified = HttpUtils.getHeaderItem(new String(data), "Last-Modified", null);
            if (last_modified != null && !last_modified.equals("")) {
                sql += " ,`last_modified_time`='" + last_modified + "'";
            }

            if (code != 200) {
                sql += " where url_hash=?";
            } else {
                sql += " ,`last_update_time`=?,`content`=? where url_hash=?";
            }
            Log.debug(sql);
            PreparedStatement statement = conn.prepareStatement(sql);
            if (code == 200) {
                statement.setString(1, Common.time());
                statement.setBlob(2, new ByteArrayInputStream(Common.zip(data)));
                statement.setString(3, url_hash);
            } else {
                statement.setString(1, url_hash);
            }

            ret = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean update_field_time(String url_hash, String field) {
        return update_field_time(url_hash, field, Common.time());
    }

    public static boolean update_field_time(String url_hash, String field, String value) {
        if(url_hash == null || "".equals(url_hash) || field == null)
        {
            Log.error("Empty url_hash or field found before update field time.");
            return false;
        }
        boolean ret = false;
        Connection conn = dbmysql.conn();
        try {
            String sql = "update page_data set `" + field + "`=? where url_hash=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, value);
            statement.setString(2, url_hash);
            ret = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean add(String url, byte[] req, byte[] data) {
        if(url == null || "".equals(url) || req == null || data == null)
        {
            Log.error("Data empty before insert to mysql.");
            return false;
        }
        
        //解析返回状态码
        int code = HttpUtils.checkHttpCode(data);
        if(code != 200)
        {
            Log.error("Response code invalid, " + code);
            return false;
        }
        
        Connection conn = dbmysql.conn();
        boolean ret = false;

        String url_hash = Common.md5(url.getBytes());
        String create_time = Common.time();
        //解析输出头
        String last_modified_time  = HttpUtils.getHeaderItem(new String(data), "Last-Modified", "");
        
        ByteArrayInputStream bais = new ByteArrayInputStream(Common.zip(data));
        ByteArrayInputStream baisheaders = new ByteArrayInputStream(Common.zip(req));

        try {
            // 要执行的SQL语句
            String sql = "insert into page_data(`url`,`url_hash`,`content`,`create_time`,`last_modified_time`,`headers`,`last_update_time`) values(?,?,?,?,?,?,?);";
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, url);
            statement.setString(2, url_hash);
            statement.setBlob(3, bais);
            statement.setString(4, create_time);
            statement.setString(5, last_modified_time);
            statement.setBlob(6, baisheaders);
            statement.setString(7, create_time);
            //statement.setString(8, new String(data, "utf-8"));

            // 结果集
            ret = statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;

    }

}
