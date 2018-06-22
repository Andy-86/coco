package com.devicemanagement;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devicemanagement.ThreadManager.ThreadPools;
import com.devicemanagement.data.AppData;
import com.devicemanagement.util.SftpClient;
import com.devicemanagement.data.MLocation;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        View.OnClickListener, Camera.PictureCallback {
    private SurfaceView mSurfaceView;
    private ImageView mIvStart;
    private TextView mTvCountDown;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean mIsSurfaceCreated = false;

    public static final int CAMERA_BACK = 0; //后置摄像头
    public static final int CAMERA_FRONT = 1; //前置摄像头

    public static final String EXTRA_CAMERA = "CAMERA";
    private int cameraStr = 1;

    private final static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd_HH_mm_ss", Locale.CHINA);

    private static String dateStr;

    private Logger logger = Logger.getLogger("CameraActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        final Intent intent = getIntent();
        cameraStr = intent.getIntExtra(EXTRA_CAMERA, CAMERA_BACK);
        logger.info("start to take photo.");
        initView();
        initEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.info("onPause()");
        stopPreview();
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        //mIvStart = (ImageView) findViewById(R.id.start);
        mTvCountDown = (TextView) findViewById(R.id.count_down);
    }

    private void initEvent() {
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        //mIvStart.setOnClickListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsSurfaceCreated = true;
        logger.info("1. surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        logger.info( "2. surfaceChanged");
        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    startPreview();
                    Thread.sleep(2000);
                    if(mCamera != null) {
                        mCamera.takePicture(null, null, null, CameraActivity.this);
                    }
                }catch (Exception e){
                    logger.error(e.toString());
                }

            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsSurfaceCreated = false;
    }

    private void startPreview() throws Exception {
        if (mCamera != null || !mIsSurfaceCreated) {
            logger.info("3. startPreview will return");
            return;
        }
        logger.info( "4. startPreview");
        try {
            mCamera = Camera.open(cameraStr);
        }catch (Exception e){
            logger.error(e.toString());
        }
        if(mCamera == null){
            logger.info("5. open camera which can use.");
            mCamera = Camera.open();
        }
        if(mCamera != null) {
            //logger.info("infomation of Camera object: " + mCamera.toString());
            Camera.Parameters parameters = mCamera.getParameters();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            Camera.Size size = getBestPreviewSize(width, height, parameters);
            if (size != null) {
                //设置预览分辨率
                parameters.setPreviewSize(size.width, size.height);
                //设置保存图片的大小
                parameters.setPictureSize(size.width, size.height);
            }

            //自动对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setPreviewFrameRate(20);

            //设置相机预览方向
            mCamera.setDisplayOrientation(90);

            mCamera.setParameters(parameters);

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }

            mCamera.startPreview();
        }else{
            logger.info("cannot open Camera.");
            CameraActivity.this.finish();
        }
    }

    /**  打开相机设备 */
    private Camera openCamera(int cameraId) {
        try{
            return Camera.open(cameraId);
        }catch(Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }



    private void stopPreview() {
        //释放Camera对象
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                logger.info( e.getMessage());
            }
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            Date date = new Date();
            dateStr = sdf.format(date);
            StringBuffer pictureStr = new StringBuffer();
            pictureStr.append(Environment.getExternalStorageDirectory());
            pictureStr.append(File.separator);
            pictureStr.append("Pictures/");
            pictureStr.append(AppData.getImei());
            pictureStr.append("-");
            pictureStr.append(dateStr);
            pictureStr.append("-");
            pictureStr.append(MLocation.getLat());
            pictureStr.append("-");
            pictureStr.append(MLocation.getLon());
            pictureStr.append(".jpeg");
            String filename = pictureStr.toString();

            File picture = new File(filename);
            FileOutputStream fos = new FileOutputStream(picture);

            //旋转角度，保证保存的图片方向是对的
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(180);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            //压缩图片
//            NativeUtil.compressBitmap(bitmap,filename);
            //upload pictures
            uploadPicuresByFilePath(filename);
            //uploadPicturesByHttp(filename);
            CameraActivity.this.finish();
        }  catch (Exception e) {
            logger.error(e.getMessage());
        }
        logger.info("save pictures");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        logger.info("onDestroy()回调");
        stopPreview();
    }

    /**
     * upload the picture by
     * @param filePath the picture's path which need to be uploaded.
     */
    private void uploadPicuresByFilePath(final String filePath){
        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(500);

                    boolean isconnected = SftpClient.InitChannelSftp(AppData.getUpgrade_ip(), AppData.getUpgrade_port(),
                            AppData.getUsername(), AppData.getPassword());
                    if(isconnected){
                        SftpClient.getInStance().upload(picture_directory,filePath,
                                SftpClient.getChannelSftp());
                        logger.info("upload picture succeed.");
                    }else{
                        logger.error("cannot connect to server");
                    }
                }catch (Exception e){
                    logger.error(e.getMessage());
                }finally {
                    try{
                        SftpClient.getInStance().stopSftpConnect();
                        //CameraActivity.this.finish();
                    }catch (Exception e){
                        logger.error(e.getMessage());
                    }
                }
            }
        });
    }

    private static String picture_directory = "/home/mowushi/Pictures";
    //private static final String URL = "";

    private void uploadPicturesByHttp(final String filePath){
        ThreadPools.getInstance().getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(500);
//                    File file = new File(filePath);
//                    Map<String, File> files = new HashMap<>();
//                    files.put("files", file);
//                    Map<String, String> p = new HashMap<>();
//                    p.put("deviceSn", MainActivity.IMEI+"");
//                    p.put("time", dateStr);
//                    String result = UploadUtil.doFilePost(URL,p,files);
//                    logger.info("upload picture: "+result);
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        });
    }

    /**  imdroid图片分辨率为 640 * 480 */
}
