package com.devicemanagement.obd.impl;

import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.data.Config;
import com.devicemanagement.obd.face.OBDEventIble;

import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2017/12/25 0025.
 *
 */
public class OBDFileEventImpl implements OBDEventIble {

    @Override
    public void dispose(final byte[] bytes){
        if(bytes[3] == '0'){   //失败
            MyLogger.info("failed.");
            Config.fileResult = 0;
        }else if(bytes[3] == '1'){  //成功
            MyLogger.info("succeed.");
            Config.fileResult = 1;
        }else{
            MyLogger.info("break.");
            Config.fileResult = 2;
        }

    }

}
