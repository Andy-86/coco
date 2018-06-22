package com.devicemanagement.phoneState;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.devicemanagement.MyApplication;
import com.devicemanagement.data.Data;
import com.devicemanagement.MainActivity;

import java.lang.ref.WeakReference;


/**
 * Created by Administrator on 2017/7/14 0014.
 *
 */
public class SimSignalState {
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private TelephonyManager tm;

    public int getHasSingal() {
        return hasSingal;
    }

    public void setHasSingal(int hasSingal) {
        this.hasSingal = hasSingal;
    }

    public int hasSingal = 1;

    public int dbm = 0;
    public int asu = 0;

    WeakReference<MainActivity> mActivity;

    public SimSignalState(MainActivity demoActivity) {
        mActivity = new WeakReference<MainActivity>(demoActivity);
        getCurrentNetDBM(mActivity.get());
    }

//    public static void getSimSignal(Context context){
//        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        //NETWORK_TYPE_UNKNOWN
//
//        phoneStateListener = new PhoneStateListener(){
//            @Override
//            public void onSignalStrengthsChanged(SignalStrength signalStrength){
//                super.onSignalStrengthsChanged(signalStrength);
//                //设置通信信号强度
//                Status.setCommunicationSignal(signalStrength.getGsmSignalStrength());
//                hasSingal=signalStrength.getGsmSignalStrength();
//                setHasSingal(hasSingal);
////                Log.d("dddd", "信号强度: "+hasSingal);
//            }
//        };
//
//        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
//    }

    /**
     * 得到当前的手机蜂窝网络信号强度
     * 获取LTE网络和3G/2G网络的信号强度的方式有一点不同，
     * LTE网络强度是通过解析字符串获取的，
     * 4G网络信号强度：
     * asu 与 dbm 之间的换算关系是 dbm=-140 + asu
     * 3G/2G网络信号强度是通过API接口函数完成的。
     * asu 与 dbm 之间的换算关系是 dbm=-113 + 2*asu
     *
     */
    public void getCurrentNetDBM(Context context) {

        tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String signalInfo = signalStrength.toString();
                String[] params = signalInfo.split(" ");

                if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G网络 最佳范围   >-90dBm 越大越好
                    dbm = Integer.parseInt(params[9]);
//                    Status.setCommunicationSignal(dbm);
                    Data.setSignal(Math.abs(dbm));

                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {
                    //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                    //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
                    int OperatorsName = 0;
                    if (ActivityCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    String IMSI = tm.getSubscriberId();
                    // IMSI号前面3位460是国家，紧接着后面2位00 运营商代码
                    if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
                        OperatorsName = 1;
                    } else if (IMSI.startsWith("46001") || IMSI.startsWith("46006")) {
                        OperatorsName = 2;
                    } else if (IMSI.startsWith("46003") || IMSI.startsWith("46005")) {
                        OperatorsName = 3;
                    }
                    if (OperatorsName==1) {
                        //中国移动3G不可获取，故在此返回0
                    }else if (OperatorsName==2) {
                        dbm = signalStrength.getCdmaDbm();
//                        Status.setCommunicationSignal(dbm);
                        Data.setSignal(Math.abs(dbm));
                    }else if (OperatorsName==3) {
                        dbm = signalStrength.getEvdoDbm();
//                        Status.setCommunicationSignal(dbm);
                        Data.setSignal(Math.abs(dbm));
                    }

                }else{
                    //2G网络最佳范围>-90dBm 越大越好
                    asu = signalStrength.getGsmSignalStrength();
                    dbm = -113 + 2 * asu;
//                    Status.setCommunicationSignal(dbm);
                    Data.setSignal(Math.abs(dbm));
                }

            }
        };
        //开始监听
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public  void removeListener(){
        if(tm != null){
            tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

}
