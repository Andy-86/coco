package com.devicemanagement.data;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Administrator on 2017/7/15 0015.
 *
 */
public class AppData {

    private static long imei = 0;
    public static void setImei(long i){imei = i;}
    public static long getImei(){return imei;}

    /**
     * 服务器IP,PORT
     */
    //private static String IP = "59.41.210.162";       //120.55.161.203
    private static String IP = "120.55.161.203";
//    private static String IP = "192.168.107.110";
    private static int PORT = 9101;
    public static void setIP(String ip){IP = ip;}
    public static String getIP(){return IP;}
    public static void setPORT(int port){PORT = port;}
    public static int getPORT(){return PORT;}

    /**
     * 在线升级服务器IP,PORT，USERNAME,PASSWORD"
     * sftp://mowushi@192.168.104.239/home/mowushi/dingdong
     */
    private static String upgrade_ip = "59.41.210.162";    //59.41.210.162   192.168.104.239
    private static int upgrade_port = 22;
    private static int udp_port = 9090;
    private static String username = "mowushi";
    private static String password = "F01Imdroid29832611MoWuShi";

    public static void setUpgrade_ip(String ip){upgrade_ip = ip;}
    public static String getUpgrade_ip(){return upgrade_ip;}
    public static void setUpgrade_port(int port){upgrade_port = port;}
    public static int getUpgrade_port(){return upgrade_port;}
    public static void setUdp_port(int port){udp_port = port;}
    public static int getUdp_port(){return udp_port;}
    public static void setUsername(String user){username = user;}
    public static String getUsername(){return username;}
    public static void setPassword(String pwd){password = pwd;}
    public static String getPassword(){return password;}

    /**
     * 在线升级服务器xml,apk文件储存路径"/home/mowushi/dingdong", "update.xml"
     * 看门狗apk文件存储路径"/home/mowushi/Guarddog"
     */
    private static String upgrade_directory = "/home/mowushi/jrhb/Device";
    private static String upgrade_xml = "update.xml";
    private static String Log_directory = "/home/mowushi/jrhb/devicelog";
    private static String errorLog_directory = "/home/mowushi/jrhb/deviceErrorLog";


    public static void setUpgrade_directory(String directory){upgrade_directory = directory;}
    public static String getUpgrade_directory(){return upgrade_directory;}

    public static void setUpgrade_xml(String xml){upgrade_xml = xml;}
    public static String getUpgrade_xml(){return upgrade_xml;}

    public static void setLog_directory(String directory){Log_directory = directory;}
    public static String getLog_directory(){return Log_directory;}

    public static void setErrorLog_directory(String directory){errorLog_directory = directory;}
    public static String getErrorLog_directory(){return errorLog_directory;}


    /**下发指令上传文件的所在目录*/
    private static String  uploadLogDirectory = "/home/mowushi/jrhb/logmanager";
    public static void setUploadLogDirectory(String directory){uploadLogDirectory = directory;}
    public static String getUploadLogDirectory(){return uploadLogDirectory;}

    /**
     * 记录发送ClientHello包的次数
     */
    private static int sendHelloTimes=0;
    public static void setSendHelloTimes(int times){sendHelloTimes = times;}
    public static int getSendHelloTimes(){return sendHelloTimes;}
    public static void addSendHelloTimes(){sendHelloTimes++;}

    /** DeviceHello 包的iccid 和 vincode rom版本号，客户软件版本号，gps定位方式，在不在白名单*/
    private static byte[] iccid = {1,2,2,4,5,6,3,3,5,2};
    private static short scmVersionCode = 1;
    public static void setIccid(byte[] id){iccid = id;}
    public static byte[] getIccid(){return iccid;}
    public static short getScmVersionCode() {
        return scmVersionCode;
    }

    public static void setScmVersionCode(short scmVersionCode) {
        AppData.scmVersionCode = scmVersionCode;
    }


