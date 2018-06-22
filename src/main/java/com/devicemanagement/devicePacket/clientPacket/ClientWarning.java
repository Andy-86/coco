package com.devicemanagement.devicePacket.clientPacket;


import com.devicemanagement.util.DataUtil;
import com.devicemanagement.devicePacket.ClientPacket;

/**
 * Created by Administrator on 2018-04-19.
 *
 */
public class ClientWarning extends ClientPacket {

    /**size*/
    protected static final int SIZE_WARNMES_BYTE = 1;
    protected static final int SIZE_TIME_BYTE = 8;
    protected static final int SIZE_WARNSEQ_BYTE = 1;
    protected static final int SIZE_LATITUDE_BYTE = 4;

    public static final int SIZE_CLIENT_WARNING_BYTE = SIZE_WARNMES_BYTE +
            SIZE_TIME_BYTE + SIZE_WARNSEQ_BYTE + SIZE_LATITUDE_BYTE * 2;

    /** offset*/
    protected static final int OFFSET_WARNMES = 0;
    protected static final int OFFSET_TIME = OFFSET_WARNMES +
            SIZE_WARNMES_BYTE;
    protected static final int OFFSET_WARNSEQ = OFFSET_TIME +
            SIZE_TIME_BYTE;
    protected static final int OFFSET_LATITUDE = OFFSET_WARNSEQ +
            SIZE_WARNSEQ_BYTE;
    protected static final int OFFSET_LONGITUDE = OFFSET_LATITUDE +
            SIZE_LATITUDE_BYTE;

    //预警信息
    public static final byte DEVICE_POW_OFF = 0X01;
    public static final byte BATTERY_VOLT_LOW = 0X02;

    /**预警信息*/
    private byte warnMes;
    /**时间戳 */
    private long time;

    /** 序号*/
    private byte warnSeq;

    //经纬度
    private float latitude;
    private float longitude;

    public ClientWarning(byte version,short id,short attribute,long imei,short seq,byte obl,
                         byte warnMes, long time, byte warnSeq, float latitude, float longitude) throws Exception {
        super(version,id,attribute,imei,seq,obl);
        this.warnMes = warnMes;
        this.time = time;
        this.warnSeq = warnSeq;
        this.latitude = latitude;
        this.longitude = longitude;
        callAfterSetMemberDone();
    }

    public void setWarnSeq(byte warnSeq) {
        this.warnSeq = warnSeq;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setWarnMes(byte warnMes) {
        this.warnMes = warnMes;
    }


    @Override
    protected void dumpData() throws Exception {
        setDataAt(OFFSET_WARNMES, this.warnMes);
        setDataOfRange(OFFSET_TIME, DataUtil.longToByte(this.time));
        setDataAt(OFFSET_WARNSEQ, this.warnSeq);
        setDataOfRange(OFFSET_LATITUDE, DataUtil.floatTobyte(this.latitude));
        setDataOfRange(OFFSET_LONGITUDE, DataUtil.floatTobyte(this.longitude));
    }
}
