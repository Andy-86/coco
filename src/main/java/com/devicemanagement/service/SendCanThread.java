package com.devicemanagement.service;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;

import static com.devicemanagement.util.AnalysisUtil.byteArrayToHexStr;

/**
 * Created by mac on 2018/6/5.
 */

public class SendCanThread extends Thread {
    public static final String TAG="SendCans";
    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                byte[] bytes = BlockQueuePool.queue.take().getBytes();
                try {
                    if(TcpNioClient.getSelector()!=null){
                        Log.d(TAG, "run: "+byteArrayToHexStr(bytes));
                        broadcastToServer(TcpNioClient.getfinalchanl(), bytes);
                    }else {
                        continue;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 广播给服务端
     *
     * @param channel
     * @param content
     * @throws IOException
     */
    public void broadcastToServer(SocketChannel channel, byte[] content)
            throws IOException {


            Channel targetchannel =channel;
            if (targetchannel instanceof SocketChannel) {
                SocketChannel dest = (SocketChannel) targetchannel;
                dest.write(ByteBuffer.wrap(content));
                System.out.println(" send"+byteArrayToHexStr(content));
            }



    }
}
