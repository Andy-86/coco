package com.devicemanagement;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.devicemanagement.data.AppData;
import com.devicemanagement.phoneState.PhoneInfoUtils;
import com.devicemanagement.util.BcdUtil;
import com.devicemanagement.log.CashReportSender;
import com.devicemanagement.log.ConfiguratorLog;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;
import org.apache.log4j.Logger;
/**
 * Created by Administrator on 2017/7/11 0011.
 *
 */
@ReportsCrashes(
        applicationLogFile = "applog.log",
        applicationLogFileLines = 200,
        logcatArguments = { "-t", "100", "-v", "long", "ActivityManager:I", "MyApp:D", "*:S" }
//        formUri = "http://192.168.104.239",
//        mode = ReportingInteractionMode.TOAST,
//        resToastText = R.string.crash_toast_text
)
public class MyApplication extends Application {

    private static Logger logger = Logger.getLogger("MyApplication");

    private static Context mcontext;
    private static MyApplication myApplication;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
    @Override
    public void onCreate(){
        logger.info("MyApplication"+"onCreate()回调");
        myApplication = this;
        ACRA.init(this);
        String root = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        String error = root + "/WERA/Error/";

        ErrorReporter reporter = ACRA.getErrorReporter();

        reporter.removeAllReportSenders();
        reporter.addReportSender(new CashReportSender(error));
        super.onCreate();
        mcontext = getApplicationContext();

        PhoneInfoUtils phone = new PhoneInfoUtils(mcontext);
        String imei = phone.getImei();
        if(imei != null) {
            AppData.setImei(Long.valueOf(imei));
        }
        String iccidStr = phone.getIccid();
        try {
            if (iccidStr != null) {
                byte[] iccid = null;
                iccid = BcdUtil.str2Bcd(iccidStr, 10);
                AppData.setIccid(iccid);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //配置日志文件
        ConfiguratorLog.configure(AppData.getImei());

    }

    public static Context getInstance(){
        return mcontext;
    }

    public static Context getContext() {
        return mcontext;
    }

}
