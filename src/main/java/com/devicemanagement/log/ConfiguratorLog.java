package com.devicemanagement.log;

import android.os.Environment;

import org.apache.log4j.Level;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by Administrator on 2017/7/14 0014.
 * Class of config error log.
 */
public class ConfiguratorLog {


    /**
     * 日志配置
     */
    public static void configure(long imei) {
        final LogConfigurator logConfigurator = new LogConfigurator();
        Date nowtime = new Date();
        try {
            // String needWriteMessage = myLogSdf.format(nowtime);
            //日志文件路径地址:SD卡下dingdonglog文件夹yyyy-MM-dd-IMEI.txt文件
            String name = ft.format(nowtime);
            StringBuilder fileName = new StringBuilder();
            fileName.append(Environment.getExternalStorageDirectory().getAbsolutePath());
            fileName.append("/ImdroidDeviceLog");
            fileName.append("/");
            fileName.append(name);
            fileName.append("-");
            fileName.append(String.valueOf(imei));
            fileName.append("-device");
            fileName.append(".txt");
            String fileAllname = fileName.toString();
            //设置文件名
            logConfigurator.setFileName(fileAllname);

            //设置root日志输出级别 默认为DEBUG
            logConfigurator.setRootLevel(Level.DEBUG);
            // 设置日志输出级别
            logConfigurator.setLevel("org.apache", Level.DEBUG);
            //设置 输出到日志文件的文字格式 默认 %d %-5p [%c{2}]-[%L] %m%n
            logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
            //设置输出到控制台的文字格式 默认%m%n
            logConfigurator.setLogCatPattern("%m%n");
            //设置总文件大小 (1M)
            logConfigurator.setMaxFileSize(1024 * 1024);
            //设置最大产生的文件个数
            logConfigurator.setMaxBackupSize(2);
            //设置所有消息是否被立刻输出 默认为true,false 不输出
            logConfigurator.setImmediateFlush(true);
            //是否本地控制台打印输出 默认为true ，false不输出
            logConfigurator.setUseLogCatAppender(true);
            //设置是否启用文件附加,默认为true。false为覆盖文件
            logConfigurator.setUseFileAppender(true);
            //设置是否重置配置文件，默认为true
            logConfigurator.setResetConfiguration(true);
            //是否显示内部初始化日志,默认为false
            logConfigurator.setInternalDebugging(false);

            logConfigurator.configure();
        }catch (Exception e){
            //logConfigurator.setResetConfiguration(true);
            android.util.Log.e("ConfiguratorLog","Log4j configure error");
        }
    }

    private static SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
    private static String logDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ImdroidDeviceLog";
    public static final int THREE_DAY_AGO = 3 * 24 * 60 * 60 * 1000;
    public static final int ONE_DAY_AGO = 24 * 60 * 60 * 1000;
    public static final int ONE_AND_A_HALF_DAYS = 24 * 60 * 60 * 1000 + (24 * 60 * 60 * 1000)/2;

    public static String getLogDirectory(){return logDirectory;}

    public static SimpleDateFormat getFt(){return ft;}

}
