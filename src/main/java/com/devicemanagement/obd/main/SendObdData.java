package com.devicemanagement.obd.main;

import android.content.Context;
import android.os.PowerManager;


import com.devicemanagement.MyApplication;
import com.devicemanagement.ThreadManager.ScheduledFutureManager;
import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.face.OBDEventIble;
import com.devicemanagement.util.DataUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android_serialport_api.SerialPort;

/**
 * Created by Abner on 2017/5/10.
 *
 */

public class SendObdData {
    private Thread obdSendThread;
    private Thread obdReceiveThead;
    private InputStream is;
    private OutputStream os;
    //OBD串口
    private SerialPort mSerialPort;

    private boolean isPortConnected = false;

    private static boolean isObdUpgrade = false;


    /**声明为volatile，a*/
    private volatile byte[] sendCommand = null;

    private static SendObdData sendObdData;

    /**阻塞队列，同一时间只接收一条指令*/
    private BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(1);


    // TODO: 2017/5/12  加上连接成功，失败标志

    public static SendObdData getInstance()
    {
        if (sendObdData==null)
        {
            sendObdData = new SendObdData();
        }
        return sendObdData;
    }
    private SendObdData(){}

    /**
     * 开始终端与串口之间的消息传递，启动发送线程和接收线程  STOP ONLINE “STALKERS”  BLOCK MALICIOUS SOFTWARE
     */
    public void StartObd() {
        obdSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                enablePort();
                MyLogger.info(isPortConnected + " in obdSendThread");
                if(!isPortConnected){   //如果串口打不开，则重启，再次一定可以打得开。
                    try {
                        Thread.sleep(3 * 1000);
                        PowerManager pManager=(PowerManager) MyApplication.getContext().getSystemService(Context.POWER_SERVICE); //重启到fastboot模式
                        pManager.reboot("");
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                        MyLogger.error(ioe.toString());
                    }
                }
                while (isPortConnected) {
                    try {
                        if(os!=null){
                            isSendCommand();
                            queue.offer(sendCommand,5 * 1000,TimeUnit.MILLISECONDS);
                            MyLogger.info("write:" + DataUtil.getHexString(sendCommand));
                            os.write(sendCommand);
                            os.flush();
                            sendCommand = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyLogger.error(e.toString());
                    }
                }
            }
        });
        obdReceiveThead = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    MyLogger.info(isPortConnected + " in obdReceiveThread");
                    List<Byte> listByte = new ArrayList<>(30);
                    byte[] b = new byte[1];
                    while (isPortConnected) {
                        MyLogger.info("Receive");
                        while (is!=null && is.read(b) != -1) {   //当无数据接收时，线程阻塞
                            listByte.add(b[0]);
                            //将obd返回的消息按一次一个字节接收，当最后两个字节是"\r"和"\n"时，停止接收
                            if (listByte.size() >= 8 && listByte.get(listByte.size() - 1) == '\n' && listByte.get(listByte.size() - 2) == '\r') {
                                //Message message = Message.obtain();
                                byte[] bytes = new byte[listByte.size()];
                                for(int i=0; i<bytes.length;i++){
                                    bytes[i] = listByte.get(i);
                                }
//                                MyApplication.getInstance().sendBroadcast(new Intent(ACTION_OBD_BYTE_RECEIVE).putExtra(
//                                        EXTRA_OBD_BYTE_DATA,bytes));
                                dataHandle(bytes);
                                listByte.clear();
                                //queue.take();
                                if(AppData.getIsReceiveCommand() && !isObdUpgrade) {
                                    queue.poll(5 * 1000, TimeUnit.MILLISECONDS);
                                }

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MyLogger.error(e.toString());
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        });

        obdSendThread.start();
        obdReceiveThead.start();
    }

    private void dataHandle(final byte[] bytes){
        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //数据按字节接收
                try {
                    //接收的数据大于等于8字节才是正确的
                    if (bytes.length >= 8) {
                        //数据分发处理
                        OBDEventIble obdEventIble = OBDEventDispatch.dispatch(bytes);
                        if (obdEventIble != null) {
                            obdEventIble.dispose(bytes);
                        }
                    } else if (AppData.isReceiveSerCommand()) {   //若接收到服务器下发的指令，并且接收obd数据出错
                        MyLogger.info("obd 返回信息错误");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    MyLogger.error(e.toString());
                }
            }
        });
    }


    /**
     * 打开串口
     * @return isPortConnected
     */
    public boolean enablePort() {
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyMT2"), 38400, 0);
            is = mSerialPort.getInputStream();
            os = mSerialPort.getOutputStream();
            if(is != null && os != null)
                isPortConnected = true;
            else
                isPortConnected = false;
        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.error(e.toString());
            isPortConnected = false;
        } finally {
            return isPortConnected;
        }
    }

    public void sendCommand(final byte[] bytes){
//        try{
//            queue.offer(sendCommand,5 * 1000,TimeUnit.MILLISECONDS);
//            MyLogger.info("write: " + DataUtil.getHexString(bytes));
//            os.write(bytes);
//            os.flush();
//        }catch (Exception e){
//            MyLogger.error(e.toString());
//        }
        SetSendCommand(bytes);
        isSendCommand();
    }

    /**
     * 设置指令，利用长度为一的阻塞队列
     * @param command
     */
    public synchronized void SetSendCommand(byte[] command){
        sendCommand = command;
//        isSendCommand();
//        insertObdDataToQueue(command);
    }

    /**判断指令，当sendCommand==""时，obd发送线程阻塞 |  当sendCommand是实际指令时，唤醒obd发送线程*/
    public synchronized void isSendCommand(){
        try{
            while(sendCommand == null)
                wait();
            notify();
        }catch (Exception e){
            MyLogger.error(e.toString());
        }
//        isObdQueuEmpty();
    }

    private Queue<String> obdQueue = new LinkedList<>();
    private Lock obdQueueLock = new ReentrantLock();

    public void insertObdDataToQueue(String command){
        obdQueueLock.lock();
        if(obdQueue.size() > 1){
            obdQueue.clear();
        }
        obdQueue.offer(command);
        obdQueueLock.unlock();
    }

    public void isObdQueuEmpty(){
        try{
            while(obdQueue.isEmpty())
                wait();
            notify();
        }catch (Exception e){
            MyLogger.error(e.toString());
        }
    }

