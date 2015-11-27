package common.instamsg.driver;

import utils.Config;

import common.instamsg.driver.include.Log;
import common.instamsg.driver.include.Misc;
import common.instamsg.driver.include.ModulesProviderFactory;
import common.instamsg.driver.include.ModulesProviderInterface;
import common.instamsg.driver.include.Time;
import common.instamsg.driver.include.Watchdog;

public class Globals {
	
	public static enum ReturnCode
	{
	    SOCKET_READ_TIMEOUT,
	    BUFFER_OVERFLOW,
	    FAILURE,
	    SUCCESS
	};
	
	public static Misc misc;
	public static Log logger;
	public static Time time;
	public static common.instamsg.driver.Config config;
	public static Watchdog watchdog;
	public static final int MAX_BUFFER_SIZE = 1000;
	public static final byte[] LOG_GLOBAL_BUFFER= new byte [MAX_BUFFER_SIZE];
	
	public static String LOG_FILE_PATH = "/home/sensegrow/instamsg.log";
	public static String USER_LOG_FILE_PATH = null;
	public static String USER_DEVICE_UUID = null;
	public static int LOG_LEVEL = 2;
	public static int MAX_CLIENT_ID_SIZE = 50;
	public static int NETWORK_INFO_INTERVAL = 300;
	public static int MAX_CONN_ATTEMPTS_WITH_PHYSICAL_LAYER_FINE = 5;
	public static int MAX_TRIES_ALLOWED_WHILE_READING_FROM_SOCKET_MEDIUM = 1;
	public static int SOCKET_READ_TIMEOUT_SECS = 1;
	
	public static int pingRequestInterval;
	public static int compulsorySocketReadAfterMQTTPublishInterval;	
	
	public static String INSTAMSG_HOST;
	public static int INSTAMSG_PORT;
	
	static {
		INSTAMSG_HOST = "platform.instamsg.io";
		INSTAMSG_PORT = 1883;
	}
	

	public static void globalSystemInit(){
		
		ModulesProviderInterface modulesProvider = ModulesProviderFactory.getModulesProvider(Config.DEVICE_NAME);
		
		config = modulesProvider.getConfig();
		config.initConfig();
		
	    misc = modulesProvider.getMisc();
		misc.bootstrapInit();

	    Log.currentLogLevel = Globals.LOG_LEVEL;
	    
	    logger = modulesProvider.getLogger();
	    logger.initLogger();
	    
	    time = modulesProvider.getTime();
	    time.initGlobalTimer();
	    
	    watchdog = modulesProvider.getWatchdog();
	    watchdog.watchdogInit();
	}
	
	
	/*
	 * This method causes the current thread to wait for "n" seconds.
	 */
	public static void startAndCountdownTimer(int seconds, boolean showRunningStatus)
	{
	    int i;
	    long j;
	    long cycles = 1000000 / time.getMinimumDelayPossibleInMicroSeconds();

	    for(i = 0; i < seconds; i++)
	    {
	        if(showRunningStatus == true)
	        {
	        	Log.infoLog(seconds - 1 + "");
	        }

	        for(j = 0; j < cycles; j++)
	        {
	            time.minimumDelay();
	        }
	    }
	}
}
