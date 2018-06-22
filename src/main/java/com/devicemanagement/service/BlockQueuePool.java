package com.devicemanagement.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockQueuePool {
    //发送can指令的阻塞队列
    public static BlockingQueue<ByteTaker> queue=new ArrayBlockingQueue<ByteTaker>(10);
}
