package common.instamsg.driver.include;

import utils.Config;

import common.instamsg.driver.Globals;
import common.instamsg.driver.Globals.ReturnCode;


public abstract class Log {
	
	public static int INSTAMSG_LOG_LEVEL_DISABLED  = 0;
	public static int INSTAMSG_LOG_LEVEL_INFO      = 1;
	public static int INSTAMSG_LOG_LEVEL_ERROR     = 2;
	public static int INSTAMSG_LOG_LEVEL_DEBUG     = 3;
	
	public static int currentLogLevel;

	public abstract void initLogger();
	public abstract ReturnCode loggerWrite(byte[] buffer, int len);
	public abstract void releaseLogger();
	
	static ModulesProviderInterface modulesProvideInterface;

	static {
		modulesProvideInterface = ModulesProviderFactory.getModulesProvider(Config.DEVICE_NAME);
	}
	
	
	public static void log(String log, int level) {
		
		if(level <= Globals.LOG_LEVEL) {
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
