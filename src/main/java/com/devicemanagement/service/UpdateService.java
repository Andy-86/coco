package com.devicemanagement.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.widget.Toast;

import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.util.ParseXml;
import com.devicemanagement.util.SftpClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Created by Administrator on 2017/7/1 0001.
 * @author lg
 */

public class UpdateService extends Service {

    private static boolean isLoaded = false;
    private static String apkName = "";

    public static String getApkName() {
        return apkName;
    }

    private static String xmlName = "";
    private static Vector<ChannelSftp.LsEntry> fileVector = new Vector<>();

    private String folder = "";
    private boolean isMakedir = true;

    public UpdateService() {

    }

    private Handler mhandlerThread = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //是否更新
                if (ParseXml.isUpdate("/Device")) {
                    downApkThread();
                }
                //不用更新则干掉自己
                else {
                    //关闭连接
                    SftpClient.getInStance().stopSftpConnect();
                    UpdateService.this.stopSelf();
                }
            }
            if (msg.what == 2) {
                //无网络 则干掉自己
                UpdateService.this.stopSelf();
            }
            if (msg.what == 3) {
                Toast.makeText(getApplicationContext(), "下载完成，准备安装！", Toast.LENGTH_SHORT).show();
                MyLogger.info( "下载完成，准备安装！");
                //android 6.0 安装
                InstallApk();
            }
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        MyLogger.info("onCreate()回调");
        folder = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Device";
        File path = new File(folder);

        if (!path.exists())
            isMakedir = path.mkdirs();
        else isMakedir = true;
        //检查更新
        if(isMakedir) {
            initThread();
        }else{
            MyLogger.info("create file folder failed.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLogger.info("onStartCommand()回调");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化线程，从服务器下载xml文件，判断有无更新
     */
    private void initThread() {
        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(), AppData.getUsername(), AppData.getPassword());
                ChannelSftp sftp = SftpClient.getChannelSftp();
                MyLogger.info("路径" + folder);
                String filepath = folder + "/" + AppData.getUpgrade_xml();
                if (sftp != null && SftpClient.getInStance() != null) {
                    isLoaded = SftpClient.getInStance().download(AppData.getUpgrade_directory(), AppData.getUpgrade_xml(), filepath, sftp);
                    if (!isLoaded) {
                        //关闭sftp连接
                        SftpClient.getInStance().stopSftpConnect();
                        UpdateService.this.stopSelf();  //如果下载失败，停止服务
                    }
                    mhandlerThread.sendEmptyMessage(1);
                } else mhandlerThread.sendEmptyMessage(2);  //无网络
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SftpClient.getInStance().stopSftpConnect();
        MyLogger.info( "onDestroy()回调");
    }

    /**
     * 下载apk线程
     */
    private void downApkThread() {
        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                ChannelSftp sftp = SftpClient.getChannelSftp();
                if (sftp != null) {
                    MyLogger.info( "开始下载apk。。。。");
                    getFilename();   //获取apk文件名
                    String filepath = folder + "/" + apkName;
                    MyLogger.info( "apk name is: " + apkName);
                    if (!apkName.equals("")) {
                        isLoaded = SftpClient.getInStance().download(AppData.getUpgrade_directory(), apkName, filepath, sftp);
                        if (!isLoaded) UpdateService.this.stopSelf();  //如果下载失败，停止服务
                        MyLogger.info("下载apk成功。");

                        mhandlerThread.sendEmptyMessage(3);
                    } else {
                        MyLogger.info( "没有apk文件存在");
                    }
                    //关闭sftp连接
                    SftpClient.getInStance().stopSftpConnect();
                }
            }
        });
    }

    /**
     * 获得apk文件名
     */
    private void getFilename() {
        try {
            fileVector = SftpClient.getInStance().listFiles(AppData.getUpgrade_directory(), SftpClient.getChannelSftp());
            for (int i = 0; i < fileVector.size(); i++) {
                if (fileVector.get(i).getFilename().contains("apk")) {
                    apkName = fileVector.get(i).getFilename();
                    break;
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    /**
     * 静默安装apk，系统级别权限
     *
     * @return
     */
    private void InstallApkSilent() {
        String apkAbsolutePath = Environment.getExternalStorageDirectory().getPath() + "/" + apkName;
        String[] args = {"pm", "install", "-r", apkAbsolutePath};
        //String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            //errIs = process.getErrorStream();
//            while ((read = errIs.read()) != -1) {
//                baos.write(read);
//            }
//            baos.write('\n');
            inIs = process.getInputStream();
            byte[] bs = new byte[256];
            while ((read = inIs.read(bs)) != -1) {
                //baos.write(read);
                String state = new String(bs, 0, read);
                if (state.equals("Success\n")) {
                    MyLogger.info( "安装完成！");
                    //重启到fastboot模式
                    PowerManager pManager = (PowerManager) UpdateService.this.getSystemService(Context.POWER_SERVICE);
                    pManager.reboot("");
                }
            }
//            byte[] data = baos.toByteArray();
//            result = new String(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                UpdateService.this.stopSelf();
            }
            if (process != null) {
                process.destroy();
            }
        }
        //return result;
    }

    /**
     * 弹出安装对话框，用户自定义安装apk
     */
    private void InstallApk() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        //String filepath = Environment.getExternalStorageDirectory().getPath();
        String path = folder + "/" + apkName;
        //一定要转化为文件类型才能startActivity成功
        File apkFile = new File(path);

        if (apkFile.length() == 0) {  //apk文件大小为0
            MyLogger.info("The length of new apk that downloaded from imdroid server is zero ");
        } else {
            MyLogger.info("The length of new apk that downloaded from imdroid server is:" + apkFile.length());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            startActivity(i);
            //杀掉旧app的运行进程
            //android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}