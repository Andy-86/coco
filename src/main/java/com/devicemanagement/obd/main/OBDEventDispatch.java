package com.devicemanagement.obd.main;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.devicemanagement.MyApplication;
import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.devicePacket.main.SendData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.data.LightInfo;
import com.devicemanagement.obd.face.OBDEventIble;
import com.devicemanagement.obd.impl.OBDFileEventImpl;
import com.devicemanagement.obd.impl.OBDScmVersionEventImpl;
import com.devicemanagement.obd.impl.OBDStartTansFileEventImpl;
import com.devicemanagement.obd.impl.OBDStartUpgradeEventImpl;
import com.devicemanagement.obd.impl.OBDStopTansFileEventImpl;
import com.devicemanagement.service.BlockQueuePool;
import com.devicemanagement.service.ByteTaker;
import com.devicemanagement.util.AnalysisUtil;
import com.devicemanagement.util.DataUtil;

/**
 * Created by Abner on 2017/5/13.
 * 对于obd返回的数据进行分发到各个接口去处理
 * obd 对于错误发送的命令，接收数据统一为“’A’ , ’T’ , 0x00 , 0x00 , 0x00 , 0x00 , ‘\r’ , ‘\n’”
 * 	对于正在获取VIN Code过程中接收到的指令，接收数据统一为“'A','T','E',0x00,0x00,0x00,'\r','\n'”
 */

public class OBDEventDispatch {
public static final String TAG="OBDEventDispatch";
//    private static Logger logger = Logger.getLogger("OBDEventDispatch");
    public static OBDEventIble dispatch(final byte[] bytes)
    {
        switch (bytes[2])
        {
            case '0':
                return new OBDScmVersionEventImpl();

            //upgarde
            case '7':  //开始传送文件
                return new OBDStartTansFileEventImpl();
            case '8':  //停止传送文件
                return new OBDStopTansFileEventImpl();
            case '9':  //开始升级
                return new OBDStartUpgradeEventImpl();
            case '1':  //文件返回
                return new OBDFileEventImpl();


            //COBO
            case 'd':
                MyLogger.info("控制灯信息返回");
                if(AppData.getIsReceiveCommand() && AppData.getServerCommand() == 0x1A){
                    ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            byte len = 0x01;
                            byte[] mes = DataUtil.hexStringToBytes("31");
                            SendData.sendResponse(AppData.getMessage_seq(), AppData.getMessage_id(), AppData.getCommand_id()
                                    , AppData.getMessage_time(), len, mes);
                        }
                    });
                    AppData.setServerCommand(0);
                    AppData.setIsReceiveCommand(false);
                }
                break;
            case 'L':
                MyLogger.info("light: "+ DataUtil.byteToBit(bytes[4]));
                LightInfo.L1 = (bytes[4] & 0x01) == 1;
                LightInfo.L2 = (bytes[4]>>1 & 0x01) == 1;
                LightInfo.L3 = (bytes[4]>>2 & 0x01) == 1;
                LightInfo.L4 = (bytes[4]>>3 & 0x01) == 1;
                LightInfo.L5 = (bytes[4]>>4 & 0x01) == 1;
                LightInfo.L6 = (bytes[4]>>5 & 0x01) == 1;
                LightInfo.L7 = (bytes[4]>>6 & 0x01) == 1;
                LightInfo.L8 = (bytes[4]>>7 & 0x01) == 1;

                break;
            case 'B':
                if(bytes[4]==0x01){
                    //使用通用应答回复can命令 0x31代表成功 由于没有time 和 command id 所以把他们都设为0
                    byte[] respone={0x31};
                    Log.d(TAG, "dispatch: "+AnalysisUtil.byteArrayToHexStr(bytes));
                    SendData.sendResponse(AppData.getCan_message_seq(),AppData.getCan_message_id(),(byte)0x00,(long) 0,(byte) 0x01,respone);

                }else {
                    Log.d(TAG, "dispatch: "+AnalysisUtil.byteArrayToHexStr(bytes));
                    byte[] respone={0x30};
                    SendData.sendResponse(AppData.getCan_message_seq(),AppData.getCan_message_id(),(byte)0x00,(long) 0,(byte) 0x01,respone);

                }
                break;
            case 'C':
