package com.devicemanagement.ThreadManager;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by Administrator on 2017/7/20 0020.
 *
 */
public class ScheduledFutureManager {
    /**
     * 定时器的Future类型
     */
    /**Cpu温度定时监控器*/
    public static ScheduledFuture<?> CputimerFuture = null;
    public static ScheduledFuture<?> getCputimerFuture(){return CputimerFuture;}
    public static void setCputimerFuture(ScheduledFuture<?> future){CputimerFuture = future;}

    /**车辆状态信息定时查询*/
    private static ScheduledFuture<?> CarStateFuture;
    public static ScheduledFuture<?> getCarStateFuture(){return CarStateFuture;}
    public static void setCarStateFuture(ScheduledFuture<?> future){CarStateFuture = future;}

    /**HeartbeatFull包定时发送*/
    private static ScheduledFuture<?> HeartbeatFullFuture;
    public static ScheduledFuture<?> getHeartbeatFullFuture(){return HeartbeatFullFuture;}
    public static void setHeartbeatFullFuture(ScheduledFuture<?> fullFuture){HeartbeatFullFuture = fullFuture;}

}
