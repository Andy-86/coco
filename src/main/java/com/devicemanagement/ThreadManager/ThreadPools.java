package com.devicemanagement.ThreadManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Administrator on 2017/7/16 0016.
 * @author lg
 */
public class ThreadPools {
    /**
     * 单例线程池，用来发送ClientResponce包和ClientHello包
     */
    private  ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    /**固定线程池*/
    private ExecutorService fixedThreadPool = Executors.newCachedThreadPool();

    /**
     * 定时执行线程池，
     * 1、风扇监控
     * 2、3种心跳包
     * 3、定时获取车身状态
     */
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);

    /**
     * ThreadPool 唯一实例
     */
    private static ThreadPools threadPools = null;

    /**
     * 获取ThreadPool 唯一实例
     * @return
     */
    public static ThreadPools getInstance(){
        if(threadPools == null){
            threadPools = new ThreadPools();
        }
        return threadPools;
    }

    /**
     * 私有构造函数
     */
    private ThreadPools(){}

    public ExecutorService getSingleThreadPool(){return singleThreadPool;}
    public ExecutorService getFixedThreadPool(){return fixedThreadPool;}
    public ScheduledExecutorService getScheduledThreadPool(){return this.scheduledThreadPool;}

}
