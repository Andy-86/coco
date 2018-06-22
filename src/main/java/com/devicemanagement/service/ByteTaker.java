package com.devicemanagement.service;

public class ByteTaker {
    private byte[] bytes;
    public ByteTaker(byte[] can){
        this.bytes=can;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
