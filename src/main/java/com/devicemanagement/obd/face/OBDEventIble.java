package com.devicemanagement.obd.face;

/**
 * Created by Abner on 2017/5/13.
 * 处理obd返回的数据处理和发送tcp
 */

public interface   OBDEventIble {
    void dispose(final byte[] bytes);
}
