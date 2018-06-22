package com.devicemanagement.database;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.devicemanagement.data.AppData;

import java.lang.ref.WeakReference;


/**
 * Created by Administrator on 2017/11/10 0010.
 *
 */
public class SPTool {

    private WeakReference<Activity> mActivity;
    private SharedPreferences sp;
    private static SPTool spTool = null;

    public static final String TRAFFIC_STATS_FILE = "TRAFFIC_STATS_FILE";
    public static final String TRAFFIC_STATS_BYTES = "TRAFFIC_STATS_BYTES";
    public static final String SERVER_ADDRESS = "SERVER_ADDRESS";
    public static final String SERVER_PORT = "SERVER_PORT";


    private SPTool() {
    }
    public static SPTool getInstance(Activity activity){
        if(spTool==null){
            synchronized (SPTool.class) {
                if(spTool == null) {
                    spTool = new SPTool(activity);
                }
            }
        }
        return spTool;
    }
    private SPTool(Activity activity) {
        this.mActivity = new WeakReference<>(activity);
        sp = mActivity.get().getSharedPreferences(TRAFFIC_STATS_FILE,Context.MODE_PRIVATE);
    }

    public static SPTool getInstance(){
        return spTool;
    }

    /**
     * 根据存储位置和存储内容更改文件
     * @param flag  存储位置
     * @param traffic  存储内容
     * @throws Exception  当存储位置不存在时抛出异常
     */
    public synchronized void save(String flag,float traffic) throws Exception{
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(flag,traffic);
        editor.apply();
    }

    public void save(String flag, String info) throws Exception{
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(flag, info);
        editor.apply();
    }

    public String readString(String flag) throws Exception{
        if(flag.equals(SERVER_ADDRESS)) {
            return sp.getString(flag, AppData.getIP());
        }else if(flag.equals(SERVER_PORT)){
            return sp.getString(flag, String.valueOf(AppData.getPORT()));
        }else{
            return null;
        }
    }

    /**
     * 根据存储位置获得文件中对应的内容
     * @param flag  存储位置
     * @return 返回存储内容
     * @throws Exception  当存储位置不存在时抛出异常
     */
    public synchronized float read(String flag) throws Exception{
        return sp.getFloat(flag,0);
    }

    /** 当前使用流量 */
    public float getCurrentTrafficBytes() {
        return currentTrafficBytes;
    }

    public void addCurrentTrafficBytes(float currentTrafficBytes){
        this.currentTrafficBytes += currentTrafficBytes;
    }

    public void setCurrentTrafficBytes(float currentTrafficBytes) {
        this.currentTrafficBytes = currentTrafficBytes;
    }

    private float currentTrafficBytes = 0.0f;

    private float oldTrafficBytes = 0.0f;

    public float getOldTrafficBytes() {
        return oldTrafficBytes;
    }

    public void setOldTrafficBytes(float oldTrafficBytes) {
        this.oldTrafficBytes = oldTrafficBytes;
    }
}
