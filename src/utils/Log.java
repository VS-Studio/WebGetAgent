package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Log {
    private static String logFile = "log/log.";
    private static int level = 1;//默认INFO模式
    private static FileWriter logFileWriter; 
    private static java.text.DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static boolean writeLogFile = false;
    
    public final static int DEBUG = 0;
    public final static int INFO = 1;
    public final static int WARN = 2;
    public final static int ERROR = 3;
    public final static String[] info = {"DEBUG", "INFO ", "WARN ", "ERROR"};
    
    static{
        Common.mkdir("log");
    }

    public static void level(int _level) {
        level = _level;
    }

    public static void debug(String str) {
        write(str, 0);
    }

    public static void info(String str) {
        write(str, 1);
    }

    public static void warn(String str) {
        write(str, 2);
    }

    public static void error(String str) {
        write(str, 3);
    }

    public static void write(String str, int _level) {
        if (level <= _level) {
            //格式化输出
            String time = format1.format(new Date());
            String inf = String.format("[%s][%s] %s\n", time, info[_level], str);

            if(writeLogFile)
            {
                writeFile(inf);
            }else{
                System.out.println(inf);
            }
        }
    }
    
    private static String getFileName()
    {
        //区分进程号
        return logFile + Common.time("yyyyMMdd", new Date()) + "." + Common.getCurrentThreadID();
    }
    
    private static void getWriter(String filename)
    {
        if(logFileWriter == null || (new File(filename).exists() == false))
        {
            try {
                if(logFileWriter != null) logFileWriter.close();
                
                logFileWriter = new FileWriter(filename);
            } catch (IOException ex) {
                System.out.println("Log.getWriter failed : " + ex.getMessage());
            }
        }
    }

    public static void writeFile(String str) {
        getWriter(getFileName());//每次写都检查一遍
        
        if(logFileWriter != null)
        {
            try {
                logFileWriter.write(str);
                logFileWriter.flush();
            } catch (IOException ex) {
                System.out.println("Log.writeFile failed : " + ex.getMessage());
            }
        }
        //System.out.println(""+ logFile + str);
    }
    


}
