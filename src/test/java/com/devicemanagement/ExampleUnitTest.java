package com.devicemanagement;

import com.devicemanagement.devicePacket.serverPacket.ServerCan;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getData() {
        byte[] bytes = new byte[]{(byte)0x7e, (byte)0x05, (byte)0x10, (byte)0x05, (byte)0x00, (byte)0x0d,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3c, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x28, (byte)0x7e, };
        ServerCan serverCan = ServerCan.fromBytes(bytes);
        System.out.println(getHexString(serverCan.getBytes()));
        if (serverCan != null) {
            byte[] b = new byte[2];
            byte[] command = new byte[14];
            System.arraycopy("BB".getBytes(), 0, command, 0, 2);
            System.arraycopy(serverCan.getDataBuffer(), 0, command, 2, 4);
            System.arraycopy(serverCan.getDataBuffer(), 5, command, 6, 8);

            System.out.println(getHexString(command));
        }
        }
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