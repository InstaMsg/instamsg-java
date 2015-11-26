package common.instamsg.driver.include;

import common.instamsg.driver.Globals.ReturnCode;


public abstract class Log {
	
	static int INSTAMSG_LOG_LEVEL_DISABLED  = 0;
	static int INSTAMSG_LOG_LEVEL_INFO      = 1;
	static int INSTAMSG_LOG_LEVEL_ERROR     = 2;
	static int INSTAMSG_LOG_LEVEL_DEBUG     = 3;
	
	public static int currentLogLevel;

	public abstract void initLogger();
	public abstract ReturnCode loggerWrite(byte[] buffer, int len);
	public abstract void releaseLogger();
}
