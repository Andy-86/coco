package com.devicemanagement.devicePacket.serverPacket;


import com.devicemanagement.devicePacket.ServerPacket;
import com.devicemanagement.util.AnalysisUtil;

/**
 * Created by Administrator on 2017/9/14 0014.
 *应答流水号	WORD short
 *应答ID	WORD  short
 *结果	BYTE  byte

 */
public class ServerHello extends ServerPacket {


    //size
    protected static final int SIZE_SERVER_SEQ_BYTE = 2;
    protected static final int SIZE_SERVER_ID_BYTE = 2;
    protected static final int SIZE_RESULT_LEN_BYTE = 1;
//    protected static final int SIZE_SERVER_RESULT_BYTE = 1;

    //offset
    protected static final int OFFSET_SERVER_SEQ = 0;
    protected static final int OFFSET_SERVER_ID = OFFSET_SERVER_SEQ
            + SIZE_SERVER_SEQ_BYTE;
    protected static final int OFFSET_RESULT_LEN = OFFSET_SERVER_ID
            + SIZE_SERVER_ID_BYTE;
    protected static final int OFFSET_SERVER_RESULT = OFFSET_RESULT_LEN
            + SIZE_RESULT_LEN_BYTE;

    public static ServerHello fromBytes(byte[] data) {
        try {
            ServerHello packet = new ServerHello(data);
            if (!packet.validate()) {
                return null;
            }
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ServerHello(byte[] data) throws Exception {
        super(TYPE_SERVER_HELLO,data);
    }

    private short server_seq;
    private short server_id;
    private byte len;
    private byte[] result;

    public short getServer_seq(){return server_seq;}
    public short getServer_id(){return server_id;}
    public byte[] getResult(){return result;}
    public byte getLen(){return len;}

    protected void loadData() throws Exception{
        server_seq = AnalysisUtil.byteToShort(getDataOfRange(OFFSET_SERVER_SEQ,SIZE_SERVER_SEQ_BYTE));
        server_id = AnalysisUtil.byteToShort(getDataOfRange(OFFSET_SERVER_ID,SIZE_SERVER_ID_BYTE));
        len = getDataAt(OFFSET_RESULT_LEN);
        result = getDataOfRange(OFFSET_SERVER_RESULT, (int)len);
    }
}
