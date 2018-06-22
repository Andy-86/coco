package com.devicemanagement.devicePacket.clientPacket;

import com.devicemanagement.devicePacket.ClientPacket;
import com.devicemanagement.util.DataUtil;

/**
 * Created by mac on 2018/5/21.
 */

public class ClientCan extends ClientPacket{

    public static final short  SIZE_CAN_BYTE=13;
    public static final byte LEN=0x08;
    public static final int SIZE_LED_BYTE=1;
    public static final int SIZE_GAUGE=1;

    public static final int OFFSET_ID=0;
    public static final int OFFSET_LEN=4;
    public static final int OFFSET_LED0=5;
    public static final int OFFSET_LED1=6;
    public static final int OFFSET_LED2=7;
    public static final int OFFSET_LED3=8;
    public static final int OFFSET_GAUGE0=9;
    public static final int OFFSET_GAUGE1=10;
    public static final int OFFSET_GAUGE2=11;
    public static final int OFFSET_GAUGE3=12;
    int cid;
    byte led0;
    byte led1;
    byte led2;
    byte led3;
    byte Gauge0;
    byte Gauge1;
    byte Gauge2;
    byte Gauge3;

    /**
     *
     * @param version
     * @param id
     * @param attribute 消息体的长度
     * @param imei
     * @param seq
     * @param reserved
     * @param led0 每1bit表示一个LED的状态，1：on, 0:off
     * @param led1
     * @param led2
     * @param led3
     * @param gauge0 (0-255)
     * @param gauge1
     * @param gauge2
     * @param gauge3
     * @throws Exception
     */
        public ClientCan(byte version, short id, short attribute, long imei, short seq, byte reserved,int cid, byte led0, byte led1, byte led2,
                         byte led3, byte gauge0, byte gauge1, byte gauge2, byte gauge3) throws Exception {
        super(version, id, attribute, imei, seq, reserved);
        this.cid=cid;
        this.led0 = led0;
        this.led1 = led1;
        this.led2 = led2;
        this.led3 = led3;
        Gauge0 = gauge0;
        Gauge1 = gauge1;
        Gauge2 = gauge2;
        Gauge3 = gauge3;
        callAfterSetMemberDone();
    }

    @Override
    protected void dumpData() throws Exception {
        setDataOfRange(OFFSET_ID, DataUtil.intToByte(cid));
        setDataAt(OFFSET_LEN,LEN);
        setDataAt(OFFSET_LED0,led0);
        setDataAt(OFFSET_LED1,led1);
        setDataAt(OFFSET_LED2,led2);
        setDataAt(OFFSET_LED3,led3);
        setDataAt(OFFSET_GAUGE0,Gauge0);
        setDataAt(OFFSET_GAUGE1,Gauge1);
        setDataAt(OFFSET_GAUGE2,Gauge2);
        setDataAt(OFFSET_GAUGE3,Gauge3);
    }
}
