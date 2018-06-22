package com.devicemanagement.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.devicemanagement.util.DataUtil;
import com.devicemanagement.devicePacket.dispose.UdpEventDispose;
import com.devicemanagement.devicePacket.main.SendData;

import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2017/8/3 0003.
 *
 */
//接收udp数据广播
public class ReceiveUdpDataBroadcast extends BroadcastReceiver {

    private Logger logger = Logger.getLogger("ReceiveUdpDataBroadcast");
    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        if(action.equals(SendData.ACTION_UDP_COMMAND)){
            final byte[] bytes = intent.getByteArrayExtra(SendData.EXTRA_UDP_COMMAND);
            try {
                logger.info(DataUtil.getHexString(bytes));
                UdpEventDispose.dispose(bytes);
            }catch (Exception e){
                e.printStackTrace();
                logger.error(e);
            }
        }
    }
}
