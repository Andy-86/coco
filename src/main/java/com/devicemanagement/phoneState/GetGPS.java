package com.devicemanagement.phoneState;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import com.devicemanagement.MyApplication;
import com.devicemanagement.data.MLocation;
import com.devicemanagement.log.MyLogger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by Administrator on 2017/6/30 0030.
 *
 */
public class GetGPS {

    private short speed = 0;

    private Context mcontext;
    private static Location currentBestLocation = null;
    //    private String currentProvider = LocationManager.NETWORK_PROVIDER;
    private LocationManager locationManager;

    public short getSpeed() {
        return speed;
    }

    private static GetGPS gps = null;

    private GetGPS(Context context) {
        this.mcontext = context;
        locationManager = (LocationManager) mcontext.getSystemService(Context.LOCATION_SERVICE);
        if (latList == null && lonList == null) {
            latList = new CopyOnWriteArrayList<>();
            lonList = new CopyOnWriteArrayList<>();
        }
        getGps();
    }

    public static void getInstance(Context context) {
        if (gps == null) {
            gps = new GetGPS(context);
        }
    }

    public static GetGPS getInstance() {
        return gps;
    }


    private List<Float> latList = null;
    private List<Float> lonList = null;

    public List<Float> getLatList() {
        return latList;
    }

    public List<Float> getLonList() {
        return lonList;
    }

    private long waitTime = System.currentTimeMillis();

    public void setWaitTime(long time) {
        this.waitTime = time;
    }

    public long getWaitTime() {
        return this.waitTime;
    }

    private boolean currentFlag = false;
    private boolean isClosed = false;
    private boolean isFirstLoc = true;

    private void getGps() {
        try {
            //监听卫星个数
            MyLogger.info("init GPS.");
            if (ActivityCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.addGpsStatusListener(listener);
            Location location;
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                MyLogger.info("use network for location");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                updateView(location);
//                Status.setGPSState(true);

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                MyLogger.info("use GPS for location");
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateView(location);
//                Status.setGPSState(true);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            }
        } catch (Exception e) {
            MyLogger.error(e.toString());
        }
    }

    private LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch(status){
                case LocationProvider.AVAILABLE:
//                    Status.setGPSSignal(2);
                    //MyLogger.info("available");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
//                    Status.setGPSSignal(0);
                    MyLogger.info("out_of_service");
                    //开始使用netWork监听GPS
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    MyLogger.info("TEMPORARILY_UNAVAILABLE");
//                    registerListenerByProvider(LocationManager.GPS_PROVIDER);
//                    Status.setGPSSignal(1);
                    break;
            }
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            if (ActivityCompat.checkSelfPermission(mcontext,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mcontext,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
//            Status.setGPSState(true);
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
//            Status.setGPSState(false);
            locationManager.removeGpsStatusListener(listener);
            if(locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            try {
                updateView(location);
            }catch (Exception e){
                MyLogger.error(e.toString());
            }
        }
    };

    private GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前状态
                    try{
                        if (ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                        // 获取卫星颗数的默认最大值
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        // 创建一个迭代器保存所有卫星
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                                .iterator();
                        int count = 0;
                        while (iters.hasNext() && count <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            if (s.usedInFix())//只有信躁比不为0的时候才算搜到了星
                            {
                                count++;
                            }
                        }
                        //设置卫星个数
                    }catch (Exception e){
                        MyLogger.error(e.toString());
                    }
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    //Log.i(TAG, "定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    //Log.i(TAG, "定位结束");
                    break;
            }
        }
    };

    public static final long DELAY_MINUTES = 3 * 60 * 1000L;
    public static final long CLOSE_GPS_TIME = 30 * 60 * 1000L;
    /**
     * 设置GPS坐标，方向，速度，海拔
     * @param location
     */
    private void updateView(Location location) throws Exception{
        if (location != null && location.getAccuracy() != 0) {
            boolean flag;
            speed = (short) location.getSpeed();
            MLocation.setSpeed((short) location.getSpeed());
            MLocation.setLat((float) location.getLatitude());
            MLocation.setLon((float) location.getLongitude());
            MLocation.setDirection((short) location.getBearing());
            MLocation.setAlt((short) location.getAltitude());
        }
    }


    public void unregisterListener() throws Exception{
        if(locationManager != null){
            if(listener != null){
                locationManager.removeGpsStatusListener(listener);
            }
            if(locationListener != null){
                locationManager.removeUpdates(locationListener);
            }
        }
        gps = null;
        locationManager = null;
        currentFlag = false;
        currentBestLocation = null;
        latList.clear();
        lonList.clear();
        latList = null;
        lonList = null;
    }

    /**
     * 强制帮用户打开GPS
     * @param context
     */
    public static void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */
    public static boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
//            Status.setGPSState(true);
            return true;
        }

        return false;
    }


    private static final long TWO_MINUTES = 1000 * 60 * 2;
    /**
     * Determines whether one MLocation reading is better than the current
     * MLocation fix
     *
     * @param location
     *            The new MLocation that you want to evaluate
     * @param currentBestLocation
     *            The current MLocation fix, to which you want to compare the new
     *            one
     */
    protected boolean isBetterLocation(Location location,
                                       Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private boolean isBetterLocationOfAccuracy(Location location,
                                               Location currentBestLocation){
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        Log.e("GetGPS",accuracyDelta +", list len: " + latList.size());
        // Determine location quality using accuracy
        if (isMoreAccurate) {
            return true;
        } else if (!isLessAccurate) {
            return true;
        }
        return false;
    }

    private boolean isBestLocation(Location location,
                                   Location currentBestLocation){

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of distance and accuracy
        if(isFromSameProvider){
            Log.e("GetGPS",accuracyDelta +", list len: " + latList.size());
            return isMoreAccurate;
        }else{
            Log.e("GetGPS","not same provider: "+accuracyDelta);
            float[] result = new float[1];
            Location.distanceBetween(currentBestLocation.getLatitude(),
                    currentBestLocation.getLongitude(), location.getLatitude(), location.getLongitude(), result);
            if(result[0] > location.getAccuracy())
                return false;
            else
                return isMoreAccurate || !isLessAccurate;
        }
    }
}
