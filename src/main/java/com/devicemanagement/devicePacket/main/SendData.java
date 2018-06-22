package com.devicemanagement.devicePacket.main;

import android.content.Intent;

import com.devicemanagement.BuildConfig;
import com.devicemanagement.MyApplication;
import com.devicemanagement.data.AppData;
import com.devicemanagement.data.Seq;
import com.devicemanagement.devicePacket.Packet;
import com.devicemanagement.devicePacket.clientPacket.ClientCans;
import com.devicemanagement.devicePacket.clientPacket.DeviceHello;
import com.devicemanagement.devicePacket.clientPacket.DeviceResponse;
import com.devicemanagement.devicePacket.clientPacket.Heartbeat;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.util.DataUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class SendData {

    private static DatagramSocket datagramSocket = null;

    /**标志是否建立虚连接*/
    private static boolean isConnected=false;

    /**  发送异常标志 */
    private static int exceptionCounts=0;


    public static void sendData(byte[] manager, String host,
                                int port) {
        InetAddress addr = null;
        try {
            if (datagramSocket == null) {
                //port 这个是绑定本地的窗口
                datagramSocket = new DatagramSocket(55050);
            }
            addr = InetAddress.getByName(host);
            //port 这个是绑定远程的服务器地址和窗口
            DatagramPacket datagramPacket = new DatagramPacket(manager,
                    manager.length, addr, port);

            //MyLogger.info(datagramPacket.getSocketAddress()+" "+ DataUtil.getHexString(manager));
            datagramSocket.send(datagramPacket);
            exceptionCounts = 0;
        } catch (Exception e) {
            e.printStackTrace();
            exceptionCounts++;
            MyLogger.error("udp发送数据异常"+e);
        }
    }

    /**
     * 接收服务器下发的数据包
     */
    public static void receiveData() {
        byte[] recvBuf = new byte[420];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        try {
            if (datagramSocket == null) {
                datagramSocket = new DatagramSocket(55050);
            }
            //gMyLogger.error("本地端口："+datagramSocket.getLocalPort());
            datagramSocket.receive(recvPacket);
            byte[] bodyAttri = new byte[2];
            int len = 0;
            bodyAttri[0] = recvPacket.getData()[5];
            bodyAttri[1] = recvPacket.getData()[4];
            if((bodyAttri[0] >> 7 & 0x01) > 0){
                for(int i = 7; i >= 0; i--){
                    int t = (bodyAttri[0] >> i) & 0x01;
                    if(t > 0){
                        len += Math.pow(2,i);
                    }
                }
            }else{
                len = bodyAttri[0];
                if((bodyAttri[1] & 0x01) > 0){
                    len += Math.pow(2,8);
                }
                if((bodyAttri[1] >> 1 & 0x01) > 0){
                    len += Math.pow(2,9);
                }
            }
            MyLogger.info("body len: "+len);
            len = len + 19;
            byte[] data = new byte[len];
            System.arraycopy(recvPacket.getData(),0,data,0,len);
            MyApplication.getInstance().sendBroadcast(new Intent(ACTION_UDP_COMMAND).putExtra
                    (EXTRA_UDP_COMMAND,data));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭UDP
     * @return whether if close the datagramSocket.
     */
    public static boolean stopUdp() {
        try {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket=null;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * udp的ACTION
     */
    public static String ACTION_UDP_COMMAND = "com.imdroid.jrhb.ACTION_UDP_COMMAND";
    /**
     * udp的指令
     */
    public static String EXTRA_UDP_COMMAND = "com.imdroid.jrhb.EXTRA_UDP_COMMAND";


    public static void setIsConnected(boolean connected){isConnected = connected;}
    public static boolean getIsConnected(){return isConnected;}

    public static void setExceptionCounts(int exce){exceptionCounts = exce;}
    public static int getExceptionCounts(){return exceptionCounts;}

    /**
     * 发送Hello包
     */
    public static void sendDeviceHello(short type,byte customer,byte simOperator,byte[] iccid,short vin){
        DeviceHello deviceHello = null;
        byte[] deviceHelloBytes = null;
        try{
            short versioncode = (short) BuildConfig.VERSION_CODE;
            //short len = (short) (DeviceHello.SIZE_DEVICE_HELLO_BYTE + 2);
            deviceHello = new DeviceHello((byte)0x05, Packet.TYPE_DEVICE_HELLO,DeviceHello.SIZE_DEVICE_HELLO_BYTE,
                    AppData.getImei(), Seq.getDeviceSeq(),Packet.RESERVED,type,customer,simOperator,iccid,versioncode,vin);
            deviceHelloBytes = deviceHello.getBytes();
            if(deviceHelloBytes != null){
                sendData(deviceHelloBytes, AppData.getIP(),AppData.getPORT());
                MyLogger.info("Send deviceHello succeed");
                MyLogger.info("Hello: "+ DataUtil.getHexString(deviceHelloBytes));
            }else{
                MyLogger.info("Send deviceHello failed! ");
            }
        }catch (Exception e){
            MyLogger.error(e.toString());
        }
    }

    /**
     * 发送心跳包
     */
    public static void sendHeartbeatPacket(){
        Seq.addDeviceSeq();
        Heartbeat heartbeat = null;
        byte[] heartbeatBytes = null;
        try {
            heartbeat = new Heartbeat(Packet.VERSION,Packet.TYPE_CLIENT_HEARTBEAT,Heartbeat.SIZE_HEARTBEAT_DATA_BYTE,
                    AppData.getImei(), Seq.getDeviceSeq(),Packet.RESERVED);
            heartbeatBytes = heartbeat.getBytes();
            if(heartbeatBytes != null) {
                sendData(heartbeatBytes, AppData.getIP(), AppData.getPORT());
                MyLogger.info("send heartbeat succeed"+"序列号是："+Seq.getDeviceSeq());
//                MyLogger.info("length: "+heartbeatBytes.length+",{"+ DataUtil.getHexString(heartbeatBytes)+"}");
            }else{
                MyLogger.info("HeartBeat Send failed! ");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  发送响应包
     * @param res_seq 应答消息seq
     * @param message_id  应答消息id
     * @param command_id 应答命令消息id
     * @param len 响应结果长度
     * @param message 响应结果
     */
    public static void sendResponse(short res_seq, short message_id, byte command_id,long time, byte len, byte[] message){
        Seq.addDeviceSeq();
        DeviceResponse deviceResponse = null;
        byte[] deviceResponseBytes = null;
        try{
            short responseLen = (short)(DeviceResponse.SIZE_REPONSE_PACKET_BYTE + len);
            deviceResponse = new DeviceResponse((byte)0x02,Packet.TYPE_CLIENT_RESPONSE,responseLen,
                    AppData.getImei(), Seq.getDeviceSeq(),Packet.RESERVED,res_seq,message_id,command_id,time,len,message);
            deviceResponseBytes = deviceResponse.getBytes();
            if(deviceResponseBytes != null){
                sendData(deviceResponseBytes,AppData.getIP(),AppData.getPORT());
                MyLogger.info("send response succeed"+"序列号是："+Seq.getDeviceSeq());
                MyLogger.info("response: "+DataUtil.getHexString(deviceResponseBytes));
            }else{
                MyLogger.info("send response failed for bytes is null");
            }
        }catch (Exception e){
            MyLogger.error(e.toString());
        }
    }

    public static void sendClientCans(ClientCans clientCans){
        byte[] deviceResponseBytes = null;
        deviceResponseBytes=clientCans.getBytes();
        if(deviceResponseBytes != null){
            sendData(deviceResponseBytes,AppData.getIP(),AppData.getPORT());
        }else{
        }
    }


//    public static void sendLoginPacket(short id, long time, byte[] idnumber){
//        Seq.addDeviceSeq();
//        ClientLogin clientLogin = null;
//        byte[] loginBytes = null;
//        try {
//            clientLogin = new ClientLogin((byte)0x05, id,
//                    AppData.getImei(), Seq.getDeviceSeq(),Packet.RESERVED, MLocation.getLat(),MLocation.getLon(),
//                    time,(byte)idnumber.length,idnumber);
//            loginBytes = clientLogin.getBytes();
//            if(loginBytes != null) {
//                sendData(loginBytes, AppData.getIP(), AppData.getPORT());
//                MyLogger.info("login length: "+loginBytes.length+",{"+ DataUtil.getHexString(loginBytes)+"}");
//            }else{
//                MyLogger.info("login packet Send failed! ");
//            }
//        }catch (Exception e){
//            MyLogger.error(e.toString());
//        }
//    }
}
