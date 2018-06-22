package com.devicemanagement.modedetection;

/**
 * shock  值的计算方式： 将变化的部分取出，x,y,z值都扩大alpha = 100倍，
 * 然后取其平方和的开方 -> sqrt(x*x + y*y + z*z) * 100
 */

public interface OnBigShockListener {
	void onBigShock();
	float getSensitivity();
	long getLastShockTime();
	long getShockMinInterval();
}
