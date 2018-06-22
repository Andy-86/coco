package com.devicemanagement.devicePacket.clientPacket;

import com.devicemanagement.devicePacket.ClientPacket;

/**
 * Created by mac on 2018/5/22.
 */

public class ClientCans extends ClientPacket {
    private byte[] cans;
    public ClientCans(byte version, short id, short attribute, long imei, short seq, byte reserved, byte[] cans) throws Exception {
        super(version, id, attribute, imei, seq, reserved);
        this.cans=cans;
        callAfterSetMemberDone();
    }

    @Override
    protected void dumpData() throws Exception {
        setDataOfRange(0,cans);
    }
}
