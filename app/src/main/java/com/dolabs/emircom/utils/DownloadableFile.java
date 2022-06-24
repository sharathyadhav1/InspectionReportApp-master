package com.dolabs.emircom.utils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class DownloadableFile extends File {

	private final String TAG = this.getClass().getSimpleName();
	private static final long serialVersionUID = 2247012619505115863L;

	private long expectedSize = 0;
	private byte[] sha1sum;
	private byte[] aeskey;
	private byte[] iv;

	public DownloadableFile(String parent, String child) {
		super(parent, child);
	}

	public DownloadableFile(String path) {
		super(path);
	}

	public DownloadableFile(URI path) {
		super(path);
	}

	public long getSize() {
		return super.length();
	}

	public long getExpectedSize() {

		if(this.expectedSize == 0)
			this.expectedSize = getSize();

		return this.expectedSize;
	}

	public String getMimeType() {

		String path = this.getAbsolutePath();
		int start = path.lastIndexOf('.') + 1;

		if (start < path.length()) {

			String mime = MimeUtils.guessMimeTypeFromExtension(path.substring(start));
			return mime == null ? "" : mime;
		}
		else {

			return "";
		}
	}

	public void setExpectedSize(long size) {
		this.expectedSize = size;
	}

	public byte[] getSha1Sum() {
		return this.sha1sum;
	}

	public void setSha1Sum(byte[] sum) {
		this.sha1sum = sum;
	}

	public void setKeyAndIv(byte[] keyIvCombo) {
		// originally, we used a 16 byte IV, then found for aes-gcm a 12 byte IV is ideal
		// this code supports reading either length, with sending 12 byte IV to be done in future
		if (keyIvCombo.length == 48) {
			this.aeskey = new byte[32];
			this.iv = new byte[16];
			System.arraycopy(keyIvCombo, 0, this.iv, 0, 16);
			System.arraycopy(keyIvCombo, 16, this.aeskey, 0, 32);
		} else if (keyIvCombo.length == 44) {
			this.aeskey = new byte[32];
			this.iv = new byte[12];
			System.arraycopy(keyIvCombo, 0, this.iv, 0, 12);
			System.arraycopy(keyIvCombo, 12, this.aeskey, 0, 32);
		} else if (keyIvCombo.length >= 32) {
			this.aeskey = new byte[32];
			System.arraycopy(keyIvCombo, 0, aeskey, 0, 32);
		}
	}

	public void setKey(byte[] key) {
		this.aeskey = key;
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}

	public byte[] getKey() {
		return this.aeskey;
	}

	public byte[] getIv() {
		return this.iv;
	}

	public byte[] getBytes() {

		byte[] b = new byte[0];

		try {

			FileChannel fChannel = new FileInputStream(this).getChannel();
			byte[] bArray = new byte[(int) this.length()];
			ByteBuffer bb = ByteBuffer.wrap(bArray);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			fChannel.read(bb);

			return bArray;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return b;
	}
}
