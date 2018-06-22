package com.devicemanagement.util;

import java.net.ProtocolException;

/**
 * Created by Jim斌 on 2017/9/12.
 */

public class BcdUtil {



    /**
     * @param phone
     * @return
     */
    public static String bcd2Str(byte[] phone) {

        StringBuffer temp = new StringBuffer(phone.length * 2);

        for (int i = 0; i < phone.length; i++) {

            temp.append((byte) ((phone[i] & 0xf0) >>> 4));
            temp.append((byte) (phone[i] & 0x0f));
        }
        while(temp.charAt(0)=='0'){
            temp.deleteCharAt(0);
        }
        return temp.toString();
    }

    public static byte[] str2Bcd(String asc,int destLen) throws ProtocolException {
        int len = asc.length();
        if(len>destLen*2){
            throw new ProtocolException("length not match");
        }
        int mod = len % 2;

        while(destLen*2-len>0||mod != 0){
            asc = "0" + asc;
            len = asc.length();
            mod = len % 2;
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }
}
