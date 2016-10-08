package com.seven.x.core.util;

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Random;

public class GUID {//
	private static String initValue;// 取用IP地址取初始化值
	private static Random random;// 非安全流水号
	private static SecureRandom secureRandom;// 安全流水号
	private String fractalGUID;
	private String fullGUID;
	private boolean isSecureRandomSet;// 安全随机数设置

	static {
		try {
			InetAddress address = InetAddress.getLocalHost();
			byte[] bytes = address.getAddress();// 用IP地址取值为做初始值
			initValue = LeftPaddedWithZerosShift(ArrayAdd(bytes), 8);
		} catch (Exception e) {
		}

		secureRandom = new SecureRandom();
		long l = secureRandom.nextLong();
		random = new Random(l);
	}

	private static int ArrayAdd(byte[] bytes) {
		int result = bytes[0] & 0xFF000000;
		result += (bytes[1] & 0xFF0000);
		result += (bytes[2] & 0xFF00);
		result += (bytes[3] & 0xFF);
		return result;
	}

	public GUID() {
		this(false); // 用非安全模式初始化
	}

	public GUID(boolean isSecureRandomSet) {
		this.isSecureRandomSet = isSecureRandomSet;
		StringBuffer sb = new StringBuffer(initValue);

		sb.append(LeftPaddedWithZerosShift(System.identityHashCode(this), 8));
		this.fractalGUID = sb.toString();
		next();
	}

	public GUID next() {
		int currentTimeMillis = (int) System.currentTimeMillis();
		int newRandom = this.isSecureRandomSet ? secureRandom.nextInt() : random.nextInt();
		this.fullGUID = (LeftPaddedWithZerosShift(currentTimeMillis, 8) + this.fractalGUID + LeftPaddedWithZerosShift(newRandom, 8));
		return this;
	}

	public String toPlainString() {
		return this.fullGUID;
	}

	public String toString() {
		String str = toPlainString().toUpperCase();
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0, 8));
		sb.append("-");
		sb.append(str.substring(8, 12));
		sb.append("-");
		sb.append(str.substring(12, 16));
		sb.append("-");
		sb.append(str.substring(16, 20));
		sb.append("-");
		sb.append(str.substring(20));

		return sb.toString();
	}

	public boolean equals(Object obj) {
		return ((obj instanceof GUID)) && (((GUID) obj).toPlainString().equals(toPlainString()));
	}

	public int hashCode() {
		return toPlainString().hashCode();
	}

	private static String LeftPaddedWithZerosShift(int shiftNum, int length) {// 整数转换16进制数字，然后左补零
		String str = Integer.toHexString(shiftNum);
		if (str.length() < length) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < length - str.length(); i++) {
				sb.append("0");
			}
			sb.append(str);
			return sb.toString();
		}
		return str;
	}
}
