//package com.devicemanagement.receiver;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import com.devicemanagement.data.AppData;
//import com.devicemanagement.data.Seq;
//import com.devicemanagement.MyApplication;
//import com.devicemanagement.ProviderData.socData;
//import com.devicemanagement.RtmpVideoActivity;
//import com.devicemanagement.util.Tools;
//import com.devicemanagement.devicePacket.clientPacket.ClientWarning;
//import com.devicemanagement.devicePacket.main.SendData;
//import com.devicemanagement.h264.VideoActivity;
//
//import org.apache.log4j.Logger;
//
///**
// * Created by Administrator on 2018/1/24 0024.
// *
// */
//public class VideoReceiver extends BroadcastReceiver {
//
//    private Logger logger = Logger.getLogger("VideoReceiver");
//    public static final String START_RTMP_ACTION = "START_RTMP_ACTION";
//    public static final String STOP_RTMP_ACTION = "STOP_RTMP_ACTION";
//    public static final String TAKE_PICTURE_FRONT = "TAKE_PICTURE_FRONT";
//    public static final String TAKE_PICTURE_BACK = "TAKE_PICTURE_BACK";
//    public static final String LOW_VOLT_WARNING = "LOW_VOLT_WARNING";
//    public static final String DEVICE_POW_OFF_WARNING = "DEVICE_POW_OFF_WARNING";
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        if (action.equals(START_RTMP_ACTION)) {
//            if(Tools.isForeground(MyApplication.getContext(),"VideoActivity")){
//                logger.info("video has started");
//                return;
//            }
//            Intent rtmpVedioIntent = new Intent(MyApplication.getInstance(), VideoActivity.class);
//            String imeiStr = String.valueOf(AppData.getImei());
//            String imeiSuffix = imeiStr.substring(10, 15);   //403201738101762
////            String rtmlUrl = "rtmp://59.41.210.162/live/" + imeiSuffix;
////            logger.info(rtmlUrl);
//            rtmpVedioIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////            rtmpVedioIntent.putExtra(RtmpVideoActivity.RTMPURL_MESSAGE, rtmlUrl);
//            rtmpVedioIntent.putExtra(RtmpVideoActivity.FILENAME, imeiSuffix);
////            rtmpVedioIntent.putExtra(RtmpVideoActivity.IS_START_RTMP, false);
//            MyApplication.getInstance().startActivity(rtmpVedioIntent);
//        }else if(action.equals(STOP_RTMP_ACTION)){
////            MyApplication.getContext().sendBroadcast(new Intent(RtmpVideoActivity.RtmpReceiver.STOP_RTMP_ACTION));
//            if(VideoActivity.getInstance() != null) {
//                VideoActivity.getInstance().finish();
//            }
//        }else if(action.equals(TAKE_PICTURE_FRONT)){
//            MyApplication.getContext().sendBroadcast(new Intent(RtmpVideoActivity.RtmpReceiver.TAKE_PICTURE_FRONT));
//        }else if(action.equals(TAKE_PICTURE_BACK)){
//            MyApplication.getContext().sendBroadcast(new Intent(RtmpVideoActivity.RtmpReceiver.TAKE_PICTURE_BACK));
//        }
//        if(action.equals(LOW_VOLT_WARNING)){
//            ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
//                @Override
//                public void run() {
//                    SendData.sendWarning(ClientWarning.BATTERY_VOLT_LOW,System.currentTimeMillis(),
//                            Seq.getWarnSeq(), socData.getLat(), socData.getLon());
//                }
//            });
//        }else if(action.equals(DEVICE_POW_OFF_WARNING)){
//            ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
//                @Override
//                public void run() {
//                    SendData.sendWarning(ClientWarning.DEVICE_POW_OFF,System.currentTimeMillis(),
//                            Seq.getWarnSeq(), socData.getLat(), socData.getLon());
//                }
//            });
//        }
//    }
//}