//    public void getAndReleaseQueueData(){
//        String obd;
//        obdQueueLock.lock();
//        obd = obdQueue.poll();
//        obdQueueLock.unlock();
//        try {
//            queue.offer(obd, 5000, TimeUnit.MILLISECONDS);
//            MyLogger.info("write:" + obd);
//            os.write(obd.getBytes());
//            os.flush();
//        } catch (Exception e) {
//            MyLogger.error(e.toString());
//        }
//    }

    /**
     * 停止连接串口
     */
    public void stopOBD()
    {
        try {
            if(mSerialPort!=null) {
                mSerialPort.close();
            }
            mSerialPort = null;
            if(is != null) {
                is.close();
            }
            if(os != null) {
                os.close();
            }
            is = null;
            os = null;
            isPortConnected=false;
            if(ScheduledFutureManager.getCarStateFuture() != null) {
                ScheduledFutureManager.getCarStateFuture().cancel(true);
                ScheduledFutureManager.setCarStateFuture(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.error(e.toString());
        }
    }

    public boolean IsPortConnected(){return isPortConnected;}

    public static boolean isObdUpgrade() {
        return isObdUpgrade;
    }

    public static void setObdUpgrade(boolean obdUpgrade) {
        isObdUpgrade = obdUpgrade;
    }

    public BlockingQueue<byte[]> getQueue(){return this.queue;}

    public static final String EXTRA_OBD_BYTE_DATA = "extra_cobo_obd_byte_data";
    public static final String ACTION_OBD_BYTE_RECEIVE = "com.imdroid.cobo.ACTION_OBD";

    //判读当前是否有命令没有执行完
    public boolean isObdAvailable(){
        //可用的
        if(sendCommand==null){
            return true;
        }else {
            return false;
        }
    }

}
