package common.instamsg.driver;

import utils.Config;

import common.instamsg.driver.include.Log;
import common.instamsg.driver.include.Misc;
import common.instamsg.driver.include.ModulesProviderFactory;
import common.instamsg.driver.include.ModulesProviderInterface;
import common.instamsg.driver.include.SerialLogger;
import common.instamsg.driver.include.Time;
import common.instamsg.driver.include.Watchdog;

public class Globals {
	
	public static void globalSystemInit(String logFilePath){
		
		ModulesProviderInterface modulesProvider = ModulesProviderFactory.getModulesProvider(Config.DEVICE_NAME);
		
	    Misc misc = modulesProvider.getMisc();
		misc.bootstrapInit();

	    Log.currentLogLevel = common.instamsg.driver.include.Globals.LOG_LEVEL;
	    
	    SerialLogger serialLogger = modulesProvider.getSerialLogger();
	    serialLogger.initSerialLogger();
	    
	    Time time = modulesProvider.getTime();
	    time.initGlobalTimer();
	    
	    Watchdog watchdog = modulesProvider.getWatchdog();
	    watchdog.watchdogInit();

	    
	    if(Config.FILE_SYSTEM_INTERFACE_ENABLED){
	    	common.instamsg.driver.Log.initFileLogger(Log.fileLogger, logFilePath);
	    }
	}
}
