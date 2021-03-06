package com.devicemanagement.obd.impl;




import com.devicemanagement.data.AppData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.data.Config;
import com.devicemanagement.obd.face.OBDEventIble;
import com.devicemanagement.util.OBDMethod;

import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/12/25 0025.
 *
 */
public class OBDStopTansFileEventImpl implements OBDEventIble {

    @Override
    public void dispose(final byte[] bytes){
        if(bytes[4] == 0x00){   //失败
            MyLogger.info("failed.");
            Config.fileResult = 0;
        }else if(bytes[4] == 0x01){  //成功
            MyLogger.info("succeed.");
            Config.fileResult = 1;
        }else{
            MyLogger.info("无意义");
            Config.fileResult = 2;
        }

        //阻塞同步
        if(AppData.isNeedSynchronized() && AppData.getCurrentObdCommand().equals(Config.OBD_STOP_TRANS_FILE)) {
            MyLogger.info("need Synchronzied when send OBD_STOP_TRANS_FILE command.");
            try {
                AppData.getCarQueue().offer(Config.OBD_STOP_TRANS_FILE, OBDMethod.TEN_SECONDS_OF_TIME_OUT,
                        TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                MyLogger.error("AppData.getCarQueue()" + e);
            }
        }
    }
}
