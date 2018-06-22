package com.devicemanagement.devicePacket.clientPacket;

import android.os.SystemClock;

import com.devicemanagement.MyApplication;
import com.devicemanagement.data.Data;
import com.devicemanagement.data.MLocation;
import com.devicemanagement.devicePacket.ClientPacket;
import com.devicemanagement.phoneState.CPU;
import com.devicemanagement.util.CPUUtil;
import com.devicemanagement.util.DataUtil;


/**
 * Created by Administrator on 2017/8/28 0028.
 * @author lg
 */
public class Heartbeat extends ClientPacket {

    //sum of size
    public static final short SIZE_HEARTBEAT_DATA_BYTE = 32;
    // size
    protected static final int SIZE_GPS_TYPE_BYTE = 1;
    protected static final int SIZE_LATITUDE_BYTE = 4;
    protected static final int SIZE_LONGTITUDE_BYTE = 4;
    protected static final int SIZE_SENSORX_BYTE = 2;
    protected static final int SIZE_SENSORY_BYTE = 2;
    protected static final int SIZE_SENSORZ_BYTE = 2;
    protected static final int SIZE_CPU_TEMP_BYTE = 2;
    protected static final int SIZE_MEMORY_BYTE = 1;
    protected static final int SIZE_CPU_USAGE_BYTE = 1;
    protected static final int SIZE_AVAILABLE_MEMORY_BYTE = 2;
    protected static final int SIZE_BOOT_TIME_BYTE = 1;
    protected static final int SIZE_GPSINUSE_BYTE = 1;
    protected static final int SIZE_SIGNAL_BYTE = 1;
    protected static final int SIZE_TIME_BYTE = 8;

    // byte offset ，通过 每个字段的 size 计算偏移，而不是写死，这样一旦有变动，只要改 size 和顺序，就可以全部调整过来，需要逐一修改
    /** | GPS            | 1byte        | 0     | 0=关闭 / 1=GPS 84 / 2=GPS NEMA / 3=GPS baidu / 11=BD1 / 12=BD2 / 21=GNSS （注：目前是2 GPS） | */
    protected static final int OFFSET_GPS_TYPE = 0;
    /** | latitude       | 4byte(float) | 1-4  | 纬度 | */
    protected static final int OFFSET_LATITUDE = OFFSET_GPS_TYPE
            + SIZE_GPS_TYPE_BYTE;
    /** | longitude      | 4byte(float) | 5-8 | 经度 | */
    protected static final int OFFSET_LONGITUDE = OFFSET_LATITUDE
            + SIZE_LATITUDE_BYTE;
    /** | sensorX        | 2byte        | 10-11 | 传感器X | */
    protected static final int OFFSET_SENSOR_X = OFFSET_LONGITUDE
            + SIZE_LONGTITUDE_BYTE;
    /** | sensorY        | 2byte        | 12-13 | 传感器Y | */
    protected static final int OFFSET_SENSOR_Y = OFFSET_SENSOR_X
            + SIZE_SENSORX_BYTE;
    /** | sensorZ        | 2byte        | 14-15 | 传感器Z | */
    protected static final int OFFSET_SENSOR_Z = OFFSET_SENSOR_Y
            + SIZE_SENSORY_BYTE;
    /** | CPU-TEMP       | 2byte(short) | 16-17 | CPU温度  | */
    protected static final int OFFSET_CPU_TEMP = OFFSET_SENSOR_Z
            + SIZE_SENSORZ_BYTE;
    /** | memory        | 1byte  | 25 | 剩余内存 | */
    protected static final int OFFSET_MEMORY = OFFSET_CPU_TEMP
            + SIZE_CPU_TEMP_BYTE;
    /** | cpu-usage       | 1byte  | 26 | cpu使用率（负载） | */
    protected static final int OFFSET_CPU_USAGE = OFFSET_MEMORY
            + SIZE_MEMORY_BYTE;
    /** | device-available_memory       | 2byte  | 27 - 28| 设备剩余空间 | */
    protected static final int OFFSET_AVAILABLE_MEMORY = OFFSET_CPU_USAGE
            + SIZE_CPU_USAGE_BYTE;
    /** | BOOT_TIME       | 1byte  | 29 | 设备开机时间 | */
    protected static final int OFFSET_BOOT_TIME = OFFSET_AVAILABLE_MEMORY
            + SIZE_AVAILABLE_MEMORY_BYTE;
    /** | gpsInUse       |  1byte | 30 | GPS 可用卫星个数*/
    protected static final int OFFSET_GPS_IN_USE = OFFSET_BOOT_TIME
            + SIZE_BOOT_TIME_BYTE;
    /** | signal       |  1byte | 31 | 通讯信号值*/
    protected static final int OFFSET_SIGNAL = OFFSET_GPS_IN_USE
            + SIZE_GPSINUSE_BYTE;
    /** | time            | 8byte(long) | 32-39 | GPS时间 | */
    protected static final int OFFSET_TIME = OFFSET_SIGNAL + SIZE_SIGNAL_BYTE;

