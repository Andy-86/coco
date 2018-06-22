package com.devicemanagement.service;

import android.util.Log;

import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.main.SendObdData;
import com.devicemanagement.util.AnalysisUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by mac on 2018/5/24.
 */

public class TcpNioClient {
    public static final  String TAG="TCPClient";
    //通道管理器
    private static Selector selector;
    private static SocketChannel finalchanl;
    /**
     * 获得一个Socket通道，并对该通道做一些初始化的工作
     * @param ip 连接的服务器的ip
     * @param port  连接的服务器的端口号
     * @throws IOException
     */
    public void initClient(String ip,int port)throws IOException {
        // 获得一个Socket通道
        SocketChannel channel = SocketChannel.open();
        // 设置通道为非阻塞
        channel.configureBlocking(false);
        // 获得一个通道管理器
        this.selector = Selector.open();
        // 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调
        //用channel.finishConnect();才能完成连接
        channel.connect(new InetSocketAddress(ip,port));
        //将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
        channel.register(selector, SelectionKey.OP_CONNECT);

    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     * @throws IOException
     */
    public void listen()throws IOException {
        // 轮询访问selector
        while (true) {
            // 选择一组可以进行I/O操作的事件，放在selector中,客户端的该方法不会阻塞，
            //这里和服务端的方法不一样，查看api注释可以知道，当至少一个通道被选中时，
            //selector的wakeup方法被调用，方法返回，而对于客户端来说，通道一直是被选中的
            selector.select();
            // 获得selector中选中的项的迭代器
            Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = (SelectionKey)iter.next();
                // 删除已选的key,以防重复处理
                iter.remove();
                // 连接事件发生
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel)key
                            .channel();
                    // 如果正在连接，则完成连接
                    finalchanl=channel;
                    if(channel.isConnectionPending()){
                        channel.finishConnect();
                    }
                    // 设置成非阻塞
                    channel.configureBlocking(false);
                    //在这里可以给服务端发送信息哦
//                    channel.write(ByteBuffer.wrap(new String("向服务端发送了一条信息").getBytes()));
                    //在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
                    channel.register(this.selector, SelectionKey.OP_READ);
                    // 获得了可读的事件
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    /**
     * 处理读取服务端发来的信息 的事件
     * @param key
     * @throws IOException
     * @throws InterruptedException
     */

    public void read(SelectionKey key)throws IOException{
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel)key.channel();
        // 创建读取的缓冲区
        finalchanl=channel;
        ByteBuffer buffer = ByteBuffer.allocate(12*10+1);

        if(channel.read(buffer)==-1){
            Log.d(TAG, "read: 断开连接");
            channel.close();
            return;
        }

        byte[] data =buffer.array();
        String msg = new String(data).trim();
        Log.d(TAG, "read: "+new Date().toString() +" 客户端收到信息："+byteArrayToHexStr(data));
        int index=data[0];
        if(index>10||index<0)
            return;

        byte[] command=new byte[12*index+1];
        System.arraycopy(data,0,command,0,12*index+1);
        sendOBD(command);
//        ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
//        channel.write(outBuffer);// 将消息回送给客户端
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    /**
     * 启动客户端测试
     * @throws IOException
     */
    public static void start() throws IOException {
        TcpNioClient client = new TcpNioClient();
        startGetCansThread();
        SendCanThread sendCanThread=new SendCanThread();
        sendCanThread.start();
        client.initClient("43.228.126.16",8899);
        client.listen();
    }

    public String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void sendOBD(byte[] data){
            byte[] command=new byte[data.length+2];
            System.arraycopy("BB".getBytes(),0,command,0,2);
            System.arraycopy(data,0,command,2,data.length);
            MyLogger.info(""+command);
            Log.d(TAG, "sendOBD: "+"开始发送命令"+ AnalysisUtil.byteArrayToHexStr(command));
            SendObdData.getInstance().sendCommand(command);
    }

    public static Selector getSelector() {
        return selector;
    }

    public static void startGetCansThread(){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(25000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //发送命令让单片机发送can信息
                    if(SendObdData.getInstance().isObdAvailable()) {
                        SendObdData.getInstance().sendCommand("CCC".getBytes());
                        Log.d(TAG, "发送请求can信息: ");
                    }

//                    byte[] arr = {(byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 1};
//                    try {
//                        BlockQueuePool.queue.put(new ByteTaker(arr));
//                        Log.d(TAG, "put: " + arr);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }


                }
            }
        });
        thread.start();
    }

    public static SocketChannel getfinalchanl(){
        return finalchanl;
    }
}
