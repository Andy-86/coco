package com.devicemanagement.devicePacket.dispose;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager;

import com.devicemanagement.CameraActivity;
import com.devicemanagement.MyApplication;
import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.data.MLocation;
import com.devicemanagement.database.SPTool;
import com.devicemanagement.devicePacket.Packet;
import com.devicemanagement.devicePacket.clientPacket.DeviceHello;
import com.devicemanagement.devicePacket.main.SendData;
import com.devicemanagement.devicePacket.serverPacket.ServerCan;
import com.devicemanagement.devicePacket.serverPacket.ServerCommand;
import com.devicemanagement.devicePacket.serverPacket.ServerHello;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.phoneState.MobileInfo;
import com.devicemanagement.phoneState.PhoneInfoUtils;
import com.devicemanagement.service.AppLogService;
import com.devicemanagement.service.PushApkService;
import com.devicemanagement.service.UpdateService;
import com.devicemanagement.util.DataUtil;
import com.devicemanagement.util.FileUtil;
import com.devicemanagement.util.OBDMethod;
import com.devicemanagement.util.SftpClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Created by Administrator on 2017/9/8 0008.
 * 对接收到的服务器下发的数据包进行分析处理
 */
public class UdpEventDispose {


    /** 服务器包类型，变量*/
    private static ServerCommand serverCommand = null;
    private static ServerHello serverHello = null;
    private static ServerCan serverCan=null;
    private static short message_id;
    private static short message_seq;
    private static byte command_id;
    private static byte[] result;
    private static byte[] command;
    private static long time;

