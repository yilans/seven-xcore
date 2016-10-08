package com.seven.x.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class RandomGUID {
	private String fullGUID = "";
	private static Random random;// 非安全流水号
	private static SecureRandom secureRandom;// 安全流水号
	private static String ipLocalHostAddress = null;//
	private boolean isSecureRandomSet;// 安全随机数设置
	private String ipAndHashCode;// IP ||hashcode

	static {
		try {
			ipLocalHostAddress = InetAddress.getLocalHost().toString();// 客户端IP地址生成的字符串
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		secureRandom = new SecureRandom();
		long l = secureRandom.nextLong();// 随机种子
		random = new Random(l);
	}

	public RandomGUID() {
		this(false);
	}

	public RandomGUID(boolean isSecureRandomSet) {
		this.isSecureRandomSet = isSecureRandomSet;

		StringBuffer sb = new StringBuffer(ipLocalHostAddress);

		sb.append(':').append(System.identityHashCode(this));
		this.ipAndHashCode = sb.toString();

		next();
	}

	public RandomGUID next() {
		StringBuffer sb = new StringBuffer();

		long currentTimeMillis = System.currentTimeMillis();
		long nextLong = this.isSecureRandomSet ? secureRandom.nextLong() : random.nextLong();

		sb.append(this.ipAndHashCode);
		sb.append(":");
		sb.append(Long.toString(currentTimeMillis));
		sb.append(":");
		sb.append(Long.toString(nextLong));

		String str = sb.toString();

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Error: " + e);
		}

		messageDigest.update(str.getBytes());

		byte[] bytes = messageDigest.digest();
		StringBuffer _sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			int j = bytes[i] & 0xFF;
			if (j < 16)
				_sb.append('0');
			_sb.append(Integer.toHexString(j));
		}

		this.fullGUID = _sb.toString();

		return this;
	}

	public boolean equals(Object obj) {
		return ((obj instanceof RandomGUID)) && (((RandomGUID) obj).toPlainString().equals(toPlainString()));
	}

	public int hashCode() {
		return toPlainString().hashCode();
	}

	public String toPlainString() {
		return this.fullGUID;
	}

	public String toString() {
		String str = this.fullGUID.toUpperCase();
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

}
