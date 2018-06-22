package com.devicemanagement.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;


import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.log.ConfiguratorLog;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.data.Config;
import com.devicemanagement.obd.main.SendObdData;
import com.devicemanagement.util.DataUtil;
import com.devicemanagement.util.FileUtil;
import com.devicemanagement.util.OBDMethod;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2017/7/18 0018.
 * @author lg
 */
public class AppLogService extends Service {

    private final String OIL_DRAIN_ACTION = "oil_drain_action";
    private final String SWITCH_LOG_FILE_ACTION = "imdroid_switch_log_file_action";
    public static final String OBD_UPGRADE_ACTION = "OBD_UPGRADE_ACTION";
    public static final String IMDROID_GPS_OPEN_ACTION = "IMDROID_GPS_OPEN_ACTION";

    //private File[] uploadFiles = new File[2];
    private List<File> uploadFiles = new ArrayList<>();

    private String rootDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

    private FutureTask<Boolean> conn_future;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private LogTaskReceiver logTaskReceiver = null;

    @Override
    public void onCreate(){
        MyLogger.info("onCreate()回调");
        register();
        deploySwitchLogFileTask();
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
        MyLogger.info("onDestroy()回调");
        unregisterReceiver(logTaskReceiver);
    }

    /**
     * 接收日志更新广播
     */
    class LogTaskReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(SWITCH_LOG_FILE_ACTION.equals(action)){
                //若日志轮换，则重新配置日志文件，并检查是否有三天前的日志。
                ConfiguratorLog.configure(AppData.getImei());
                if(deleteOldFile(ConfiguratorLog.getLogDirectory())){
                    MyLogger.info("日志更新成功");
                }
                else MyLogger.info("日志更新失败");

