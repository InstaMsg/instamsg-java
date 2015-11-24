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
	
	public static Misc misc;
	public static SerialLogger serialLogger;
	public static Time time;
	public static Watchdog watchdog;
	public static common.instamsg.driver.include.Config config;
	public static final int MAX_BUFFER_SIZE = 1000;
	public static final byte[] LOG_GLOBAL_BUFFER= new byte [MAX_BUFFER_SIZE];
	
	public static String LOG_FILE_PATH = "/home/sensegrow/instamsg.log";
	public static String USER_LOG_FILE_PATH = null;
	public static String USER_DEVICE_UUID = null;
	public static int LOG_LEVEL = 2;
	public static int MAX_CLIENT_ID_SIZE = 50;
	public static int NETWORK_INFO_INTERVAL = 300;
	
	public static int pingRequestInterval;
	public static int compulsorySocketReadAfterMQTTPublishInterval;
	
	
	public static char[] GLOBAL_BUFFER = new char[MAX_BUFFER_SIZE];
	
	public static void RESET_GLOBAL_BUFFER(){
		GLOBAL_BUFFER = new char[MAX_BUFFER_SIZE];
	}
	
	
	public static void globalSystemInit(String logFilePath){
		
		ModulesProviderInterface modulesProvider = ModulesProviderFactory.getModulesProvider(Config.DEVICE_NAME);
		
		config  = modulesProvider.getConfig();
		
	    misc = modulesProvider.getMisc();
		misc.bootstrapInit();

	    Log.currentLogLevel = Globals.LOG_LEVEL;
	    
	    serialLogger = modulesProvider.getSerialLogger();
	    serialLogger.initSerialLogger();
	    
	    time = modulesProvider.getTime();
	    time.initGlobalTimer();
	    
	    watchdog = modulesProvider.getWatchdog();
	    watchdog.watchdogInit();

	    
	    if(Config.FILE_SYSTEM_INTERFACE_ENABLED){
	    	common.instamsg.driver.Log.initFileLogger(Log.fileLogger, logFilePath);
	    }
	}
}
