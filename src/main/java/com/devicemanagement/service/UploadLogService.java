package com.devicemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.util.DataUtil;
import com.devicemanagement.util.SftpClient;
import com.devicemanagement.devicePacket.main.SendData;
import com.devicemanagement.log.ConfiguratorLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/9/15 0015.
 * 上传当天dingdognapp的日志文件到服务器目录（logmanager）
 */
public class UploadLogService extends Service {


    /**叮咚log日志目录*/
    private final String DINGDONGLOG = Environment.getExternalStorageDirectory().getPath()+"/dingdonglog";

    /**要上传的日志*/
    private List<File> uploadFiles = new ArrayList<>();

    /**响应包变量*/
    private byte len = (byte)0x01;
    private byte[] message;

//    /**上传日志个数*/
//    public static final String LOG = "LOG";
//    public static final int ONE = 0;
//    public static final int ALL = 1;

    @Override
    public void onCreate(){
        super.onCreate();
        uploadLog();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLogger.info("onStartCommand()回调");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SftpClient.getInStance().stopSftpConnect();
        MyLogger.info("onDestroy()回调");
    }

    private void uploadLog(){
        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if(findCurDayLog(DINGDONGLOG)){
                    boolean isconnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(), AppData.getUsername(), AppData.getPassword());
                    if (isconnected) {
                        for (File file : uploadFiles) {
                            SftpClient.getInStance().upload(AppData.getUploadLogDirectory(),
                                    file.getAbsolutePath(), SftpClient.getChannelSftp());
                        }
                        SftpClient.getInStance().stopSftpConnect();
                        MyLogger.info("upload dingdong log files succeed!");
                        //响应服务器
                        message = DataUtil.hexStringToBytes("31");
                        SendData.sendResponse(AppData.getMessage_seq(), AppData.getMessage_id(),
                                AppData.getCommand_id(), AppData.getMessage_time(),len, message);
                    } else {
                        MyLogger.error("cannot connected to server");
                        //响应服务器
                        message = DataUtil.hexStringToBytes("30");
                        SendData.sendResponse(AppData.getMessage_seq(), AppData.getMessage_id(),
                                AppData.getCommand_id(), AppData.getMessage_time(), len, message);
                    }
                }else{
                    //响应服务器
                    message = DataUtil.hexStringToBytes("30");
                    SendData.sendResponse(AppData.getMessage_seq(), AppData.getMessage_id(),
                            AppData.getCommand_id(), AppData.getMessage_time(),len, message);
                    MyLogger.info("no files to upload.");
                }
                uploadFiles.clear();
                UploadLogService.this.stopSelf();
            }
        });
    }

    /**
     * 取出filePath目录下的当天日志文件
     * @param   filePath 要查找文件的路径
     * @return  目录查找成功返回true，否则返回false
     */
    private boolean findCurDayLog(String filePath) {
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && isCurDayLog(files[i])) {
                uploadFiles.add(files[i]);
            }
        }
        if(uploadFiles.size()>0)
            return true;
        else return false;
    }

    private boolean isCurDayLog(File file){
        String filename = file.getName();
        if(filename.length()<10) return false;
        try{
            String prefileName = filename.substring(0,10);
            SimpleDateFormat ft = ConfiguratorLog.getFt();
            Date date = new Date();
            String nowString = ft.format(date);
            if(prefileName.equals(nowString))
                return true;
            else return false;
        }catch (Exception e){
            MyLogger.error(e.toString());
            return false;
        }
    }


}
