package com.devicemanagement.devicePacket.clientPacket;


import com.devicemanagement.devicePacket.ClientPacket;
import com.devicemanagement.util.DataUtil;

/**
 * Created by Administrator on 2017/9/14 0014.
 * BCD【2】	0-2	0402
 * 1byte	2-3	1=墨武士 / 2=叮咚出行 / 4 = 嗨皮出行
 * 10byte	4-13	Sim卡的icc_id
 * String	14-30	VinCode

 */
public class DeviceHello extends ClientPacket {

    //BODY TOTAL SIZE
    public static final short SIZE_DEVICE_HELLO_BYTE = 18;

    //size
    protected static final int SIZE_MACHINE_TYPE_BYTE = 2;
    protected static final int SIZE_CUSTORMER_BYTE = 1;
    protected static final int SIZE_SIM_OPERATOR_BYTE = 1;
    protected static final int SIZE_ICCID_BYTE = 10;
    protected static final int SIZE_VERSIONCODE_BYTE= 2;
    protected static final int SIZE_SCM_VERSION_BYTE = 2;

    //offset
    protected static final int OFFSET_MACHINE_TYPE = 0;
    protected static final int OFFSET_CUSTOMER = OFFSET_MACHINE_TYPE
            + SIZE_MACHINE_TYPE_BYTE;
    protected static final int OFFSET_SIM_OPERATOR = OFFSET_CUSTOMER
            + SIZE_CUSTORMER_BYTE;
    protected static final int OFFSET_ICCID = OFFSET_SIM_OPERATOR
            + SIZE_SIM_OPERATOR_BYTE;
    protected static final int OFFSET_VERSIONCODE = OFFSET_ICCID
            + SIZE_ICCID_BYTE;
    protected static final int OFFSET_SCM_VERSION = OFFSET_VERSIONCODE
            + SIZE_VERSIONCODE_BYTE;


    //固定字段信息
    public static final short MACHINE_TYPE = 403;
    public static final byte CUSTOMER = 16;
    public static final byte SIM_OPERATOR = 1;

    public DeviceHello(byte version, short id, short attribute, long imei, short seq, byte reserved
    , short machine, byte customer, byte operator, byte[] iccid, short versioncode, short scmVersioncode) throws Exception{
        super(version,id,attribute,imei,seq,reserved);
        this.machine_type = machine;
        this.customer = customer;
        this.simOperator = operator;
        this.iccid = iccid;
        this.scmVesionCode = scmVersioncode;
        this.versionCode = versioncode;
        callAfterSetMemberDone();
    }


    private short machine_type;
    private byte customer;
    private byte simOperator;
    private byte[] iccid;
    private short scmVesionCode;
    private short versionCode;

    @Override
    protected void dumpData() throws Exception{
        setDataOfRange(OFFSET_MACHINE_TYPE, DataUtil.shortToByte(this.machine_type));
        setDataAt(OFFSET_CUSTOMER,this.customer);
        setDataAt(OFFSET_SIM_OPERATOR,this.simOperator);
        setDataOfRange(OFFSET_ICCID,this.iccid);
        setDataOfRange(OFFSET_VERSIONCODE,DataUtil.shortToByte(this.versionCode));
        setDataOfRange(OFFSET_SCM_VERSION,DataUtil.shortToByte(this.scmVesionCode));
    }
}
