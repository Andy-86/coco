package com.devicemanagement.modedetection;

import android.app.Application;

/**
 * Created by 83625 on 2016/11/6.
 *
 */
public class SdkModedetection {

    private static Application application;

    public static void initSdk(Application application) {
        SdkModedetection.application = application;
    }

    public static Application getApp() {
        return application;
    }


}
