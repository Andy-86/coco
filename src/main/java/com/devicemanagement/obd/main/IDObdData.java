//package com.devicemanagement.obd.main;
//
//
//import com.devicemanagement.log.MyLogger;
//import com.devicemanagement.obd.data.Config;
//import com.devicemanagement.util.DataUtil;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import android_serialport_api.SerialPort;
//
///**
// * Created by Administrator on 2018-03-29.
// *
// */
//public class IDObdData  {
//
//    private static IDObdData idObdData = null;
//
//    private Thread IDReceiveThread = null;
//
//    //串口数据
//    private SerialPort mSerialPort;
//    private InputStream is;
//    private OutputStream os;
//    private boolean isPortConnected;
//
//    //是否有检测到刷卡(检测到刷卡后会持续20秒)
//    private boolean isCardDetected = false;
//
//    //卡号信息
//    private String IDNumber;
//    private volatile byte[] IDNumberByte;
//    public String getIDNumber(){return this.IDNumber;}
//    public byte[] getIDNumberByte(){return this.IDNumberByte;}
//
//    //控制ID模块供电
//    public static final byte[] OPEN_ID_MOUDLE = {0x52,0x52,0x01};
//    public static final byte[] CLOSE_ID_MOUDLE = {0x52,0x00};
//
//    public static IDObdData getInstance(){
//        if (idObdData==null) {
//            synchronized (IDObdData.class){
//                if (idObdData == null){
//                    idObdData = new IDObdData();
//                }
//            }
//        }
//        return idObdData;
//    }
//
//    private IDObdData(){
//
//    }
//
//
//    /**
//     * 打开串口
//     * @return isPortConnected
//     */
//    private boolean enablePort() {
//        try {
//            mSerialPort = new SerialPort(new File("/dev/ttyMT2"), 115200, 0);
//            is = mSerialPort.getInputStream();
//            os = mSerialPort.getOutputStream();
//            if(is != null && os != null)
//                isPortConnected = true;
//            else
//                isPortConnected = false;
//        } catch (IOException e) {
//            e.printStackTrace();
//            MyLogger.error(e.toString());
//            isPortConnected = false;
//        } finally {
//            return isPortConnected;
//        }
//    }
//
//    public synchronized boolean sendMessageToIDModule(byte[] bytes){
//        if(os != null && isPortConnected){
//            try{
//                os.write(bytes);
//                os.flush();
//                MyLogger.info("command: "+ DataUtil.getHexString(bytes));
//                return true;
//            }catch (Exception e){
//                MyLogger.error(e.toString());
//                return false;
//            }
//        }else{
//            MyLogger.info("the outputstream isn't opened.");
//            return false;
//        }
//    }
//
//    public void startReceiveData(){
//        if(IDReceiveThread == null) {
//            IDReceiveThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        enablePort();
//                        MyLogger.info(isPortConnected + " in IDReceiveThread");
//                        List<Byte> listByte = new ArrayList<>(30);
//                        byte[] b = new byte[1];
//                        while (isPortConnected) {
//                            MyLogger.info("Receive");
//                            while (is != null && is.read(b) != -1) {   //当无数据接收时，线程阻塞
//                                listByte.add(b[0]);
//                                if (listByte.size() >= 8 && listByte.get(listByte.size() - 1) == '\n' &&
//                                        listByte.get(listByte.size() - 2) == '\r') {
//                                    byte[] bytes = new byte[listByte.size()];
//                                    for (int i = 0; i < bytes.length; i++) {
//                                        bytes[i] = listByte.get(i);
//                                    }
//                                    //解析卡号
//                                    analysisData(bytes);
//                                    listByte.clear();
//                                }
//                            }
//
//                        }
//                    } catch (Exception e) {
//                        MyLogger.error(e.toString());
//                        isPortConnected = false;
//                        IDReceiveThread = null;
//                    }
//                }
//            });
//            IDReceiveThread.start();
//        }
//    }
//
//    private void analysisData(byte[] bytes){
////        MyLogger.info("id number: "+ DataUtil.getHexString(bytes));
//        try {
//            if (bytes[2] == 'N') {
//                byte[] idByte;
//                if (bytes[4] == 0x00) {
////                    MyLogger.info("无卡号数据");
//                    isCardDetected = false;
//                } else if (bytes[4] == 0x04) {
//                    isCardDetected = true;
//                    idByte = new byte[4];
//                    System.arraycopy(bytes, 4, idByte, 0, 4);
//                    IDNumberByte = idByte;
//                    MyLogger.info("4: " + DataUtil.getHexString(IDNumberByte));
//                    if(!isCardDetected){
//                        byte[] key = new byte[3];
//                        System.arraycopy(Config.OBD_OPERATION_MACHINE.getBytes(),0,key,0,2);
//                        key[2] = 0x01;
//                        SendObdData.getInstance().sendCommand(key);
//                    }
//                } else if (bytes[4] == 0x08) {
//                    isCardDetected = true;
//                    idByte = new byte[8];
//                    System.arraycopy(bytes, 4, idByte, 0, 8);
//                    IDNumberByte = idByte;
//                    MyLogger.info("8: " + DataUtil.getHexString(IDNumberByte));
//                    if(!isCardDetected){
//                        byte[] key = new byte[3];
//                        System.arraycopy(Config.OBD_OPERATION_MACHINE.getBytes(),0,key,0,2);
//                        key[2] = 0x01;
//                        SendObdData.getInstance().sendCommand(key);
//                    }
//                }
//            }
//        }catch (Exception e){
//            MyLogger.error(e.toString());
//        }
//    }
//
//
//
//    public void stopPort() throws Exception {
//        if(mSerialPort != null){
//            mSerialPort.close();
//            mSerialPort = null;
//        }
//        if(is != null && os != null){
//            is.close();
//            os.close();
//            is = null;
//            os = null;
//        }
//        isPortConnected = false;
//    }
//
//
//}
