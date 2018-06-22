package com.devicemanagement.data;

/**
 * Created by Administrator on 2017/8/26 0026.
 *
 */
public class Seq {

    /**设备管理包 消息流水号*/
    private static byte warnSeq = 1;
    public static byte getWarnSeq() {
        return warnSeq;
    }

    public static void setWarnSeq(byte warnSeq) {
        Seq.warnSeq = warnSeq;
    }

    public static void addWarnSeq() {
        Seq.warnSeq++;
    }

    /** 心跳包消息流水号 */
    private static short deviceSeq = 0;
    public static void addDeviceSeq(){deviceSeq++;}
    public static short getDeviceSeq(){return deviceSeq;}
    public static void setDeviceSeq(short seq){deviceSeq = seq;}
}
