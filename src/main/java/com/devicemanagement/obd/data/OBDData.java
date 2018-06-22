package com.devicemanagement.obd.data;

/**
 * Created by Abner on 2017/5/10.
 *
 */

public class OBDData {
    /**
     * 液体温度 0~100
     */
    private static byte liquidTemperature = 35;

    /**
     * 水位过高标志
     */
    private static boolean waterLevelHigh = false;

    /**
     * 水位过低标志
     */
    private static boolean waterLevelLow = true;

    /**
     * 油位过高标志
     */
    private static boolean oilLevelHigh = false;

    /**
     * 1号抽水电机工作状态
     */
    private static boolean pumper1Work = false;

    /**
     * 2号抽水电机工作状态
     */
    private static boolean pumper2Work = false;

    /**
     * 排油电机工作状态
     */
    private static boolean oilDrainPumpWork = false;

    /**
     * 加热电丝工作状态
     */
    private static boolean heatingWireWork = false;


    /**
     * 油重
     */
    private static float oilWeight = 0;

    /**
     * 按键值
     */
    private static int pressKeyValue = 1;

    //故障信息

    /**
     * 1号抽水电机是否故障
     */
    private static boolean pumper1Error = false;

    /**
     * 2号抽水电机是否故障
     */
    private static boolean pumper2Error = false;

    /**
     * 排油电机是否故障
     */
    private static boolean oilDrainPumpError = false;

    /**
     * 加热电丝是否故障
     */
    private static boolean heatingWireError = false;

    /**
     * 电子秤是否故障
     */
    private static boolean elecScaleError = false;

    //单片机主动上报数据
    private static byte[] pressValue = new byte[2];

    private static byte[] errorInfo = new byte[2];

    private static byte[] status = new byte[2];

    public static byte[] getErrorInfo() {
        return errorInfo;
    }

    public static void setErrorInfo(byte[] errorInfo) {
        OBDData.errorInfo = errorInfo;
    }

    public static byte[] getPressValue() {
        return pressValue;
    }

    public static void setPressValue(byte[] pressValue) {
        OBDData.pressValue = pressValue;
    }

    public static byte[] getStatus() {
        return status;
    }

    public static void setStatus(byte[] status) {
        OBDData.status = status;
    }

    public static boolean isElecScaleError() {
        return elecScaleError;
    }

    public static void setElecScaleError(boolean elecScaleError) {
        OBDData.elecScaleError = elecScaleError;
    }

    public static byte getLiquidTemperature() {
        return liquidTemperature;
    }

    public static void setLiquidTemperature(byte liquidTemperature) {
        OBDData.liquidTemperature = liquidTemperature;
    }

    public static boolean isWaterLevelHigh() {
        return waterLevelHigh;
    }

    public static void setWaterLevelHigh(boolean waterLevelHigh) {
        OBDData.waterLevelHigh = waterLevelHigh;
    }

    public static boolean isWaterLevelLow() {
        return waterLevelLow;
    }

    public static void setWaterLevelLow(boolean waterLevelLow) {
        OBDData.waterLevelLow = waterLevelLow;
    }

    public static boolean isOilLevelHigh() {
        return oilLevelHigh;
    }

    public static void setOilLevelHigh(boolean oilLevelHigh) {
        OBDData.oilLevelHigh = oilLevelHigh;
    }

    public static boolean isPumper1Work() {
        return pumper1Work;
    }

    public static void setPumper1Work(boolean pumper1Work) {
        OBDData.pumper1Work = pumper1Work;
    }

    public static boolean isPumper2Work() {
        return pumper2Work;
    }

    public static void setPumper2Work(boolean pumper2Work) {
        OBDData.pumper2Work = pumper2Work;
    }

    public static boolean isOilDrainPumpWork() {
        return oilDrainPumpWork;
    }

    public static void setOilDrainPumpWork(boolean oilDrainPumpWork) {
        OBDData.oilDrainPumpWork = oilDrainPumpWork;
    }

    public static boolean isHeatingWireWork() {
        return heatingWireWork;
    }

    public static void setHeatingWireWork(boolean heatingWireWork) {
        OBDData.heatingWireWork = heatingWireWork;
    }

    public static float getOilWeight() {
        return oilWeight;
    }

    public static void setOilWeight(float oilWeight) {
        OBDData.oilWeight = oilWeight;
    }

    public static int getPressKeyValue() {
        return pressKeyValue;
    }

    public static void setPressKeyValue(int pressKeyValue) {
        OBDData.pressKeyValue = pressKeyValue;
    }

    public static boolean isPumper1Error() {
        return pumper1Error;
    }

    public static void setPumper1Error(boolean pumper1Error) {
        OBDData.pumper1Error = pumper1Error;
    }

    public static boolean isPumper2Error() {
        return pumper2Error;
    }

    public static void setPumper2Error(boolean pumper2Error) {
        OBDData.pumper2Error = pumper2Error;
    }

    public static boolean isOilDrainPumpError() {
        return oilDrainPumpError;
    }

    public static void setOilDrainPumpError(boolean oilDrainPumpError) {
        OBDData.oilDrainPumpError = oilDrainPumpError;
    }

    public static boolean isHeatingWireError() {
        return heatingWireError;
    }

    public static void setHeatingWireError(boolean heatingWireError) {
        OBDData.heatingWireError = heatingWireError;
    }


    //排油日期
    private static int[] oilDrainDays = {1,2,3,4,5,6,7};

    //排油时间
    private static int oilDrainTime = 23;


    public static int[] getOilDrainDays() {
        return oilDrainDays;
    }

    public static void setOilDrainDays(int[] oilDrainDays) {
        OBDData.oilDrainDays = oilDrainDays;
    }

    public static int getOilDrainTime() {
        return oilDrainTime;
    }

    public static void setOilDrainTime(int oilDrainTime) {
        OBDData.oilDrainTime = oilDrainTime;
    }

}
