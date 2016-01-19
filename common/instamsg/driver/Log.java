package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

import config.DeviceConstants;
import config.ModulesProviderFactory;



public abstract class Log {
	
	public static int INSTAMSG_LOG_LEVEL_DISABLED  = 0;
	public static int INSTAMSG_LOG_LEVEL_ERROR     = 1;
	public static int INSTAMSG_LOG_LEVEL_INFO      = 2;
	public static int INSTAMSG_LOG_LEVEL_DEBUG     = 3;
	

	public abstract void initLogger();
	public abstract ReturnCode loggerWrite(byte[] buffer, int len);
	public abstract void releaseLogger();
	
	static ModulesProviderInterface modulesProvideInterface;

	static {
		modulesProvideInterface = ModulesProviderFactory.getModulesProvider(DeviceConstants.DEVICE_NAME);
	}
	
	
	public static void log(String log, int level) {
		
		if(InstaMsg.instaMsg.serverLoggingEnabled == true) {			
			InstaMsg.instaMsg.publish(InstaMsg.instaMsg.serverLogsTopic,
					                  log,
					                  InstaMsg.QOS0,
					                  false,
					                  null,
					                  InstaMsg.MQTT_RESULT_HANDLER_TIMEOUT, 
					                  false);
			
		} else if(level <= DeviceConstants.LOG_LEVEL) {
			modulesProvideInterface.getLogger().loggerWrite(log.getBytes(), log.length());
		}
	}
	
	
	public static void infoLog(String log) {
		Log.log(log, INSTAMSG_LOG_LEVEL_INFO);
	}
	
	
	public static void errorLog(String log) {
		Log.log(log, INSTAMSG_LOG_LEVEL_ERROR);
	}
	
	public static void debugLog(String log) {
		Log.log(log, INSTAMSG_LOG_LEVEL_DEBUG);
	}
}
