package com.devicemanagement.devicePacket;

import com.devicemanagement.util.AnalysisUtil;
import com.devicemanagement.util.DataUtil;

import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/8/28 0028.
 * @author lg
 */
public abstract class Packet {

    private Logger logger = Logger.getLogger("Packet");
    //size
    protected static final int SIZE_FLAG_BYTE = 1;
    protected static final int SIZE_PROVERSION_BYTE = 1;
    protected static final int SIZE_ID_BYTE = 2;
    protected static final int SIZE_BODYATTRIBUTE_BYTE = 2;
    protected static final int SIZE_IMEI_BYTE = 8;
    protected static final int SIZE_SEQ_BYTE = 2;
    protected static final int SIZE_RESERVED_BYTE = 1;

    /** 消息头长度 1 + 2 + 2 + 8 + 2 + 1 = 16*/
    protected static final int SIZE_HEAD_PACKET_BYTE = SIZE_PROVERSION_BYTE + SIZE_ID_BYTE + SIZE_BODYATTRIBUTE_BYTE
            + SIZE_IMEI_BYTE+SIZE_SEQ_BYTE + SIZE_RESERVED_BYTE;

    //OFFSET
    //消息开始
    protected  static final int OFFSET_FLAG_START = 0;
    /** 协议版本号 */
    protected static final int OFFSET_PROVERSION = OFFSET_FLAG_START + SIZE_FLAG_BYTE;

    /** 消息id */
    protected static final int OFFSET_ID = OFFSET_PROVERSION + SIZE_PROVERSION_BYTE;

    /** 消息体属性 */
    protected static final int OFFSET_BODYATTRIBUTE = OFFSET_ID + SIZE_ID_BYTE;

    /** 终端设备号 */
    protected static final int OFFSET_IMEI = OFFSET_BODYATTRIBUTE + SIZE_BODYATTRIBUTE_BYTE;

    /** 流水号 */
    protected static final int OFFSET_SEQ = OFFSET_IMEI + SIZE_IMEI_BYTE;
    /** 保留 */
    protected static final int OFFSET_RESERVED = OFFSET_SEQ + SIZE_SEQ_BYTE;

    /** 消息体 */
    protected static final int OFFSET_DATA_BODY = OFFSET_RESERVED + SIZE_RESERVED_BYTE;

    //公用
    public static final byte FLAG = 0x7E;
    public static final int SIZE_FLAG_AND_CHECKCODE = 3;
    public static final byte VERSION = 4;
    public static final byte RESERVED = 0x3c;

    // 包类型
    public static final short TYPE_DEVICE_HELLO = 0x1001;
    public static final short TYPE_SERVER_HELLO = (short)0x8001;
    public static final short TYPE_CLIENT_HEARTBEAT = 0x1000;
    public static final short TYPE_SERVER_COMMAND = (short)0x9000;
    public static final short TYPE_CLIENT_RESPONSE = 0x0004;
    public static final short TYPE_CLIENT_WARNING = 0x0005;
    public static final short TYPE_CLIENT_CAN=0x1004;
    public static final short TYPE_SERVER_CAN=0x1005;

    public static final byte ENCRYPTION_PLAINTEXT = 0x00;
    public static final byte ENCRYPTION_RSA_PUBLIC = 0x01;
    public static final byte ENCRYPTION_AES_128 = 0x02;



    public Packet(byte version,short id,short attribute,long imei,short seq,byte reserved) throws Exception{
        this.proVersion = version;
        this.id = id;
        this.bodyAttribute = attribute;
        this.imei = imei;
        this.seq = seq;
        this.reserved = reserved;
        this.dataLen = getBodyLength(attribute);
        this.packetBuffer = new byte[dataLen + SIZE_HEAD_PACKET_BYTE + SIZE_FLAG_AND_CHECKCODE];
        setPacketAt(OFFSET_FLAG_START,FLAG);
        setPacketAt(OFFSET_PROVERSION,version);
        setPacketOfRange(OFFSET_ID, DataUtil.shortToByte(id));
        setPacketOfRange(OFFSET_BODYATTRIBUTE, DataUtil.shortToByte(attribute));
        setPacketOfRange(OFFSET_IMEI, DataUtil.longToByte(imei));
        setPacketOfRange(OFFSET_SEQ, DataUtil.shortToByte(seq));
        setPacketAt(OFFSET_RESERVED,reserved);
        //设置包结尾flag
        setPacketAt(getLength()-1,FLAG);
        //打印出发送的包
//        logger.info("length: "+getLength()+",{"+DataUtil.getHexString(packetBuffer)+"}");
    }

