package it.eng.cam.finder;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class FinderFactory {

	/**logger for this class**/
	private static final Logger logger = Logger
			.getLogger(FinderFactory.class.getName());
	
	public static FinderFactory instance = null;
	public static ResourceBundle finder = null;
	
	static {
		finder = ResourceBundle.getBundle("cam-service");
	}
	
	/**constructor for this class***/
	private FinderFactory () {
	}
	
	/**
	 * get static instanceof this class
	 * @return
	 */
	public static FinderFactory getInstance () {
		if (instance == null) {
			instance = new FinderFactory();
		}
		return instance;
	}
	
	/**
	 * Read property from ENV or ResourceBundle
	 * @param property
	 * @return
	 */
	public String getString (String property) {
		if (logger.isDebugEnabled())
			logger.debug("property --> " + property);
		
		String value = null;
		try {
			value = System.getenv(property);
		} catch (NullPointerException|SecurityException e) {
			logger.error("Unable to read env variable --> " + property, e);
		}

		if (logger.isDebugEnabled())
			logger.debug("ENV Value --> " + value);
		
		if (value != null)
			return value;
		
		try {
			value = finder.getString(property);
		} catch (MissingResourceException e) {
			logger.error("Unable to read env variable --> " + property, e);
		}

		if (logger.isDebugEnabled())
			logger.debug("ResourceBundle Value --> " + value);

		return value;
	}
}
