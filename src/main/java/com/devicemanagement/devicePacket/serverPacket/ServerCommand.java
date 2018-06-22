package com.devicemanagement.devicePacket.serverPacket;

import com.devicemanagement.util.AnalysisUtil;
import com.devicemanagement.devicePacket.ServerPacket;

/**
 * Created by Administrator on 2017/9/15 0015.
 * 命令ID (1byte)|	消息长度 (1byte,无特别说明时为0) | 	消息内容(长度可变)
 */
public class ServerCommand extends ServerPacket {

    //size
    protected static final int SIZE_COMMAND_ID_BYTE = 1;
    protected static final int SIZE_TIME = 8;
    protected static final int SIZE_MESSAGE_LENGTH_BYTE = 1;

    //offset
    protected static final int OFFSET_COMMAND_ID = 0;
    protected static final int OFFSET_TIME = OFFSET_COMMAND_ID
            + SIZE_COMMAND_ID_BYTE;
    protected static final int OFFSET_MESSAGE_LENGTH = OFFSET_TIME
            + SIZE_TIME;
    protected static final int OFFSET_MESSAGE = OFFSET_MESSAGE_LENGTH
            + SIZE_MESSAGE_LENGTH_BYTE;

    //size except message
    /**指令id (1byte) + 时间戳(8byte) + 消息长度(1byte)  1 + 8 + 1 =10*/
    public static final int SIZE_EXCEPT_MESSAGE = SIZE_COMMAND_ID_BYTE +
            SIZE_TIME + SIZE_MESSAGE_LENGTH_BYTE;

    public static ServerCommand fromBytes(byte[] data) {
        try {
            ServerCommand packet = new ServerCommand(data);
            if (!packet.validate()) {
                return null;
            }
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ServerCommand(byte[] data) throws Exception{
        super(TYPE_SERVER_COMMAND,data);
    }

    private byte command_id;
    private byte message_len;
    private long time;
    private byte[] message;

    /** 类成员变量的setter/getter 方法 */
    public byte getCommand_id(){return command_id;}
    public byte getMessage_len(){return message_len;}
    public byte[] getMessage(){return message;}
    public long getTime(){return time;}

    protected void loadData() throws Exception{
        command_id = getDataAt(OFFSET_COMMAND_ID);
        time = AnalysisUtil.byteToLong(getDataOfRange(OFFSET_TIME,SIZE_TIME));
        message_len = getDataAt(OFFSET_MESSAGE_LENGTH);
        message = getDataOfRange(OFFSET_MESSAGE,getDataLen() - SIZE_EXCEPT_MESSAGE);
    }
}
