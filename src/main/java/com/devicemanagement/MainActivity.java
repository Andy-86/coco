package com.devicemanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devicemanagement.data.AppData;
import com.devicemanagement.database.SPTool;
import com.devicemanagement.log.MyLogger;
import com.devicemanagement.obd.main.SendObdData;
import com.devicemanagement.phoneState.GetGPS;
import com.devicemanagement.phoneState.MobileInfo;
import com.devicemanagement.phoneState.PhoneInfoUtils;
import com.devicemanagement.phoneState.SimSignalState;
import com.devicemanagement.service.AppLogService;
import com.devicemanagement.service.DeviceManagerService;
import com.devicemanagement.service.ObdService;
import com.devicemanagement.service.TcpNioClient;
import com.devicemanagement.util.BcdUtil;
import com.devicemanagement.util.ParseXml;

import org.apache.log4j.Logger;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public  Context mContext;
//    public static long IMEI = 111111111111111L;

    private Intent intent_device = null;
    private Intent intent_upgrade_timer = null;

    private TextView textView;
    private Button upgrade, upload, takephoto, comfirm;
    private EditText ipText;
    private String ip;

    private SimSignalState simSignalState;

    /**日志文件*/
    private Logger logger = Logger.getLogger("MainActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        logger.info("IMEI:"+ AppData.getImei());
        logger.info("iccid:"+ BcdUtil.bcd2Str(AppData.getIccid()));
        //sharedPre 文件
        initSharedPreferences();

        initView();
        AppData.setRomVersion(PhoneInfoUtils.getSystemProperty(PhoneInfoUtils.ROM_FILEPATH));
        //初始化provider
        AppData.setResolver(this.getContentResolver());

        GetGPS.getInstance(mContext);
        //4G信号监听
        simSignalState = new SimSignalState(MainActivity.this);

        intent_device = new Intent(MainActivity.this, DeviceManagerService.class);
        mContext.startService(intent_device);

        intent_upgrade_timer = new Intent(MainActivity.this, AppLogService.class);
        mContext.startService(intent_upgrade_timer);

        Intent intent_odb = new Intent(MainActivity.this, ObdService.class);
        mContext.startService(intent_odb);
        //测试can信息
        Button button=(Button)findViewById(R.id.send_cans);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                            TcpNioClient.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
            }
        });
    }


    private void initSharedPreferences(){
        try {
            SPTool.getInstance(MainActivity.this);
            float traffic = SPTool.getInstance().read(SPTool.TRAFFIC_STATS_BYTES);
            SPTool.getInstance().setCurrentTrafficBytes(traffic);
            float tra = MobileInfo.getDataBytesSinceDeviceBoot() / 1024.0f;
            SPTool.getInstance().setOldTrafficBytes(tra);
            AppData.setIP(SPTool.getInstance().readString(SPTool.SERVER_ADDRESS));
            AppData.setPORT(Integer.valueOf(SPTool.getInstance().readString(SPTool.SERVER_PORT)));
            logger.info("ip: " + AppData.getIP() +", port: "+ AppData.getPORT());
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    private void initView(){
        upgrade = (Button) findViewById(R.id.upgrade);
        upgrade.setOnClickListener(this);

        upload = (Button) findViewById(R.id.uploadLog);
        upload.setOnClickListener(this);

        takephoto = (Button) findViewById(R.id.takephoto);
        takephoto.setOnClickListener(this);

        comfirm = (Button) findViewById(R.id.confirm);
        comfirm.setOnClickListener(this);

        textView = (TextView) findViewById(R.id.textview);
        int versionCode = ParseXml.getVersionCode();
        String text = "the versionCode is " + versionCode;
        textView.setText(text);

        ipText = (EditText) findViewById(R.id.ip_text);
        ipText.setFocusableInTouchMode(true);
        ipText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ip = ipText.getText().toString();
                //Log.i(TAG,imei);
            }
        });
    }



    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.upgrade:
                Toast.makeText(mContext,"发送断电预警信息",Toast.LENGTH_SHORT).show();
                SendObdData.getInstance().sendCommand("BBB".getBytes());
                break;
            case R.id.uploadLog:
                Toast.makeText(mContext,"发送电压不足预警信息",Toast.LENGTH_SHORT).show();
                break;
            case R.id.takephoto:
                break;
            case R.id.confirm:
                try {
                    SPTool.getInstance().save(SPTool.SERVER_ADDRESS, ip);
                    Toast.makeText(mContext,"修改成功，请重启程序",Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    logger.error(e);
                }
                break;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        logger.info("onDestroy()回调");
        mContext.stopService(intent_upgrade_timer);
        mContext.stopService(intent_device);
        simSignalState.removeListener();
        simSignalState = null;
        try {
            GetGPS.getInstance().unregisterListener();
        }catch (Exception e){
            MyLogger.error(e.toString());
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }


}