                if(deleteOldVersionApk(rootDirectory)){
                    MyLogger.info("删除根目录下的apk成功");
                }else {
                    MyLogger.info("没有apk可删，删除根目录下最后一个或多个apk失败");
                }
                //重启设备。
                //更新日志后重启系统
                Calendar calendar = Calendar.getInstance();
                if(calendar.get(Calendar.HOUR_OF_DAY) == 0 || calendar.get(Calendar.HOUR_OF_DAY) == 23) {
                    try {
                        PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE); //重启到fastboot模式
                        pManager.reboot("");
                    } catch (Exception e) {
                        MyLogger.error(e.toString());
                    }
                }else{
                    MyLogger.info("device time change.");
                }

            }
            else if(OBD_UPGRADE_ACTION.equals(action)){
                MyLogger.info("start to upgrade obd.");
                ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        SendObdData.setObdUpgrade(true);
                        List<File> files = FileUtil.listFilesByDirAndSuffix(Environment.getExternalStorageDirectory()
                                + "/pushApk", ".bin");
                        FileUtil.sortFileByName(files);
                        if(files == null || files.isEmpty()) {
                            SendObdData.setObdUpgrade(false);
                            MyLogger.info("have no bin file.");
                            return;
                        }
                        byte[] mes = new byte[3];
                        System.arraycopy(Config.OBD_START_TRANS_FILE.getBytes(),0,mes,0,2);
                        mes[2] = (byte)files.size();
                        MyLogger.info(DataUtil.getHexString(mes));
                        OBDMethod.makeSynchronizedByBlockingQueue(mes);
                        if(Config.fileResult == 0){
                            MyLogger.info("start trans file failed.");
                            SendObdData.setObdUpgrade(false);
                            return;
                        }

                        boolean isbreak = false;
                        int i = 0;
                        while(!isbreak) {
                            Config.fileResult = -1;
                            MyLogger.info(files.get(i).getName());
                            byte[] binByte = FileUtil.bin2String(files.get(i));
                            if(binByte != null) {
                                SendObdData.getInstance().sendCommand(binByte);
                            }
                            while (Config.fileResult == -1) {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    MyLogger.error(e.toString());
                                }
                            }
                            if (Config.fileResult == 2) {
                                SendObdData.setObdUpgrade(false);
                                return;
                            } else if (Config.fileResult == 0) {
                                continue;
                            }else{  //正确
                                i++;
                                if(i >= files.size())
                                    isbreak = true;  //跳出
                            }
                        }

                        Config.fileResult = -1;
                        OBDMethod.makeSynchronizedByBlockingQueue(Config.OBD_STOP_TRANS_FILE.getBytes());
                        while(Config.fileResult == 0){
                            OBDMethod.makeSynchronizedByBlockingQueue(Config.OBD_STOP_TRANS_FILE.getBytes());
                        }
                        if(Config.fileResult == 2) {
                            MyLogger.info("stop trans file failed.");
                            SendObdData.setObdUpgrade(false);
                            return;
                        }
                        OBDMethod.makeSynchronizedByBlockingQueue(Config.OBD_START_UPGRADE.getBytes());
                        SendObdData.setObdUpgrade(false);
                        Config.fileResult = 0;
                        MyLogger.info("升级开始中.");
                    }
                });
            }
        }
    }

    /**
     * 注册广播
     */
    private void register(){
        IntentFilter logTaskFilter = new IntentFilter();
        logTaskFilter.addAction(SWITCH_LOG_FILE_ACTION);
        logTaskFilter.addAction(OBD_UPGRADE_ACTION);
        logTaskReceiver = new LogTaskReceiver();
        registerReceiver(logTaskReceiver,logTaskFilter);
    }

    public static final int REBOOT_HOUR_OF_DAY = 0;


    /**
     * 部署日志切换任务，每天凌晨切换日志文件
     */
    private void deploySwitchLogFileTask() {
        Intent intent = new Intent(SWITCH_LOG_FILE_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, REBOOT_HOUR_OF_DAY);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 部署任务
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }


    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    private boolean deleteOldFile(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //gLogger.info(files[0].getAbsolutePath());
        //遍历删除文件夹下的三天之前的文件
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() &&isOldFile(files[i],ConfiguratorLog.THREE_DAY_AGO)) {
                //删除子文件
                flag = files[i].delete();
                //flag = deleteFile(files[i].getName());
                //if (!flag) break;
            }
        }
        return flag;
    }

    /**
     * 判断file是否是三天前的文件
     * @param file : log file
     * @param time :
     * @return
     */
    private boolean isOldFile(File file,long time){
        String filename = file.getName();
        if(filename.length()<10) return false;
        try{
            String prefileName = filename.substring(0,10);
            SimpleDateFormat ft = ConfiguratorLog.getFt();
            Date old_date = ft.parse(prefileName);
            if(System.currentTimeMillis() - old_date.getTime() >= time)
                return true;
            else return false;
        }catch (Exception e){
            MyLogger.error(e.toString());
            return false;
        }
    }

    /**
     * 获取前一天的日志文件
     * @param filePath
     * @return
     */
    private boolean uploadOneDayAgoFile(String filePath){
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        File[] files = dirFile.listFiles();
        //遍历文件夹下前一天的文件
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && isOneDayAgoFile(files[i])){
                uploadFiles.add(files[i]);
            }
        }
        return true;
    }

    private boolean isOneDayAgoFile(File file){
        String filename = file.getName();
        if(filename.length()<10) return false;
        try{
            String prefileName = filename.substring(0,10);
            SimpleDateFormat ft = ConfiguratorLog.getFt();
            Date old_date = ft.parse(prefileName);
            if(System.currentTimeMillis() - old_date.getTime() >= ConfiguratorLog.ONE_DAY_AGO &&
                    System.currentTimeMillis() - old_date.getTime() < ConfiguratorLog.ONE_AND_A_HALF_DAYS)
                return true;
            else return false;
        }catch (Exception e){
            MyLogger.error(e.toString());
            return false;
        }
    }

    private boolean deleteOldVersionApk(String path){
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        boolean flag = false;
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        File[] files = dirFile.listFiles();
        for(int i = 0;i < files.length; i++){
            if(files[i].isFile() && files[i].getName().endsWith("apk")){
                flag = files[i].delete();
            }
        }
        return flag;
    }
}
