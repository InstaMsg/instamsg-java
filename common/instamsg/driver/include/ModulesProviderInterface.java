package common.instamsg.driver.include;

public interface ModulesProviderInterface {
	
	FileSystem getFileSystem();
	Misc getMisc();
	SerialLogger getSerialLogger();
	Time getTime();
	Watchdog getWatchdog();
	
}
