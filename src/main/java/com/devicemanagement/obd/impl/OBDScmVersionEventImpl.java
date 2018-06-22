package com.devicemanagement.obd.impl;

import android.widget.Toast;

import com.devicemanagement.MyApplication;
import com.devicemanagement.data.AppData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.face.OBDEventIble;


/**
 * Created by Administrator on 2018-03-14.
 *
 */
public class OBDScmVersionEventImpl implements OBDEventIble {
   @Override
    public void dispose(final byte[] bytes){
       MyLogger.info("rom version: " +bytes[4]);
       AppData.setScmVersionCode(bytes[4]);
       Toast.makeText(MyApplication.getContext(),"version: "+bytes[4],Toast.LENGTH_SHORT).show();
   }
}
