package com.devicemanagement.util;


public class CPUUtil {
	public static double getCPUTemperature() {		
		return NodeUtil.getValue(NodeUtil.NODE_CPU_TEMPERATURE) / 1000.0;
	}
}
