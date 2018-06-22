package com.devicemanagement.modedetection;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;


import com.devicemanagement.MyApplication;
import com.devicemanagement.data.Data;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModeDetection {

	private static void startHandler() {
		thread = new HandlerThread("detection thread");
		thread.start();
		handler = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MESSAGE_SENSOR_RAW:
						if (engineListeners.size() == 0
								&& shockListeners.size() == 0) {
							break;
						}
						float[] values = (float[]) msg.obj;
						engineStartPhaseOneFilter.offer(values);    //计算变化的部分
						engineStartPhaseTwoFilter.offer(engineStartPhaseOneFilter
								.get());
						shockFilter.offer(engineStartPhaseOneFilter.get());  //抹平强烈变化的部分，保存静态部分
						float val = engineStartPhaseTwoFilter.get();
						float shock = shockFilter.get();
//						TIME_TICKService.setShock(shock);
//                        TIME_TICKService.setVal(val);
						//Log.e("sensor", "H -> " + format.format(engineStartPhaseOneFilter.getVectorValue()) + "AVG -> " + format.format( val) + ", S1 -> " + format.format(shock) + ", RUN -> " + currentMode + ", ECU -> " + currentECUMode);
						//Log.e("sensor", "H -> " + format.format(engineStartPhaseOneFilter.getVectorValue()) + "AVG -> " + format.format(val) + ", S1 -> " + format.format(val) + ", RUN -> " + currentMode + ", ECU -> " + currentECUMode);
						//Log.e("sensor", "H -> " + format.format(engineStartPhaseOneFilter.getVectorValue()) + "AVG -> " + format.format(val) + ", S1 -> " + format.format(val) + ", RUN -> " + currentMode + ", ECU -> " + currentECUMode);
						long time = System.currentTimeMillis();

						if(System.currentTimeMillis() - SYSTEM_START_TIME < 30000) {
							return;
						}

						boolean changed = false;
//					if (currentMode == MODE_OFF && val >= 15) {
//						currentMode = MODE_ON;
//						changed = true;
//					} else if (currentMode == MODE_ON && val < 10) {
//						currentMode = MODE_OFF;
//						changed = true;
//					}

						if (currentMode == MODE_ON && val <= 15) {
							currentMode = MODE_OFF;
							changed = true;
						} else if(currentMode == MODE_OFF && val >= 20) {
							currentMode = MODE_ON;
							changed = true;
						}

						if (changed && engineListeners.size() > 0) {
							for (OnEngineModeChangeListener oemc : engineListeners) {
								oemc.onChange(currentMode);
							}
							lastNotifyEngine = System.currentTimeMillis();
						} else if(System.currentTimeMillis() - lastNotifyEngine > 20000) {
							if(engineListeners.size() > 0) {
								for(OnEngineModeChangeListener oemc : engineListeners) {
									oemc.onChange(currentMode);
								}
							}
							lastNotifyEngine = System.currentTimeMillis();
						}

						if(System.currentTimeMillis() - lastNotifyECU > 20000) {
							if(ecuListeners.size() > 0) {
								for(OnECUModeChangeListener oemc : ecuListeners) {
									oemc.onChange(currentECUMode);
								}
							}
							lastNotifyECU = System.currentTimeMillis();
						}

						if (shockListeners.size() > 0) {
							for (OnBigShockListener obsl : shockListeners) {
								if (obsl.getSensitivity() < shock
										&& time - obsl.getLastShockTime() > obsl
										.getShockMinInterval()) {
									obsl.onBigShock();
								}
							}
						}
						break;
				}
			}
		};
	}

	private static void startDetection() {
		if (!isDetectionRunning()) {
			startHandler();
			manager = (SensorManager) MyApplication.getContext().getSystemService(
					Context.SENSOR_SERVICE);
			manager.registerListener(listener,
					manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.d("modedete","start");
		}
	}
	private static void stopDetection() {
		if (manager != null) {
			manager.unregisterListener(listener);
		}
		manager = null;

		if (thread != null) {
			thread.quit();
		}
		thread = null;

		engineListeners.clear();
	}

	public static boolean isDetectionRunning() {
		return manager != null;
	}

	public static void addOnEngineModeChangeListener(
			OnEngineModeChangeListener listener) {
		engineListeners.add(listener);
		if(!isDetectionRunning()) {
			startDetection();
		}
	}

	public static void removeOnEngineModeChangeListener(
			OnEngineModeChangeListener listener) {
		engineListeners.remove(listener);
		if(engineListeners.size() == 0 && shockListeners.size() == 0) {
			stopDetection();
		}
	}

	public static boolean hasEngineListener(OnEngineModeChangeListener listener) {
		return engineListeners.contains(listener);
	}

	public static void addOnBigShockListener(OnBigShockListener listener) {
		shockListeners.add(listener);
		if(!isDetectionRunning()) {
			startDetection();
		}
	}

	public static void removeOnBigShockListener(OnBigShockListener listener) {
		shockListeners.remove(listener);
		if(engineListeners.size() == 0 && shockListeners.size() == 0) {
			stopDetection();
		}
	}

	public static void addOnECUChangeListener(OnECUModeChangeListener listener) {
		ecuListeners.add(listener);
	}

	public static void removeOnECUChangeListener(OnECUModeChangeListener listener) {
		ecuListeners.remove(listener);
	}

	public static boolean hasShockListener(OnBigShockListener listener) {
		return shockListeners.contains(listener);
	}

	public static int getCurrentMode() {
		return currentMode;
	}

	public static int getCurrentECUMode() {
		return currentECUMode;
	}

	public static void setECUMode(int mode) {
		currentECUMode = mode;
		if(ecuListeners.size() > 0) {
			for(OnECUModeChangeListener listener : ecuListeners) {
				listener.onChange(mode);
			}
		}
		//changeVideoRecordingState();
		lastNotifyECU = System.currentTimeMillis();
	}

	public static void setManualSecurityMode(boolean manual) {
		manualSecurityMode = manual;
		if(manual) {
			lastManual = System.currentTimeMillis();
		} else {
			lastManual = 0;
		}
	}

	public static boolean isManualSecurityMode() {
		return manualSecurityMode;
	}

	public static long getLastManualTime() {
		return lastManual;
	}

	public static final int MODE_ON = 1;

	public static final int MODE_OFF = 2;

	public static final int MESSAGE_SENSOR_RAW = 0;

	private static final long SYSTEM_START_TIME = System.currentTimeMillis();

	private static long lastNotifyECU = 0;
	private static long lastNotifyEngine = 0;

	private static int currentMode = MODE_ON;
	private static int currentECUMode = MODE_OFF;
	private static List<OnEngineModeChangeListener> engineListeners = Collections.synchronizedList(new ArrayList<OnEngineModeChangeListener>());
	private static List<OnBigShockListener> shockListeners = Collections.synchronizedList(new ArrayList<OnBigShockListener>());
	private static List<OnECUModeChangeListener> ecuListeners = Collections.synchronizedList(new ArrayList<OnECUModeChangeListener>());
	private static HighPassFilter engineStartPhaseOneFilter = new HighPassFilter(
			100);
	private static LowPassFilter engineStartPhaseTwoFilter = new LowPassFilter(
			100);
	private static LowPassFilter shockFilter = new LowPassFilter(1);
	private static SensorManager manager;
	public static Handler handler;
	private static HandlerThread thread;
	private static DecimalFormat format = new DecimalFormat("00000.000");

	private static boolean manualSecurityMode = false;
	private static long lastManual = 0;

	/**
	 * 用来判断用户是否进入了前台行车记录
	 */
	public static boolean isClickRecord = false;


	private static SensorEventListener listener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] values = new float[3];
			System.arraycopy(event.values, 0, values, 0, 3);
			Data.setGsensor_x(event.values[0]);
			Data.setGsensor_y(event.values[1]);
			Data.setGsensor_z(event.values[2]);
			try {
				handler.removeMessages(MESSAGE_SENSOR_RAW);
				handler.obtainMessage(MESSAGE_SENSOR_RAW, values)
						.sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				MyApplication.getInstance().sendBroadcast(new Intent(ACTION_SENSOR_VALUE).putExtra(EXTRA_SENSOR_VALUE, values));
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	public static final String ACTION_SENSOR_VALUE = "ACTION.com.droid.modetection";

	public static final String EXTRA_SENSOR_VALUE = "extra_sensor_value";
}
