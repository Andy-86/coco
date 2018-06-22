package com.devicemanagement.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.devicemanagement.data.AppData;
import com.devicemanagement.util.SftpClient;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.orhanobut.logger.Logger;

//import com.isc.wang.AbsApp;

@SuppressLint("SimpleDateFormat")
public class CashReportSender implements ReportSender {
	/**
	 * @param folder
	 *            the location folder
	 */
	public CashReportSender(String folder) {
//		this.app = app;
		this.folder = folder;

	}

	private static final String TAG = "CashReportSender";
	//private static Logger gLogger = Logger.getLogger("Crash");
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.acra.sender.ReportSender#send(org.acra.collector.CrashReportData)
	 */
	//@NonNull()
	public void send(Context context, CrashReportData arg0) throws ReportSenderException {
		Log.i(TAG,this.getClass().getSimpleName()+"---"+
				arg0.getProperty(ReportField.STACK_TRACE));
		String errorFileName = ""+ AppData.getImei()+"_" + sdf.format(new Date()) + ".txt";
		File file = new File(folder + errorFileName);
		BufferedWriter output = null;
		try {
			File path = new File(folder);

			if (!path.exists())
				path.mkdirs();

			if (!file.exists())
				file.createNewFile();

			output = new BufferedWriter(new FileWriter(file));
			output.write(String.valueOf(AppData.getImei()));
			output.write("[ANDROID_VERSION]:"
					+ arg0.getProperty(ReportField.ANDROID_VERSION) + "\n");
			output.write("[APP_VERSION_CODE]:"
					+ arg0.getProperty(ReportField.APP_VERSION_CODE) + "\n");
			output.write("[APP_VERSION_NAME]:"
					+ arg0.getProperty(ReportField.APP_VERSION_NAME) + "\n");
			output.write("[APPLICATION_LOG]:"
					+ arg0.getProperty(ReportField.APPLICATION_LOG) + "\n");
			output.write("[AVAILABLE_MEM_SIZE]:"
					+ arg0.getProperty(ReportField.AVAILABLE_MEM_SIZE) + "\n");
//			output.write("[BRAND]:" + arg0.getProperty(ReportField.BRAND)
//					+ "\n");
//			output.write("[BUILD]:" + arg0.getProperty(ReportField.BUILD)
//					+ "\n");
//			output.write("[CRASH_CONFIGURATION]:"
//					+ arg0.getProperty(ReportField.CRASH_CONFIGURATION) + "\n");
//			output.write("[CUSTOM_DATA]:"
//					+ arg0.getProperty(ReportField.CUSTOM_DATA) + "\n");
//			output.write("[DEVICE_FEATURES]:"
//					+ arg0.getProperty(ReportField.DEVICE_FEATURES) + "\n");
//			output.write("[DEVICE_ID]:"
//					+ arg0.getProperty(ReportField.DEVICE_ID) + "\n");
//			output.write("[DISPLAY]:" + arg0.getProperty(ReportField.DISPLAY)
//					+ "\n");
//			output.write("[DROPBOX]:" + arg0.getProperty(ReportField.DROPBOX)
//					+ "\n");
//			output.write("[DUMPSYS_MEMINFO]:"
//					+ arg0.getProperty(ReportField.DUMPSYS_MEMINFO) + "\n");
//			output.write("[ENVIRONMENT]:"
//					+ arg0.getProperty(ReportField.ENVIRONMENT) + "\n");
//			output.write("[EVENTSLOG]:"
//					+ arg0.getProperty(ReportField.EVENTSLOG) + "\n");
//			output.write("[FILE_PATH]:"
//					+ arg0.getProperty(ReportField.FILE_PATH) + "\n");
//			output.write("[INITIAL_CONFIGURATION]:"
//					+ arg0.getProperty(ReportField.INITIAL_CONFIGURATION)
//					+ "\n");
//			output.write("[INSTALLATION_ID]:"
//					+ arg0.getProperty(ReportField.INSTALLATION_ID) + "\n");
//			output.write("[IS_SILENT]:"
//					+ arg0.getProperty(ReportField.IS_SILENT) + "\n");
//			output.write("[LOGCAT]:" + arg0.getProperty(ReportField.LOGCAT)
//					+ "\n");
//			output.write("[MEDIA_CODEC_LIST]:"
//					+ arg0.getProperty(ReportField.MEDIA_CODEC_LIST) + "\n");
//			output.write("[PACKAGE_NAME]:"
//					+ arg0.getProperty(ReportField.PACKAGE_NAME) + "\n");
//			output.write("[PHONE_MODEL]:"
//					+ arg0.getProperty(ReportField.PHONE_MODEL) + "\n");
//			output.write("[PRODUCT]:" + arg0.getProperty(ReportField.PRODUCT)
//					+ "\n");
//			output.write("[RADIOLOG]:" + arg0.getProperty(ReportField.RADIOLOG)
//					+ "\n");
//			output.write("[REPORT_ID]:"
//					+ arg0.getProperty(ReportField.REPORT_ID) + "\n");
//			output.write("[SETTINGS_SECURE]:"
//					+ arg0.getProperty(ReportField.SETTINGS_SECURE) + "\n");
//			output.write("[SETTINGS_SYSTEM]:"
//					+ arg0.getProperty(ReportField.SETTINGS_SYSTEM) + "\n");
			output.write("[SHARED_PREFERENCES]:"
					+ arg0.getProperty(ReportField.SHARED_PREFERENCES) + "\n");
			output.write("[STACK_TRACE]:"
					+ arg0.getProperty(ReportField.STACK_TRACE) + "\n");
			output.write("[THREAD_DETAILS]:"
					+ arg0.getProperty(ReportField.THREAD_DETAILS) + "\n");
			output.write("[TOTAL_MEM_SIZE]:"
					+ arg0.getProperty(ReportField.TOTAL_MEM_SIZE) + "\n");
			output.flush();
			output.close();
			SftpClient.InitChannelSftp(IP,PORT,USERNAME,PASSWORD);
			SftpClient.getInStance().upload(DIRECTORY,file.getAbsolutePath(), SftpClient.getChannelSftp());
			SftpClient.getInStance().stopSftpConnect();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null)
					output.close();
				file.delete();
//				android.os.Process.killProcess(android.os.Process.myPid()); //退出程序
//				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// private final BaseApp app;
	// private final String ftpAddress;
	// private final int ftpPort;
	// private final String ftpAccount;
	// private final String ftpPassword;
	private final String folder;
	private final static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd_HH_mm_ss");

	//self variable
	private final String DIRECTORY = "/home/mowushi/deviceErrorLog";
	private final String IP = "59.41.210.162";
	private final int PORT = 22;
	private final String USERNAME = "mowushi";
	private final String PASSWORD = "F01Imdroid29832611MoWuShi";
}
