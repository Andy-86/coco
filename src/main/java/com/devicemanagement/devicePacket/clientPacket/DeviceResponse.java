package com.devicemanagement.devicePacket.clientPacket;

import com.devicemanagement.util.DataUtil;
import com.devicemanagement.devicePacket.ClientPacket;

/**
 * Created by Administrator on 2017/9/14 0014.
 * @author lg
 * 应答流水号	WORD
 * 应答ID	WORD
 * 应答命令ID	1 byte
 * 结果长度	BYTE
 * 结果	String
 */
public class DeviceResponse extends ClientPacket {

    //size
    protected static final int SIZE_RESPONSE_SEQ_BYTE = 2;
    protected static final int SIZE_RESPNSE_ID_BYTE = 2;
    protected static final int SIZE_RESPONSE_COMMAND_ID_BYTE = 1;
    protected static final int SIZE_RESPONSE_TIME_BYTE = 8;
    protected static final int SIZE_RESPONSE_LENGTH_BYTE = 1;
    //protected static final int SIZE_RESPONSE_RESULT_BYTE

    //offset
    protected static final int OFFSET_RESPONSE_SEQ = 0;
    protected static final int OFFSET_RESPONSE_ID = OFFSET_RESPONSE_SEQ
            + SIZE_RESPONSE_SEQ_BYTE;
    protected static final int OFFSET_RESPONSE_COMMAND_ID = OFFSET_RESPONSE_ID
            + SIZE_RESPNSE_ID_BYTE;
    protected static final int OFFSET_RESPONSE_TIME = OFFSET_RESPONSE_COMMAND_ID
            + SIZE_RESPONSE_COMMAND_ID_BYTE;
    protected static final int OFFSET_RESPONSE_LENGTH = OFFSET_RESPONSE_TIME
            + SIZE_RESPONSE_TIME_BYTE;
    protected static final int OFFSET_RESPONSE_RESULT = OFFSET_RESPONSE_LENGTH
            + SIZE_RESPONSE_LENGTH_BYTE;

    /**DeviceResponse packet length except message  2+2+1+8+1 = 14*/
    public static final short SIZE_REPONSE_PACKET_BYTE = SIZE_RESPONSE_SEQ_BYTE + SIZE_RESPNSE_ID_BYTE
            + SIZE_RESPONSE_COMMAND_ID_BYTE + SIZE_RESPONSE_TIME_BYTE + SIZE_RESPONSE_LENGTH_BYTE;

    public DeviceResponse(byte version,short id,short attribute,long imei,short seq,byte obl,
                          short res_seq, short message_id, byte command_id, long time,byte len, byte[] message) throws Exception{
        super(version,id,attribute,imei,seq,obl);
        this.res_seq = res_seq;
        this.message_id = message_id;
        this.command_id = command_id;
        this.res_time = time;
        this.len = len;
        this.message = message;
        callAfterSetMemberDone();
    }

    private short res_seq;
    private short message_id;
    private byte command_id;
    private long res_time;
    private byte len;
    private byte[] message;

    @Override
    protected void dumpData() throws Exception{
        setDataOfRange(OFFSET_RESPONSE_SEQ, DataUtil.shortToByte(this.res_seq));
        setDataOfRange(OFFSET_RESPONSE_ID, DataUtil.shortToByte(this.message_id));
        setDataAt(OFFSET_RESPONSE_COMMAND_ID,this.command_id);
        setDataOfRange(OFFSET_RESPONSE_TIME, DataUtil.longToByte(this.res_time));
        setDataAt(OFFSET_RESPONSE_LENGTH,this.len);
        setDataOfRange(OFFSET_RESPONSE_RESULT,this.message);
    }

}
