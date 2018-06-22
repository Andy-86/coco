package com.devicemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.main.SendObdData;


/**
 * Created by Administrator on 2017/8/13 0013.
 * OBD数据接收，发送线程在Service里创建，使线程通过服务可控
 */
public class ObdService extends Service {

    /**SendObdData类实例*/
    private SendObdData sendObdData;

    /** 接收obd返回数据的广播 */
//    private OBDReceiver obdReceiver = null;
//
//    private OperationReceiver operationReceiver = null;

    private int SOCtimes = 0;

    @Override
    public void onCreate(){
        super.onCreate();
        MyLogger.info("onCreate()回调");
//        register();
        startObd();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLogger.info("onStartCommand()回调");
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }



    private void startObd(){
        sendObdData = SendObdData.getInstance();
        sendObdData.StartObd();
    }

    /**
     * 注册obd数据接收广播
     */
//    private void register(){
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(SendObdData.ACTION_OBD_BYTE_RECEIVE);
//        obdReceiver = new OBDReceiver();
//        registerReceiver(obdReceiver,filter);
//
//        IntentFilter operationFilter = new IntentFilter();
//        operationFilter.addAction(OperationReceiver.OPERATION_ACTION);
//        operationReceiver = new OperationReceiver();
//        registerReceiver(operationReceiver, operationFilter);
//    }


    @Override
    public void onDestroy(){
        super.onDestroy();
//        unregisterReceiver(obdReceiver);
//        unregisterReceiver(operationReceiver);
        SendObdData.getInstance().stopOBD();
    }

}
