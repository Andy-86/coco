package com.devicemanagement.obd.main;

import com.devicemanagement.data.Seq;
import com.devicemanagement.devicePacket.Packet;
import com.devicemanagement.devicePacket.clientPacket.ClientCan;
import com.devicemanagement.devicePacket.clientPacket.ClientCans;
import com.devicemanagement.util.AnalysisUtil;

import org.junit.Test;

/**
 * Created by mac on 2018/5/22.
 */
public class OBDEventDispatchTest {
    @Test
    public void dispatch() throws Exception {
        int size=8;//计算出can信息的条数
        byte[] cans=new byte[size*13];
        byte[] bytes=new byte[size*12];
        for(int i=0;i<size;i++){
            byte[] can=new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
            System.arraycopy(can,0*12,bytes,i*12,12);
        }
        System.out.println(getHexString(bytes));
        for(int i=0;i<size;i++) {
            byte[] can=new byte[12];
            System.arraycopy(bytes,i*12,can,0,12);
            byte[] id=new byte[4];
            System.arraycopy(can,0,id,0,4);
            ClientCan clientCan = new ClientCan((byte) 0x05, Packet.TYPE_SERVER_CAN, ClientCan.SIZE_CAN_BYTE,
                    (long) 0, Seq.getDeviceSeq(), Packet.RESERVED, AnalysisUtil.byteToInt(id), can[4], can[5], can[6], can[7], can[8], can[9], can[10], can[11]);
            System.out.println(getHexString(clientCan.getBytes()));
            System.arraycopy(clientCan.getDataBuffer(),0,cans,i*13,13);
        }
    ClientCans clientCans =new ClientCans((byte) 0x05, Packet.TYPE_CLIENT_CAN,(short) cans.length,
           (long) 0, Seq.getDeviceSeq(), Packet.RESERVED,cans);
        System.out.println(getHexString(clientCans.getBytes()));
    }

    //bytes to string
    public static String getHexString(byte[] bs) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bs == null || bs.length <= 0) {
            return null;
        }
        for (int i = 0; i < bs.length; i++) {
            int v = bs[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }


}