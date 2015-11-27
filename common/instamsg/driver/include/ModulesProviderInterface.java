package common.instamsg.driver.include;

import common.instamsg.driver.Config;


public interface ModulesProviderInterface {
	
	Config getConfig();
	FileSystem getFileSystem();
	Misc getMisc();
	Log getLogger();
	Time getTime();
	Watchdog getWatchdog();
	Socket getSocket(String hostName, int port);	
}
