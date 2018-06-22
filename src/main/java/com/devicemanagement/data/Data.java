package com.devicemanagement.data;


/**
 * 墨武士数据
 * @author Life
 *
 */
public class Data {
	
	/**
	 * gsensor的x轴数据
	 */
	private static double gsensor_x = 0;
	
	/**
	 * gsensor的y轴数据
	 */
	private static double gsensor_y = 0 ;
	
	/**
	 * gsensor的z轴数据
	 */
	private static double gsensor_z = 0;
	
	/**
	 * cpu温度
	 */
	private static double cpuTemp = 0;
	
	/**
	 * 串口1返回的数据
	 */
	private static short Soc = 0;
	
	/**
	 * 串口2返回的数据
	 */
	private static short Odo = 0;
	
	/**
	 * 串口4返回的数据
	 */
	private static byte SocStatus= 0x30;
	
	/**
	 * 电压
	 */
	private static short volt = 0;

	/**
	 * 当前时间
	 */
	private static long time;

	private static byte memory = 0 ;
	private static byte cpu_usage = 0;
	private static short sdMemory = 0;
	private static byte bootOnHours = 0;
	private static byte satelliteNumbers = 0;
	private static byte signal = 0 ;

	public static double getGsensor_x() {
		return gsensor_x;
	}

	public static void setGsensor_x(double g_x) {
		gsensor_x = g_x;
	}

	public static double getGsensor_y() {
		return gsensor_y;
	}

	public static void setGsensor_y(double g_y) {
		gsensor_y = g_y;
	}

	public static double getGsensor_z() {
		return gsensor_z;
	}

	public static void setGsensor_z(double g_z) {
		gsensor_z = g_z;
	}

	public static double getCpuTemp() {
		return cpuTemp;
	}

	public static void setCpuTemp(double cpuT) {
		cpuTemp = cpuT;
	}

	public static short getSoc() {
		return Soc;
	}

	public static void setSoc(short soc) {
		Soc = soc;
	}

	public static short getOdo() {
		return Odo;
	}

	public static void setOdo(short odo) {
		Odo = odo;
	}


	public static byte getSocStatus() {
		return SocStatus;
	}
	public static void setSocStatus(byte socStatus) {
		SocStatus = socStatus;
	}

	public static short getVolt() {
		return volt;
	}
	public static void setVolt(short vt) {
		volt = vt;
	}

	public static void setMemory(byte me){ memory = me;}
	public static byte getMemory(){return memory;}

	public static void setCpu_usage(byte usage){ cpu_usage = usage;}
	public static byte getCpu_usage(){return cpu_usage;}

	public static void setSdMemory(short me){ sdMemory = me;}
	public static short getSdMemory(){return sdMemory;}

	public static void setBootOnHours(byte hours){bootOnHours = hours;}
	public static byte getBootOnHours(){return bootOnHours;}

	public static void setSatelliteNumbers(int num){ satelliteNumbers = (byte)num;}
	public static byte getSatelliteNumbers(){return satelliteNumbers;}

	public static void setSignal(int sig){signal = (byte)sig;}
	public static byte getSignal(){return signal;}


	public static long getTime() {
		//time=new Date();
		return time;
	}

	public static void setTime(long t) {
		time = t;
	}


}
