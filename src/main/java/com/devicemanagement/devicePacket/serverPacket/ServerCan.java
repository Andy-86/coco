package com.devicemanagement.devicePacket.serverPacket;

import com.devicemanagement.devicePacket.Packet;
import com.devicemanagement.devicePacket.ServerPacket;
import com.devicemanagement.util.AnalysisUtil;

/**
 * Created by mac on 2018/5/21.
 */

public class ServerCan extends ServerPacket {
    public static final int  SIZE_CAN_BYTE=16;
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

    /**
     *
     * @param led0 每1bit表示一个LED的状态，1：on, 0:off
     * @param led1
     * @param led2
     * @param led3
     * @param gauge0 (0-255)
     * @param gauge1
     * @param gauge2
     * @param gauge3
     */
    int cid;
    byte led0;
    byte led1;
    byte led2;
    byte led3;
    byte Gauge0;
    byte Gauge1;
    byte Gauge2;
    byte Gauge3;

    public static ServerCan fromBytes(byte[] data) {
        try {
            ServerCan packet = new ServerCan(data);
            if (!packet.validate()) {
                return null;
            }
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ServerCan(byte[] data) throws Exception {
        super (Packet.TYPE_SERVER_CAN,data);
    }

    @Override
    protected void loadData() throws Exception {
        cid= AnalysisUtil.byteToInt(getDataOfRange(OFFSET_ID,4));
        led0=getDataAt(OFFSET_LED0);
        led1=getDataAt(OFFSET_LED1);
        led2=getDataAt(OFFSET_LED2);
        led3=getDataAt(OFFSET_LED3);

        Gauge0=getDataAt(OFFSET_GAUGE0);
        Gauge1=getDataAt(OFFSET_GAUGE1);
        Gauge2=getDataAt(OFFSET_GAUGE2);
        Gauge3=getDataAt(OFFSET_GAUGE3);
    }
}
