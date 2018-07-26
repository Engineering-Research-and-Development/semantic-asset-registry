package it.eng.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

public class Util {

	public static String generateRandomHexToken(int byteLength) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] token = new byte[byteLength];
		secureRandom.nextBytes(token);
		return new BigInteger(1, token).toString(16); // hex encoding
	}
	
	public static String getUUID () {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}
}
