package com.devicemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.util.DataUtil;
import com.devicemanagement.util.SftpClient;
import com.devicemanagement.devicePacket.main.SendData;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.util.Vector;

/**
 * Created by Administrator on 2017/9/15 0015.
 * 到指定服务器地址下载apk，并且安装，默认地址是"/home/mowushi/pushApk"
 */
public class PushApkService extends Service {


    private Handler handler = new Handler() {
        public void handleMessage(Message mes) {
            if (mes.what == 0) {
                SftpClient.getInStance().stopSftpConnect();
                PushApkService.this.stopSelf();
            } else if (mes.what == 1) {
                if(apkName.endsWith("apk")) {
                    InstallApk(localApkFilePath);
                }
                SftpClient.getInStance().stopSftpConnect();
                PushApkService.this.stopSelf();
            }
        }
    };

    private String localApkFilePath;
    private static Vector<ChannelSftp.LsEntry> fileVector = new Vector<>();
    private static String apkName = "";

    /**下载apk过程是否成功变量*/
    private boolean isConnected;
    private boolean isLoaded;
    private boolean isMakeDir;

    private byte len = 0x01;
    private byte[] message;

    @Override
    public void onCreate() {
        super.onCreate();
        MyLogger.info("onCreate()回调");
        localApkFilePath = Environment.getExternalStorageDirectory().getPath() + "/pushApk";
        File path = new File(localApkFilePath);
        if (!path.exists())
            isMakeDir = path.mkdirs();
        else isMakeDir = true;
        if(isMakeDir) {
            downLoadApkThread();
        }else{
            MyLogger.error("no such directory: "+localApkFilePath);
        }
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
        //SftpClient.getInStance().stopSftpConnect();
        MyLogger.info("onDestroy()回调");

    }

    /**  下载推送apk线程 */
    private void downLoadApkThread(){
        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                isConnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(),
                        AppData.getUsername(), AppData.getPassword());
                if(isConnected){
                    getFilename();
                    MyLogger.info("开始下载文件： "+apkName);
                    String filepath = localApkFilePath + "/" + apkName;
                    if(!apkName.equals("")) {
                        isLoaded = SftpClient.getInStance().download(AppData.getPushApkDirectory(),
                                apkName, filepath, SftpClient.getChannelSftp());
                    }else{
                        isLoaded = false;
                    }
                    if(isLoaded){
                        message = DataUtil.hexStringToBytes("31");
                        SendData.sendResponse(AppData.getMessage_seq(),AppData.getMessage_id(),AppData.getCommand_id()
                        ,AppData.getMessage_time(),len,message);
                        handler.sendEmptyMessage(1);
                    }else{
                        message = DataUtil.hexStringToBytes("30");
                        SendData.sendResponse(AppData.getMessage_seq(),AppData.getMessage_id(),AppData.getCommand_id(),
                                AppData.getMessage_time(),len,message);
                        handler.sendEmptyMessage(0);
                    }
                }else{
                    message = DataUtil.hexStringToBytes("30");
                    SendData.sendResponse(AppData.getMessage_seq(),AppData.getMessage_id(),AppData.getCommand_id()
                            ,AppData.getMessage_time(),len,message);
                    handler.sendEmptyMessage(0);
                }
            }
        });
    }

    /**
     * 弹出安装对话框，用户自定义安装apk
     */
    private void InstallApk(String path) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        String filepath = path + "/" + apkName;
        //一定要转化为文件类型才能startActivity成功
        File apkFile = new File(filepath);

        if (apkFile.length() == 0) {  //apk文件大小为0
            MyLogger.info("The length of new apk that downloaded from imdroid server is zero ");
        } else {
            MyLogger.info("The length of new apk that downloaded from imdroid server is:" + apkFile.length());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            startActivity(i);
        }
    }

    /**
     * 获得要下载的文件名
     */
    private void getFilename() {
        try {
            fileVector = SftpClient.getInStance().listFiles(AppData.getPushApkDirectory(), SftpClient.getChannelSftp());
            for (int i = 0; i < fileVector.size(); i++) {
                if(fileVector.get(i).getFilename().contains("apk") ||
                        fileVector.get(i).getFilename().contains("json")) {
                    apkName = fileVector.get(i).getFilename();
                    break;
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
            MyLogger.error(e.toString());
        }
    }
}
