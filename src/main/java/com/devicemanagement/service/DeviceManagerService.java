package com.devicemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;


import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.data.MLocation;
import com.devicemanagement.database.SPTool;
import com.devicemanagement.devicePacket.clientPacket.DeviceHello;
import com.devicemanagement.devicePacket.main.SendData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.phoneState.MobileInfo;
import com.devicemanagement.receiver.ReceiveUdpDataBroadcast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jim斌 on 2017/8/18.
 *
 */


public class DeviceManagerService extends Service {

    private static Timer timer = null;

    private Thread receiveThread = null;

    private ReceiveUdpDataBroadcast broadcast;

    private static final long INTEVAL_TIME = 5 * 1000;//  心跳包时间间隔

    private SPTool spTool;

    //液体温度，抽水电机工作时刻对应的seq
    private int changeSeq;
    private boolean compute = true;
    private int count = 0;
    private int oilcount = 0;

    //排水电机相关变量
    private int pumper1count = -1;
    private int pumper2count = -1;
    private int[] mInteval; //每次排水电机工作时间
    private int[] seq;  //每次排水电机工作开始时间
    private int[] stopSeq; //每次排水电机停止工作时间
    private int mTimes; //排水电机一天工作次数
    private int mIndex = 0; // 排水电机工作在哪个时间阶段标志。
    private int maxInteval = 0; //最大的时间间隔

    private int heatCount = 0;
    @Override
    public void onCreate(){
        super.onCreate();
        MyLogger.info("DeviceManagerService onCreate()回调");
        spTool = SPTool.getInstance();
        register();
        connectToServer();
        startTimer();
        receivePacket();
        startStatisticTraffics();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLogger.info("onStartCommand()回调");
//        return START_NOT_STICKY;
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }


    /**
     * 启动UDP，发送ClientHello包，建立虚连接，暂时使用明文进行测试
     * 当isConnected为false时，一直启动线程，向服务器发送ClientHello包
     */
    private void connectToServer(){
        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                MyLogger.info("Hello isConnected:"+ SendData.getIsConnected() + ", " + AppData.getIP());
                while(!SendData.getIsConnected()) {
                    try {
                        SendData.sendDeviceHello(DeviceHello.MACHINE_TYPE,DeviceHello.CUSTOMER,DeviceHello.SIM_OPERATOR
                                , AppData.getIccid(), AppData.getScmVersionCode());
                        AppData.addSendHelloTimes();
                        if( AppData.getSendHelloTimes() < 12) {
                            Thread.sleep(5 * AppData.getSendHelloTimes() * 1000);
                        }else{
                            Thread.sleep(60 * 1000);
                        }
                    } catch (Exception exce) {
                        exce.printStackTrace();
                        MyLogger.error(exce.toString());
                    }
                }
            }
        });
    }


    private static short seconds_1 = 0;

    private static short seconds_2 = 0;

    /**
     * 当isConnected=true，及虚连接建立时
     * 发送心跳包，每隔27秒发送一次
     */
    private void startTimer(){
        if(timer == null)
            timer = new Timer();
        long time = 10 * 1000;
        timer.schedule(new TimerTask() {
            public void run() {
                    try {
                        if(SendData.getIsConnected()) {
//
                            SendData.sendHeartbeatPacket();
                            MyLogger.info( MLocation.getLat()+","+MLocation.getLon());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyLogger.error(e.toString());
                    }

            }
        }, time, INTEVAL_TIME);

    }




    //停止定时器
    private void stopTimer(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    private boolean isReceiveData = true;
    private void receivePacket(){
        receiveThread = new Thread(new Runnable() {
            public void run() {
                MyLogger.info("receiveData isConnected: ");
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    MyLogger.error(e.toString());
                }
                while (isReceiveData) {
                    SendData.receiveData();
                }
            }
        });
        receiveThread.start();
    }

    //注册广播
    private void register(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(SendData.ACTION_UDP_COMMAND);
        broadcast = new ReceiveUdpDataBroadcast();
        registerReceiver(broadcast,filter);
    }

    private void startStatisticTraffics(){
        ThreadPools.getInstance().getScheduledThreadPool().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    float traffic = MobileInfo.getDataBytesSinceDeviceBoot() / 1024.0f;
                    float value = traffic - SPTool.getInstance().getOldTrafficBytes();
                    SPTool.getInstance().setOldTrafficBytes(traffic);
                    SPTool.getInstance().addCurrentTrafficBytes(value);
                    SPTool.getInstance().save(SPTool.TRAFFIC_STATS_BYTES, SPTool.getInstance().getCurrentTrafficBytes());
                }catch (Exception e){
                    MyLogger.error(e.toString());
                }
            }
        },10 * 1000,30 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            stopTimer();
            unregisterReceiver(broadcast);
            SendData.stopUdp();
            if (receiveThread != null) {
                isReceiveData = false;
                receiveThread.interrupt();
                receiveThread = null;
            }
        }catch (Exception e){
            MyLogger.error(e.toString());
        }
        MyLogger.info("DeviceManagerService onDestroy()回调");
    }
}
