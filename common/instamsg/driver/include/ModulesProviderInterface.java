package common.instamsg.driver.include;


public interface ModulesProviderInterface {
	
	Config getConfig();
	FileSystem getFileSystem();
	Misc getMisc();
	SerialLogger getSerialLogger();
	Time getTime();
	Watchdog getWatchdog();
	Socket getSocket(String hostName, int port);	
	
}
