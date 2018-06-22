package com.devicemanagement.util;


import com.devicemanagement.data.AppData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.main.SendObdData;

import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/27 0027.
 * @author lg
 * the public method associated with obd operation.
 */
public class OBDMethod {

    public static final long TEN_SECONDS_OF_TIME_OUT = 5 * 1000;


//    public synchronized static void makeSynchronizedByBlockingQueue(String command){
//
//        AppData.setNeedSynchronized(true);
//        AppData.setCurrentObdCommand(command);
//        SendObdData.getInstance().SetSendCommand(command);
//        SendObdData.getInstance().isSendCommand();
//        try{
////            gLogger.info("starting to wait for " + command + " completed. ");
//            AppData.getCarQueue().poll(TEN_SECONDS_OF_TIME_OUT,TimeUnit.MILLISECONDS);
////            gLogger.info("command is completed. ");
//        }catch (Exception e){
//            MyLogger.error(e.toString());
//        }
//        AppData.setNeedSynchronized(false);
//        AppData.setCurrentObdCommand(AppData.NOCOMMAND);
//    }

    /**
     * Sending OBD_COMMAND to complish synchronization in different Thread.
     * The method identified as synchronized is to make sure only one Thread is allawed to
     * visit the method at one time.
     * @param bytes the obd command in directory of obd/config
     */
    public synchronized static void makeSynchronizedByBlockingQueue(byte[] bytes){

        AppData.setNeedSynchronized(true);
        byte[] cmd = new byte[2];
        System.arraycopy(bytes,0,cmd,0,2);
        String command = new String(cmd);
        MyLogger.info(command);
        AppData.setCurrentObdCommand(command);
        SendObdData.getInstance().sendCommand(bytes);
        try{
//            MyLogger.info("starting to wait for " + command + " completed. ");
            AppData.getCarQueue().poll(TEN_SECONDS_OF_TIME_OUT,TimeUnit.MILLISECONDS);
//            MyLogger.info("command is completed. ");
        }catch (Exception e){
            MyLogger.error(e.toString());
        }
        AppData.setNeedSynchronized(false);
        AppData.setCurrentObdCommand(AppData.NOCOMMAND);
    }


}
