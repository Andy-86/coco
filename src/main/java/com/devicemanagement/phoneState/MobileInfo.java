package com.devicemanagement.phoneState;

import android.net.TrafficStats;

/**
 * Created by Administrator on 2017/11/10 0010.
 * 获取手机相关信息(流量统计、)
 */
public class MobileInfo {

    /**
     * @return
     * return number of bytes received across mobile networks since device boot
     * return -1 on devices where statistics aren't available.
     */
    public static long getDataBytesSinceDeviceBoot(){
        return TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
    }
}