    //包类型
    private static short packetType = 0;
    public static void dispose(final byte[] bytes){
        packetType = DataUtil.ByteToShort(bytes[2],bytes[3]);
        MyLogger.info("packet type: "+packetType);
        switch (packetType){
            //将can命令发给单片机
            case Packet.TYPE_SERVER_CAN:
                serverCan=ServerCan.fromBytes(bytes);
                if(serverCan!=null){
                    byte[] b=new byte[2];
                   command=new byte[14];
                    System.arraycopy("BB".getBytes(),0,command,0,2);
                   System.arraycopy(serverCan.getDataBuffer(),0,command,2,4);
                   System.arraycopy(serverCan.getDataBuffer(),5,command,6,8);
                   MyLogger.info(""+command);
                   OBDMethod.makeSynchronizedByBlockingQueue(command);
                    MyLogger.info("收到ServerCommand: " +serverCan.getId());
                    AppData.setCan_message_id(serverCan.getId());
                    AppData.setCan_message_seq(serverCan.getSeq());
                }else {
                    MyLogger.info("校验不通过");
                }
                break;
            case Packet.TYPE_SERVER_HELLO:
                try {
                    serverHello = ServerHello.fromBytes(bytes);
                    if (serverHello != null) {
                        result = serverHello.getResult();
                    } else {
                        result = null;
                        MyLogger.info("校验不通过");
                        break;
                    }
                } catch (Exception e) {
                    MyLogger.error(e.toString());
                    result = null;
                    break;
                }
                switch (serverHello.getServer_id()) {
                    case Packet.TYPE_DEVICE_HELLO:
                        if (result[0] == 0) {
                            MyLogger.info("收到ServerHello,成功");
                            try {
                                short day = DataUtil.ByteToShort(result[1], result[2]);
                                AppData.setDeviceAvaTime(day);
                            }catch (Exception e){
                                MyLogger.error(e.toString());
                            }
                            MyLogger.info("Device Available days: "+ AppData.getDeviceAvaTime());
                            SendData.setIsConnected(true);   //连接建立
                        } else if (result[0] == 1) {
                            MyLogger.info("收到ServerHello，失败");
                        } else if (result[0] == 2) {
                            MyLogger.info("收到ServerHello，消息有误");
                        } else if (result[0] == 3) {
                            MyLogger.info("收到ServerHello，不支持");
                        } else if (result[0] == 4) {
                            MyLogger.info("收到ServerHello，升级");
                            SendData.setIsConnected(true);   //连接建立
                            Intent intent_update = new Intent(MyApplication.getContext(), UpdateService.class);
                            MyApplication.getContext().startService(intent_update);
                        }
                        break;
//                    case Packet.TYPE_CLIENT_LOGIN:
//                        if(result[0] == 0){
//                            MyLogger.info("登录成功");
//                            //允许按键板操作
//                            byte[] obdcommand = new byte[3];
//                            System.arraycopy(Config.OBD_OPERATION_PUMPER_2.getBytes(),0,obdcommand,0,2);
//                            obdcommand[2] = 0x01;
//                            SendObdData.getInstance().sendCommand(obdcommand);
//                        }else if(result[0] == 1){
//                            MyLogger.info("登录失败");
//                        }
//                        break;
//                    case Packet.TYPE_CLIENT_EXIT:
//                        if(result[0] == 0){
//                            MyLogger.info("登出成功");
//                        }else if(result[0] == 1){
//                            MyLogger.info("登出失败");
//                        }
//                        break;
                }
                break;
            case Packet.TYPE_SERVER_COMMAND:
                try {
                    serverCommand = ServerCommand.fromBytes(bytes);
                    if (serverCommand != null) {
                        command_id = serverCommand.getCommand_id();
                        message_seq = serverCommand.getSeq();
                        message_id = serverCommand.getId();
                        time = serverCommand.getTime();
                    } else {
                        command_id = 0;
                        break;
                    }
                } catch (Exception e) {
                    MyLogger.error(e.toString());
                    command_id = 0;
                    break;
                }
                MyLogger.info("收到ServerCommand: " +command_id);
                AppData.setMessage_id(message_id);
                AppData.setCommand_id(command_id);
                AppData.setMessage_seq(message_seq);
                AppData.setMessage_time(time);
                /**响应包变量*/
                final byte len;
                final byte[] message;
                switch (command_id){
                    case 0x01:   //获取gps
                        MyLogger.info("纬度："+ MLocation.getLat() + ", 经度："+MLocation.getLon());
                        StringBuilder builderStr = new StringBuilder();
                        builderStr.append(String.valueOf(MLocation.getLat()));
                        builderStr.append(",");
                        builderStr.append(String.valueOf(MLocation.getLon()));
                        message = builderStr.toString().getBytes();
                        len = (byte)message.length;
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                            }
                        });
                        break;
                    case 0x02:  //上传日志
                        len = 0x01;
                        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    byte[] mes;
                                    List<File> uploadFiles = null;
                                    boolean isConnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(),
                                            AppData.getUsername(), AppData.getPassword());
                                    if (isConnected) {
                                        uploadFiles = FileUtil.findCurDayLog(Environment.getExternalStorageDirectory() + "/jrhb");
                                        if (uploadFiles != null) {
                                            for (File file : uploadFiles) {
                                                SftpClient.getInStance().upload(AppData.getUploadLogDirectory(),
                                                        file.getAbsolutePath(), SftpClient.getChannelSftp());
                                            }
                                            mes = DataUtil.hexStringToBytes("31");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("upload current time log succeed.");
                                        }else{
                                            mes = DataUtil.hexStringToBytes("30");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("no current log to upload.");
                                        }
                                    } else {
                                        mes = DataUtil.hexStringToBytes("30");
                                        SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                        MyLogger.info("cannot connect to server.");
                                    }
                                }catch (Exception e){
                                    byte[] mes = DataUtil.hexStringToBytes("30");
                                    SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                    MyLogger.error(e.toString());
                                }finally {
                                    SftpClient.getInStance().stopSftpConnect();
                                }
                            }
                        });
                        break;
                    case 0x03:  //推送apk
                        Intent intent_pushApk = new Intent(MyApplication.getInstance(), PushApkService.class);
                        MyApplication.getInstance().startService(intent_pushApk);
                        break;
                    case 0x04: //上传vincode
                        message = DataUtil.hexStringToBytes("30");
                        len = (byte) message.length;
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                            }
                        });

                        break;
                    case 0x05:  //上传新安装app列表
                        message = DataUtil.hexStringToBytes("30");
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                List<String> applist = PhoneInfoUtils.scanLocalInstallAppList(MyApplication.getInstance()
                                .getPackageManager());
                                byte length;
                                byte[] list_bytes;
                                if(applist.isEmpty()) {
                                    length = 0x01;
                                    list_bytes = DataUtil.hexStringToBytes("30");
                                }else {
                                    StringBuffer appStrBuf = new StringBuffer();
                                    for(int i = 0;i < applist.size();i++){
                                        appStrBuf.append(applist.get(i));
                                        appStrBuf.append("|");
                                    }
                                    String appStr = appStrBuf.toString();
                                    list_bytes = appStr.getBytes();
                                    length = (byte)list_bytes.length;
                                }
                                if(length <= 0){
                                    SendData.sendResponse(message_seq, message_id, command_id, time, (byte)0x01, message);
                                }else {
                                    SendData.sendResponse(message_seq, message_id, command_id, time, length, list_bytes);
                                }
                            }
                        });
                        break;
                    case 0x06:  //自动升级
                        Intent intent_upgade = new Intent(MyApplication.getInstance(), UpdateService.class);
                        MyApplication.getInstance().startService(intent_upgade);
                        break;
                    case 0x07:  //上传设备管理日志
                        len = 0x01;
                        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    byte[] mes;
                                    List<File> uploadFiles = null;
                                    boolean isConnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(),
                                            AppData.getUsername(), AppData.getPassword());
                                    if (isConnected) {
                                        uploadFiles = FileUtil.findCurDayLog(Environment.getExternalStorageDirectory() + "/jrhb");
                                        if (uploadFiles != null) {
                                            for (File file : uploadFiles) {
                                                SftpClient.getInStance().upload(AppData.getUploadLogDirectory(),
                                                        file.getAbsolutePath(), SftpClient.getChannelSftp());
                                            }
                                            mes = DataUtil.hexStringToBytes("31");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("upload current time log succeed.");
                                        }else{
                                            mes = DataUtil.hexStringToBytes("30");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("no current log to upload.");
                                        }
                                    } else {
                                        mes = DataUtil.hexStringToBytes("30");
                                        SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                        MyLogger.info("cannot connect to server.");
                                    }
                                }catch (Exception e){
                                    byte[] mes = DataUtil.hexStringToBytes("30");
                                    SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                    MyLogger.error(e.toString());
                                }finally {
                                    SftpClient.getInStance().stopSftpConnect();
                                }
                            }
                        });
                        break;
                    case 0x08:  //立即拍照并上传照片  前置
                        len = 0x01;
                        message = DataUtil.hexStringToBytes("31");
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                            }
                        });
                        Intent intent_photo = new Intent(MyApplication.getInstance(), CameraActivity.class);
                        intent_photo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent_photo.putExtra(CameraActivity.EXTRA_CAMERA, CameraActivity.CAMERA_FRONT);
                        MyApplication.getInstance().startActivity(intent_photo);
                        break;
                    case 0x09:  //重启
                        len = 0x01;
                        message = DataUtil.hexStringToBytes("31");
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                                    Thread.sleep(2000);
                                    PowerManager pManager=(PowerManager) MyApplication.getInstance().
                                            getSystemService(Context.POWER_SERVICE); //重启到fastboot模式
                                    pManager.reboot("");
                                } catch (Exception ioe) {
                                    MyLogger.error(ioe.toString());
                                    byte[] mes = DataUtil.hexStringToBytes("30");
                                    SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                }
                            }
                        });
                        break;
                    case 0x0A:   //上传rom版本号
                        message = AppData.getRomVersion().getBytes();
                        len = (byte)message.length;
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                            }
                        });
                        break;
                    case 0x0B:  //上传叮咚全部的日志
                        len = 0x01;
                        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    byte[] mes;
                                    List<File> uploadFiles = null;
                                    boolean isConnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(),
                                            AppData.getUsername(), AppData.getPassword());
                                    if (isConnected) {
                                        uploadFiles = FileUtil.findAllLog(Environment.getExternalStorageDirectory() + "/jrhb");
                                        if (uploadFiles != null) {
                                            for (File file : uploadFiles) {
                                                SftpClient.getInStance().upload(AppData.getUploadLogDirectory(),
                                                        file.getAbsolutePath(), SftpClient.getChannelSftp());
                                            }
                                            mes = DataUtil.hexStringToBytes("31");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("upload current time log succeed.");
                                        }else{
                                            mes = DataUtil.hexStringToBytes("30");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("no current log to upload.");
                                        }
                                    } else {
                                        mes = DataUtil.hexStringToBytes("30");
                                        SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                        MyLogger.info("cannot connect to server.");
                                    }
                                }catch (Exception e){
                                    byte[] mes = DataUtil.hexStringToBytes("30");
                                    SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                    MyLogger.error(e.toString());
                                }finally {
                                    SftpClient.getInStance().stopSftpConnect();
                                }
                            }
                        });
                        break;
                    case 0x0C:  //上传设备管理保存的全部日志
                        len = 0x01;
                        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    byte[] mes;
                                    List<File> uploadFiles = null;
                                    boolean isConnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(),
                                            AppData.getUsername(), AppData.getPassword());
                                    if (isConnected) {
                                        uploadFiles = FileUtil.findAllLog(Environment.getExternalStorageDirectory() + "/ImdroidDeviceLog");
                                        if (uploadFiles != null) {
                                            for (File file : uploadFiles) {
                                                SftpClient.getInStance().upload(AppData.getUploadLogDirectory(),
                                                        file.getAbsolutePath(), SftpClient.getChannelSftp());
                                            }
                                            mes = DataUtil.hexStringToBytes("31");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("upload current time log succeed.");
                                        }else{
                                            mes = DataUtil.hexStringToBytes("30");
                                            SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                            MyLogger.info("no current log to upload.");
                                        }
                                    } else {
                                        mes = DataUtil.hexStringToBytes("30");
                                        SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                        MyLogger.info("cannot connect to server.");
                                    }
                                }catch (Exception e){
                                    byte[] mes = DataUtil.hexStringToBytes("30");
                                    SendData.sendResponse(message_seq,message_id,command_id,time,len,mes);
                                    MyLogger.error(e.toString());
                                }finally {
                                    SftpClient.getInStance().stopSftpConnect();
                                }
                            }
                        });
                        break;
                    case 0x0D:   //上传流量
                        try {
                            float traffic = MobileInfo.getDataBytesSinceDeviceBoot() / 1024.0f;
                            float value = traffic - SPTool.getInstance().getOldTrafficBytes();
                            SPTool.getInstance().setOldTrafficBytes(traffic);
                            SPTool.getInstance().addCurrentTrafficBytes(value);
                            SPTool.getInstance().save(SPTool.TRAFFIC_STATS_BYTES,
                                    SPTool.getInstance().getCurrentTrafficBytes());
                            MyLogger.info("当前使用流量："+traffic);
                            MyLogger.info("上一次的流量差："+value);
                            MyLogger.info("总使用流量："+SPTool.getInstance().getCurrentTrafficBytes());
                        }catch (Exception e){
                            MyLogger.error(e.getMessage());
                        }
                        message = String.valueOf(SPTool.getInstance().getCurrentTrafficBytes()).getBytes();
                        len = (byte)message.length;
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                            }
                        });
                        break;
                    case 0x0E:  //后摄像头立即拍照
                        len = 0x01;
                        message = DataUtil.hexStringToBytes("31");
                        Intent intent_photo1 = new Intent(MyApplication.getInstance(), CameraActivity.class);
                        intent_photo1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent_photo1.putExtra(CameraActivity.EXTRA_CAMERA, CameraActivity.CAMERA_BACK);
                        MyApplication.getInstance().startActivity(intent_photo1);

                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                            }
                        });
                        break;
                    case 0x0F:  //单片机升级
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                boolean isConnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(),
                                        AppData.getUsername(), AppData.getPassword());
                                byte[] mes;
                                byte len = 0x01;
                                if(isConnected){
                                    MyLogger.info("开始下载bin文件： ");
                                    String filepath = Environment.getExternalStorageDirectory().getPath() + "/pushApk";
                                    File path = new File(filepath);
                                    if (!path.exists())
                                        path.mkdirs();
                                    Vector<ChannelSftp.LsEntry> files = null;
                                    try {
                                        files = SftpClient.getInStance().listFiles(
                                                AppData.getBinDir(), SftpClient.getChannelSftp());
                                    }catch (SftpException e){
                                        MyLogger.error(e.toString());
                                    }
                                    boolean isLoaded = false;
                                    if(files != null && !files.isEmpty() ){
                                        for(ChannelSftp.LsEntry file : files){
                                            if(file.getFilename().endsWith(".bin")) {
                                                isLoaded = SftpClient.getInStance().download(AppData.getBinDir(),
                                                        file.getFilename(), filepath +"/"+ file.getFilename(),
                                                        SftpClient.getChannelSftp());
                                            }
                                        }
                                    }

                                    if(isLoaded){
                                        mes = DataUtil.hexStringToBytes("31");
                                        SendData.sendResponse(AppData.getMessage_seq(),AppData.getMessage_id(),AppData.getCommand_id()
                                                ,AppData.getMessage_time(),len,mes);
                                        MyApplication.getInstance().sendBroadcast(new Intent(AppLogService.OBD_UPGRADE_ACTION));
                                    }else{
                                        mes = DataUtil.hexStringToBytes("30");
                                        SendData.sendResponse(AppData.getMessage_seq(),AppData.getMessage_id(),AppData.getCommand_id(),
                                                AppData.getMessage_time(),len,mes);
                                    }
                                }else{
                                    mes = DataUtil.hexStringToBytes("30");
                                    SendData.sendResponse(AppData.getMessage_seq(),AppData.getMessage_id(),AppData.getCommand_id()
                                            ,AppData.getMessage_time(),len,mes);
                                }
                            }
                        });
                        break;


                    case 0x7e: //服务器设置
                        len = (byte) 0x01;
                        message = DataUtil.hexStringToBytes("31");
                        //向旧服务器发送成功
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.sendResponse(message_seq,message_id,command_id,time,len,message);
                            }
                        });
                        //设置新服务器ip,port
                        try {
                            byte[] serverIpAndPort = serverCommand.getMessage();
                            String ipAndPort = new String(serverIpAndPort);
                            MyLogger.info("commandBody: " + ipAndPort);
                            int index = ipAndPort.indexOf(":");
                            String ip = ipAndPort.substring(0,index);
                            String portStr = ipAndPort.substring(index+1,ipAndPort.length());
                            int port = Integer.valueOf(portStr);
                            AppData.setIP(ip);
                            AppData.setPORT(port);
                            SPTool.getInstance().save(SPTool.SERVER_ADDRESS, ip);
                            SPTool.getInstance().save(SPTool.SERVER_PORT, portStr);
                        }catch (Exception e){
                            MyLogger.error(e.getMessage());
                        }
                        //向下继续运行，往新服务器发送hello包
                    case 0x7f: //服务器重置连接
//                        len = (byte) 0x01;
//                        message = DataUtil.hexStringToBytes("30");
                        ThreadPools.getInstance().getSingleThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                SendData.setIsConnected(false);
                                while(!SendData.getIsConnected()) {
                                    try {
                                        SendData.sendDeviceHello(DeviceHello.MACHINE_TYPE,DeviceHello.CUSTOMER,DeviceHello.SIM_OPERATOR
                                                , AppData.getIccid(),AppData.getScmVersionCode());
                                        AppData.addSendHelloTimes();
                                        if( AppData.getSendHelloTimes() < 12) {
                                            Thread.sleep(5 * AppData.getSendHelloTimes() * 1000);
                                        }else{
                                            Thread.sleep(60 * 1000);
                                        }
                                    } catch (Exception exce) {
                                        exce.printStackTrace();
                                        MyLogger.error(exce.toString());
                                    }
                                }
                            }
                        });
                        break;
                }
                break;
        }

    }
}
