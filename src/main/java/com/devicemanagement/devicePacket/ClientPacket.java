package com.devicemanagement.devicePacket;


import org.apache.log4j.Logger;

public abstract class ClientPacket extends Packet {

	private Logger logger = Logger.getLogger("ClientPacket");

//	/**
//	 * 根据原始数据长度 和 加密方式计算出加密后的数据长度
//	 * @param encryption
//	 * @param rawDataLength
//	 * @return 加密后的数据长度，单位是 byte
//	 * @throws Exception
//	 */
//	private static int getEncryptedDataLength(byte encryption,
//			int rawDataLength) throws Exception {
//		switch (encryption) {
//		case ENCRYPTION_AES_128:
//			return ((rawDataLength / SIZE_AES_128_BLOCK_BYTE) + 1)
//					* SIZE_AES_128_BLOCK_BYTE;
//		case ENCRYPTION_RSA_PUBLIC:
//			throw new IllegalArgumentException("服务器数据包不支持公钥加密方式");
//		case ENCRYPTION_PLAINTEXT:
//			// fall through
//		default:
//			return rawDataLength;
//		}
//	}

	private byte[] dataBuffer = null;

	public ClientPacket(byte version,short id,short attribute,long imei,short seq,byte reserved)
			throws Exception {
		super(version,id,attribute,imei,seq,reserved);
		dataBuffer = new byte[getDataLen()];
	}

	private void aesEncrypt() throws Exception {
		// TODO: get AES key and decrypt
		setPacketOfRange(OFFSET_DATA_BODY, dataBuffer);
	}

	protected void digest() throws Exception{
		int offset_digest = getLength() - 2;
		byte check = getPacketAt(OFFSET_PROVERSION);
		for(int i = OFFSET_PROVERSION + 1; i < offset_digest; i++){
			check = (byte) (getPacketAt(i) ^ check);
		}
		setPacketAt(offset_digest,check);
	}

//	private void encrypt() throws Exception {
//		switch (getEncryptionMethod()) {
//		case ENCRYPTION_RSA_PUBLIC:
//			rsaEncrypt();
//			break;
//		case ENCRYPTION_AES_128:
//			aesEncrypt();
//			break;
//		case ENCRYPTION_PLAINTEXT:
//			// fall through
//		default:
//			paintextSet();
//		}
//	}

	private void paintextSet() throws Exception {
		setPacketOfRange(OFFSET_DATA_BODY, dataBuffer);
		//logger.info("paintextSet: "+dataBuffer.length + " , Packet bytes: "+ DataUtil.getHexString(getBytes()));
	}

	private void rsaEncrypt() throws Exception {
		// TODO: get RSA private key and decrypt
		setPacketOfRange(OFFSET_DATA_BODY, dataBuffer);
	}

	/**
	 * 统一完成 数据存储 / 加密 / 摘要 过程，供子类设置成员变量后调用
	 * @throws Exception
	 */
	protected void callAfterSetMemberDone() throws Exception {
		dumpData();
		paintextSet();
		digest();
	}

	public byte[] getDataBuffer(){
		return dataBuffer;
	}
	/**
	 * 将各个字段存放在  dataBuffer 中，为加密和校验做准备，具体存储过程跟包结构有关，由子类实现
	 * @throws Exception
	 */
	protected abstract void dumpData() throws Exception;

	protected byte getDataAt(int offset) throws Exception {
		return dataBuffer[offset];
	}
	
	protected void setDataAt(int offset, byte data) throws Exception {
		dataBuffer[offset] = data;
	}

	/**
	 * 将 offset (包括) 后的字节设置为 data，设置长度为 data 的长度
	 * @param offset
	 * @param data
	 * @throws Exception
	 */
	protected void setDataOfRange(int offset, byte[] data) throws Exception {
		for (int i = 0; i < data.length; i++) {
			dataBuffer[offset + i] = data[i];
		}
	}
}