    public static String romVersion = "";
    public static void setRomVersion(String version){
        romVersion = version;
    }
    public static String getRomVersion(){return romVersion;}


    /**
     * 记录是否接受到ServerCommand命令
     */
    private volatile static boolean isReceiveCommand =false;
    private static int ServerCommand = 0;
    public static void setIsReceiveCommand(boolean command){isReceiveCommand = command;}
    public static boolean getIsReceiveCommand(){return isReceiveCommand;}
    public static void setServerCommand(int command){ServerCommand = command;}
    public static int getServerCommand(){return ServerCommand;}


    /** 收到的消息id,命令id,消息流水号 收到指令的时间戳*/
    private static boolean receiveSerCommand;
    private static short message_id;
    private static byte command_id;
    private static short message_seq;
    private static long message_time;

    public static boolean isReceiveSerCommand(){return receiveSerCommand;}
    public static void setReceiveSerCommand(boolean receiveSerCommand){
        AppData.receiveSerCommand = receiveSerCommand;
    }
    public static void setMessage_id(short mes_id){message_id = mes_id;}
    public static short getMessage_id(){return message_id;}

    public static void setCommand_id(byte cmd){command_id = cmd;}
    public static byte getCommand_id(){return command_id;}

    public static void setMessage_seq(short mes_seq){message_seq = mes_seq;}
    public static short getMessage_seq(){return message_seq;}

    public static void setMessage_time(long time){message_time = time;}
    public static long getMessage_time(){return message_time;}

    /**  线程同步 （用于查询车辆状态时 -> 电量 和 车辆状态 ） 阻塞队列 */
    private static BlockingQueue<String> carQueue = new ArrayBlockingQueue<>(1);
    public static BlockingQueue<String> getCarQueue(){return carQueue;}

    /** 阻塞队列 标识是否需要线程同步 */
    private volatile static boolean needSynchronized = false;
    public static void setNeedSynchronized(boolean need){needSynchronized = need;}
    public static boolean isNeedSynchronized(){return needSynchronized;}

    /**  当前的OBD指令 */
    public static final String NOCOMMAND = "000";
    private volatile static String CURRENT_OBD_COMMAND = NOCOMMAND;
    public static void setCurrentObdCommand(String command){CURRENT_OBD_COMMAND = command;}
    public static String getCurrentObdCommand(){return CURRENT_OBD_COMMAND;}


    /** 推送apk所在服务器的目录 */
    private static String pushApkDirectory = "/home/mowushi/jrhb/pushApk";
    public static String getPushApkDirectory(){return pushApkDirectory;}
    public static void setPushApkDirectory(String directory){pushApkDirectory = directory;}


    /** bin 文件所在目录 */
    private static final String binDir = "/home/mowushi/jrhb/obdBin";
    public static String getBinDir() {
        return binDir;
    }


    /**Content Provider */
    //读取contentprovider 数据
    private static ContentResolver resolver;
    public static void setResolver( ContentResolver res){resolver = res;}
    public static ContentResolver getResolver(){
        return resolver;
    }

    private static Uri uri = Uri.parse("content://com.ddshareddemo.providers.myprovider/OBDData");
    public static Uri getUri(){return uri;}

    public static final boolean isJRHB = true;

    //设备可用时间
    private static short DeviceAvaTime = 0;
    public static short getDeviceAvaTime() {
        return DeviceAvaTime;
    }

    public static void setDeviceAvaTime(short deviceAvaTime) {
        DeviceAvaTime = deviceAvaTime;
    }


    //设置can信息头
    private static short can_message_id;
    private static short can_message_seq;

    public static short getCan_message_id() {
        return can_message_id;
    }

    public static void setCan_message_id(short can_message_id) {
        AppData.can_message_id = can_message_id;
    }

    public static short getCan_message_seq() {
        return can_message_seq;
    }

    public static void setCan_message_seq(short can_message_seq) {
        AppData.can_message_seq = can_message_seq;
    }
}