    private static final boolean isJRHB = true;

    public Heartbeat(byte version, short id, short attribute, long imei, short seq, byte obl)throws Exception{
        super(version,id,attribute,imei,seq,obl);
        callAfterSetMemberDone();
    }

    private byte gpsType;
    private float latitude;
    private float longitude;
    private short GSensorX;
    private short GSensorY;
    private short GSensorZ;
    private short cpuTemp;

    private byte memory;
    private byte cpu_usage;
    private short sdMemory;
    private byte bootOnHours;
    private byte gpsInUse;
    private byte Signal;
    private long time;

    /**
     * 给心跳包的数据赋值
     */
    public void initHeartbeatVar(){
        this.gpsType = MLocation.getGPSState();
        this.latitude = MLocation.getLat();
        this.longitude = MLocation.getLon();
        this.GSensorX = (short)Data.getGsensor_x();
        this.GSensorY= (short)Data.getGsensor_y();
        this.GSensorZ = (short)Data.getGsensor_z();
        this.cpuTemp = (short) (CPUUtil.getCPUTemperature());
        this.memory = (byte) CPU.getAvailMemory(MyApplication.getContext());  //Data.getMemory();
        this.cpu_usage = (byte) CPU.getProcessCpuRate();  //Data.getCpu_usage();
        this.sdMemory = CPU.getSDAvailableSize(MyApplication.getInstance());   //Data.getSdMemory();
        this.bootOnHours = (byte)(SystemClock.elapsedRealtime()/3600000);
        this.gpsInUse = MLocation.getGpsInuse();
        this.Signal = Data.getSignal();
        this.time = Data.getTime();

    }


    /**
     * 将心跳包数据装换成数组
     * @throws Exception
     */
    @Override
    protected void dumpData() throws Exception {
        //初始化心跳包数据
        initHeartbeatVar();

        //将心跳包数据转化为byte数组
        setDataAt(OFFSET_GPS_TYPE,gpsType);
        setDataOfRange(OFFSET_LATITUDE, DataUtil.floatTobyte(latitude));
        setDataOfRange(OFFSET_LONGITUDE, DataUtil.floatTobyte(longitude));
        setDataOfRange(OFFSET_SENSOR_X,DataUtil.shortToByte(GSensorX));
        setDataOfRange(OFFSET_SENSOR_Y,DataUtil.shortToByte(GSensorY));
        setDataOfRange(OFFSET_SENSOR_Z,DataUtil.shortToByte(GSensorZ));
        setDataOfRange(OFFSET_CPU_TEMP,DataUtil.shortToByte(cpuTemp));
        setDataAt(OFFSET_MEMORY,memory);
        setDataAt(OFFSET_CPU_USAGE,cpu_usage);
        setDataOfRange(OFFSET_AVAILABLE_MEMORY,DataUtil.shortToByte(sdMemory));
        setDataAt(OFFSET_BOOT_TIME,bootOnHours);
        setDataAt(OFFSET_GPS_IN_USE,gpsInUse);
        setDataAt(OFFSET_SIGNAL,Signal);
        setDataOfRange(OFFSET_TIME, DataUtil.longToByte(this.time));
    }

}
