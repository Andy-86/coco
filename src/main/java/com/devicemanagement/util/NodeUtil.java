package com.devicemanagement.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class NodeUtil {
	
	public static int getValue(String node) {
		FileInputStream fis = null;
		try {
			File f = new File(node);
			if(!f.exists())
				return -1;
			fis = new FileInputStream(f);
			byte[] buf = new byte[16];
			int len = fis.read(buf);
			if(len > 0) {
				return Integer.parseInt(new String(buf, 0, len).trim());
			}
		} catch(Exception e){
			Log.e("Node Util", "Node read error", e);
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch(Exception e){}
			}
		}
		return -1;
	}
	
	public static void writeValue(String node, int value) {
		FileOutputStream fos = null;
		try {
			File f = new File(node);
			if(!f.exists())
				return;
			fos = new FileOutputStream(f);
			fos.write(String.valueOf(value).getBytes());
			fos.flush();
		} catch(Exception e){
			Log.e("Node Util", "Node write error", e);
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch(Exception e) {}
			}
		}
	}
	
	public static final String NODE_ATV_SMALL_BOARD = "/sys/bus/platform/drivers/zw_btsound/extboard";
	
	public static final String NODE_ATV_SIGNAL = "/sys/bus/platform/drivers/zw_btsound/avin";
	
	public static final String NODE_EXPOSURE = "/sys/bus/platform/drivers/zw_btsound/camshut";
	
	public static final String NODE_CPU_TEMPERATURE = "/sys/class/thermal/thermal_zone1/temp";

	public static final String NODE_AUX = "/sys/bus/platform/drivers/zw_btsound/aux";
	
	public static final String NODE_SCREEN = "/sys/bus/platform/drivers/zw_btsound/sleep";
}
