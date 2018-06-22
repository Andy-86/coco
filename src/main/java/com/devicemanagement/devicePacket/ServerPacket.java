package com.devicemanagement.devicePacket;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/9/14 0014.
 *
 */
public abstract class ServerPacket extends Packet {

    protected ServerPacket(short type, byte[] data) throws Exception {
        super(data);
        if (getPacketType() != type) {
            throw new IllegalArgumentException("错误的数据包类型。");
        }
        if (validate()) {
            paintextCopy();
            loadData();
        }
    }

    private byte[] dataBuffer = null;

    public byte[] getDataBuffer() {
        return dataBuffer;
    }

    private void paintextCopy() throws Exception {
        dataBuffer = getPacketOfRange(OFFSET_DATA_BODY, getDataLen());
    }

    protected byte getDataAt(int offset) throws Exception {
        return dataBuffer[offset];
    }

    /**
     * 返回 [from, from + size) 区间数据的拷贝
     * @param offset
     * @param size
     * @return
     */
    protected byte[] getDataOfRange(int offset, int size) throws Exception {
        return Arrays.copyOfRange(dataBuffer, offset, offset + size);
    }

    /**
     * 从解密之后的  dataBuffer 中解析各个字段的值，具体解析过程跟包结构有关，由子类实现
     * @throws Exception
     */
    protected abstract void loadData() throws Exception;
}