//                int size=10;//计算出can信息的条数
//                byte[] cans;
//                Log.d(TAG, "dispatch: "+AnalysisUtil.byteArrayToHexStr(bytes));
//                cans=getTestCans();
//                LocationManager manager = getLocationManager(MyApplication.getContext());
//
//                    Location location = getBestLocation(manager);
//                    //需要广播的GPS时钟信息
//                    byte[] time=DataUtil.longToByte(location.getTime());
//                    //十一位代表 数据头"AT"+"数据长度"+"GPS时钟信息"  防止出现沾包
//                    byte[] info=new byte[cans.length+11];
//                    info[0]=(byte) 0x41;
//                    info[1]=(byte) 0x54;
//                    info[2]=(byte) size;
//                    System.arraycopy(cans,0,info,3,cans.length);
//                    System.arraycopy(time,0,info,cans.length+3,8);
//                    BlockQueuePool.queue.offer(new ByteTaker(info));

                    int size=bytes[3];
                    Log.d(TAG, "dispatch: "+AnalysisUtil.byteArrayToHexStr(bytes));
//防止数据错误
                    if(bytes.length<size*12+4)
                        break;

                    byte[] cans=new byte[size*12];

                    System.arraycopy(bytes,5,cans,0,size*12);

                    LocationManager manager = getLocationManager(MyApplication.getContext());

                    Location location = getBestLocation(manager);
                    //需要广播的GPS时钟信息
                    byte[] time=DataUtil.longToByte(location.getTime());
                    //十一位代表 数据头"AT"+"数据长度"+"GPS时钟信息"  防止出现粘包
                    byte[] info=new byte[cans.length+11];
                    info[0]=(byte) 0x41;
                    info[1]=(byte) 0x54;
                    info[2]=(byte) size;
                    System.arraycopy(cans,0,info,3,cans.length);
                    System.arraycopy(time,0,info,cans.length+3,8);
                    BlockQueuePool.queue.offer(new ByteTaker(info));



//                for(int i=0;i<size;i++){
//                    byte[] can=new byte[12];
//                    System.arraycopy(bytes,4+i*12,can,0,12);
//                    byte[] id=new byte[4];
//                    System.arraycopy(can,0,id,0,4);
//                    try {
//                        ClientCan clientCan=new ClientCan((byte)0x05, Packet.TYPE_CLIENT_CAN, ClientCan.SIZE_CAN_BYTE,
//                                AppData.getImei(), Seq.getDeviceSeq(),Packet.RESERVED,AnalysisUtil.byteToInt(id),can[4],can[5],can[6],can[7],can[8],can[9],can[10],can[11]);
//                        System.arraycopy(clientCan.getDataBuffer(),0,cans,i*13,13);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    ClientCans clientCans =new ClientCans((byte) 0x05, Packet.TYPE_CLIENT_CAN,(short) cans.length,
//                            (long) 0, Seq.getDeviceSeq(), Packet.RESERVED,cans);
////                    SendData.sendClientCans(clientCans);
//                    LocationManager manager = getLocationManager(MyApplication.getContext());
//
//                    Location location = getBestLocation(manager);
//                    //需要广播的GPS时钟信息
//                    byte[] time=DataUtil.longToByte(location.getTime());
//                    //十一位代表 数据头"AT"+"数据长度"+"GPS时钟信息"  防止出现沾包
//                    byte[] info=new byte[cans.length+11];
//                    info[0]=(byte) 0x41;
//                    info[1]=(byte) 0x54;
//                    info[2]=(byte) size;
//                    System.arraycopy(cans,0,info,3,cans.length);
//                    System.arraycopy(time,0,info,cans.length+3,8);
//                    BlockQueuePool.queue.offer(new ByteTaker(info));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                break;

            default:
                Log.d(TAG, "dispatch: "+AnalysisUtil.byteArrayToHexStr(bytes));
                break;
        }
        return null;
    }

    private static LocationManager getLocationManager(@NonNull Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private static Location getBestLocation(LocationManager locationManager) {
        Location result = null;
        if (locationManager != null) {
            result = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (result != null) {
                return result;
            } else {
                result = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                return result;
            }
        }
        return result;
    }

    public static byte[] getTestCans(){
        int size=10;//计算出can信息的条数
        byte[] bytes=new byte[size*12];
        for(int i=0;i<size;i++){
            byte[] can=new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
            System.arraycopy(can,0*12,bytes,i*12,12);
        }
        return bytes;
    }
}
