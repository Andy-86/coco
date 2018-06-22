package com.devicemanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Administrator on 2017/7/6 0006.
 *
 */
public class BootReceiver extends BroadcastReceiver {
    //private MediaPlayer mediaPlayer = null;
    private final String packageName = "com.devicemanagement";
    private final String packageName2="com.example.mac.cobo2";
    //private final String GPSTEST = "com.chartcross.gpstestplus";
    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")|| Intent.ACTION_REBOOT.equals(intent.getAction())) {     // boot
            try {
                Intent newintent = new Intent(context,MainActivity.class);
                context.startActivity(newintent);
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                Intent intentpackage = context.getPackageManager().getLaunchIntentForPackage(packageName);
                context.startActivity(intentpackage);
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}
