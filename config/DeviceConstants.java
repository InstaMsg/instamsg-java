package config;

import common.instamsg.driver.Log;

public class DeviceConstants {

	/**
	 * THESE VARIABLES TO BE CHANGED BY DEVICE-VENDOR, AS PER THE DEVICE BEING IMPLEMENTED.
	 */
	public static final String INSTAMSG_HOST			=   "device.instamsg.io";
	public static final String INSTAMSG_HTTP_HOST       =   "platform.instamsg.io";
	public static final int INSTAMSG_HTTP_PORT          =   80;
	public static final int INSTAMSG_HTTPS_PORT         =   443;
 	public static final String DEVICE_NAME              =   "";
	public static final int LOG_LEVEL                   =   Log.INSTAMSG_LOG_LEVEL_INFO;
	public static final boolean SSL_SOCKET              =   false;
	public static final boolean GSM_DEVICE              =   false;
	public static final boolean MEDIA_STREAMING_ENABLED =   false;
	public static final String SENSEGROW_FOLDER         =   "";
	public static final int OTA_PING_BUFFER_SIZE        =   100;
	/**
	 *
	 */	
}
