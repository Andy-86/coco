package com.devicemanagement.obd.data;

/**
 * Created by Abner on 2017/5/10.
 *
 */

public class Config {
    //obd发送指令，文档有

    //共用
    /** 按键操作公共板 */
    public static final String OBD_OPERATION_MACHINE = "KK";

    /** 控制1号抽水电机启停 */
    public static final String OBD_OPERATION_PUMPER_1 = "AA";

    /** 控制2号抽水电机启停 */
    public static final String OBD_OPERATION_PUMPER_2 = "BB";

    /** 控制排油电机启停 */
    public static final String OBD_OPERATION_OILDRAINPUMP = "CC";

    /** 控制加热电丝启停 */
    public static final String OBD_OPERATION_HEATINGWIRE = "DD";

    /** 设置加热温度的阀值 */
    public static final String OBD_HEATING_TEMPE_VALUE = "TT";

    /** 时间校准*/
    public static final String OBD_TIME_ADJUST = "II";

    /** 清空所有故障信息*/
    public static final String OBD_CLEAR_ERROR_INFO = "EEE";

    /** 数据上报频率 */
    public static final String OBD_CHANGE_INTEVAL = "XX";

    /** 排油间隔 */
    public static final String OBD_OILDRAIN_INTEVAL = "RR";

    /** 油重阀值 */
    public static final String OBD_OIL_WEIGHT_COMMAND = "YY";

    /** 灯光信息 */
    public static final String OBD_LIGHT_COMMAND = "dd";


    public static final byte[] OBD_START_OIL_DRAIN = {0x4B,0x4B,0x01};
    public static final byte[] OBD_STOP_OIL_DRAIN = {0x4B,0x4B,0x00};



    //upgrade
    public static final String OBD_VERSION = "000";
    public static final String OBD_START_TRANS_FILE = "77";
    public static final String OBD_STOP_TRANS_FILE = "888";
    public static final String OBD_START_UPGRADE = "999";

    public volatile static int fileResult = 0;


}