    /**
     * 从已有的数据中初始化实例，仅供 ServerPacket 调用
     * @param data
     */
    protected Packet(byte[] data) throws Exception{

        id = AnalysisUtil.byteToShort(Arrays.copyOfRange(data, OFFSET_ID,
                OFFSET_ID + SIZE_ID_BYTE));
        seq = AnalysisUtil.byteToShort(Arrays.copyOfRange(data, OFFSET_SEQ,
                OFFSET_SEQ + SIZE_SEQ_BYTE));
        dataLen = getBodyLength(AnalysisUtil.byteToShort(Arrays.copyOfRange(data,OFFSET_BODYATTRIBUTE,
                OFFSET_BODYATTRIBUTE+SIZE_BODYATTRIBUTE_BYTE)));
        packetBuffer = Arrays.copyOfRange(data, 0, getLength());
        logger.info("receive data from server: "+"{"+ DataUtil.getHexString(packetBuffer)+"}");
    }

    /** 是否做过校验 */
    private boolean validateDone = false;
    /** 校验是否有效 */
    private boolean validated = false;

    //私有数据
    private byte[] packetBuffer;
    private short dataLen;

    private byte proVersion;
    private short id;
    private short bodyAttribute;
    private long imei;
    private short seq;
    private byte reserved;

    public short getDataLen(){return dataLen;}

    /**
     * 获取包的字节，一般用于发送
     * @return 缓存区地址，非拷贝。如果需要修改要先自行拷贝
     * @throws Exception
     */
    public byte[] getBytes() {
        return packetBuffer;
    }

    public int getLength(){
        return dataLen + SIZE_HEAD_PACKET_BYTE + SIZE_FLAG_AND_CHECKCODE;
    }

    public short getSeq(){return this.seq;}

    public short getId(){return this.id;}

    /**
     * 从消息体属性中获取消息体长度
     * @param bodyAttribute 消息体属性
     * @return 消息体长度
     */
    private short getBodyLength(short bodyAttribute) throws Exception{
        byte low = DataUtil.shortToByte(bodyAttribute)[1];
        byte top = DataUtil.shortToByte(bodyAttribute)[0];
        int isZero = top & 1;
        if(isZero==0){
            return (short)low;
        }else{
            return (short)(low + 256);
        }
    }

    /**
     * 包类型
     * @return
     */
    public short getPacketType() {
        try {
            return AnalysisUtil.byteToShort(getPacketOfRange(OFFSET_ID,SIZE_ID_BYTE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 校验有效性，第一次调用时校验并储存结果，后面直接返回
     * @return
     */
    public boolean validate() {
        try {
            if (!validateDone) {
                int digestOffset = getLength() - 2;
                validated = false;
                byte check = getPacketAt(OFFSET_PROVERSION);
                //logger.info("checkcode: "+check + "validated: "+validated);
                for(int i = OFFSET_PROVERSION + 1; i < digestOffset; i++){
                    check = (byte) (getPacketAt(i) ^ check);
                }
                if(check == getPacketAt(digestOffset))
                    validated = true;
                validateDone = true;
                logger.info("checkcode: "+check + "validated: "+validated);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validated;
    }

    protected void setPacketAt(int offset, byte data) throws Exception {
        packetBuffer[offset] = data;
    }

    /**
     * 将 offset (包括) 后的字节设置为 data，设置长度为 data 的长度
     * @param offset
     * @param data
     * @throws Exception
     */
    protected void setPacketOfRange(int offset, byte[] data) throws Exception {
        for (int i = 0; i < data.length; i++) {
            packetBuffer[offset + i] = data[i];
        }
    }

    /**
     * 获取指定offset的byte值
     * @param offset
     * @return
     * @throws Exception
     */
    protected byte getPacketAt(int offset) throws Exception{
        return packetBuffer[offset];
    }

    /**
     * 返回 [from, from + size) 区间数据的拷贝
     * @param offset
     * @param size
     * @return
     */
    protected byte[] getPacketOfRange(int offset, int size) throws Exception {
        return Arrays.copyOfRange(packetBuffer, offset, offset + size);
    }

//    protected abstract void digest() throws Exception;
//    protected abstract void dumpData() throws Exception;

}
