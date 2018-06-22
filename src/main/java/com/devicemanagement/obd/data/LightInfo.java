package com.devicemanagement.obd.data;


import com.devicemanagement.util.DataUtil;

/**
 * Created by Administrator on 2018-04-08.
 *
 */
public class LightInfo {

    public volatile static boolean L1 = false;
    public volatile static boolean L2 = false;
    public volatile static boolean L3 = false;
    public volatile static boolean L4 = false;
    public volatile static boolean L5 = false;
    public volatile static boolean L6 = false;
    public volatile static boolean L7 = false;
    public volatile static boolean L8 = false;


    public static byte[] getStatus(){
        byte[] lightinfo = new byte[2];
        String bitStr = "";
        int i;
        for(i=0;i<2;i++){
            lightinfo[i]=0;
        }
        //转化为bit数组
        byte m1[] = DataUtil.getBitArray(lightinfo[0]);
        if(L2) {
            m1[7] = 1;
        }else{
            m1[7] = 0;
        }
        if(L3) {
            m1[6] = 1;
        }else{
            m1[6] = 0;
        }
        if(L4) {
            m1[5] = 1;
        }else{
            m1[5] = 0;
        }
        for(i = 0; i < 5;i++){
            m1[i] = 0;
        }
        for(i=0;i<8;i++){
            bitStr+=m1[i];
        }
        lightinfo[0]= DataUtil.BitToByte(bitStr);

        byte m2[] = DataUtil.getBitArray(lightinfo[1]);
        if(L5) {
            m2[7] = 1;
        }else{
            m2[7] = 0;
        }
        if(L6) {
            m2[6] = 1;
        }else{
            m2[6] = 0;
        }
        if(L7) {
            m2[5] = 1;
        }else{
            m2[5] = 0;
        }
        if(L8) {
            m2[4] = 1;
        }else{
            m2[4] = 0;
        }
        for(i = 0; i < 4;i++){
            m2[i] = 0;
        }

        bitStr = "";
        //构造一字节
        for(i=0;i<8;i++){
            bitStr+=m2[i];
        }
        lightinfo[1]= DataUtil.BitToByte(bitStr);
        return lightinfo;
    }

}
