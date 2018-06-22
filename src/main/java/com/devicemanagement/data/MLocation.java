package com.devicemanagement.data;

/**
 * 位置信息
 * @author Life
 *
 */
public class MLocation {
	@Override
	public String toString() {
		return "MLocation{" +
				"GPSstate=" + GPSstate +
				", lat=" + lat +
				", lon=" + lon +
				", direction=" + direction +
				", speed=" + speed +
				", alt=" + alt +
				'}';
	}

	/**
	 * GPS关闭状态，可用于本类设置GPS状态
	 */
	public static final byte GPSCLOSE = 0;
	
	/**
	 * 使用GPS48坐标系，可用于本类设置GPS状态
	 */
	public static final byte GPS48 = 1;
	
	/**
	 * 使用高德地图坐标系，可用于本类设置GPS状态
	 */
	public static final byte GPSAMAP = 2;
	
	/**
	 * 使用百度地图坐标系，可用于本类设置GPS状态
	 */
	public static final byte GPSBAIDU = 3;
	
	/**
	 * 使用BD1坐标系，可用于本类设置GPS状态
	 */
	public static final byte BD1 = 4;
	
	/**
	 * 使用BD2坐标系，可用于本类设置GPS状态
	 */
	public static final byte BD2 = 5;
	
	/**
	 * 使用GNSS坐标系，可用于本类设置GPS状态
	 */
	public static final byte GNSS = 6;
	
	/**
	 * 使用基站定位，可用于本类设置GPS状态
	 */
	public static final byte NETWORK = 7;
	
	/**
	 * 当前GPS状态
	 */
	private static byte GPSstate=0x02;
	
	/**
	 * 维度
	 */
	private static float lat=0.0f;
	
	/**
	 * 经度
	 */
	private static float lon=0.0f;
	
	/**
	 * 方向
	 */
	private static short direction=0;
	
	/**
	 * 速度
	 */
	private static short speed=0;
	
	/**
	 * 海拔高度
	 */
	private static short alt=100;

	/**
	 * gps卫星可用个数
     */
	private static byte gpsInuse = 0;

	public static byte getGpsInuse() {
		return gpsInuse;
	}

	public static void setGpsInuse(byte gpsInuse) {
		MLocation.gpsInuse = gpsInuse;
	}


	public static byte getGPSState() {
		return GPSstate;
	}

	public static void setGPSState(byte Gpstate) {
		GPSstate = Gpstate;
	}

	public static float getLat() {
		return lat;
	}

	public static void setLat(float l) {
		lat = l;
	}

	public static float getLon() {
		return lon;
	}

	public static void setLon(float l) {
		lon = l;
	}

	public static short getDirection() {
		return direction;
	}

	public static void setDirection(short direct) {
		direction = direct;
	}

	public static short getSpeed() {
		return speed;
	}

	public static void setSpeed(short sp) {
		speed = sp;
	}

	public static short getAlt() {
		return alt;
	}

	public static void setAlt(short al) {
		alt = al;
	}
}
