package it.eng.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.eng.orion.cb.ngsi.bean.Timestamp;

public class Util {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(it.eng.util.Util.class);

	public static String generateRandomHexToken(int byteLength) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] token = new byte[byteLength];
		secureRandom.nextBytes(token);
		return new BigInteger(1, token).toString(16); // hex encoding
	}

	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

	public static String getUUIDAsNumber() {
		return String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)).substring(0,
				5);
	}

	public static Properties getSparqlQuery() throws IOException {
		Properties prop = new Properties();
		prop.load(it.eng.util.Util.class.getClassLoader().getResourceAsStream("querysparql.properties"));
		return prop;
	}

	public static Timestamp getTimeStamp() {
		Timestamp timestamp = new Timestamp();
		timestamp.setType("String");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);
		timestamp.setValue(strDate);
		return timestamp;
	}

}
