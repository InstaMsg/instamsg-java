package common.instamsg.driver;

public interface ModulesProviderInterface {
	
	Config getConfig();
	Misc getMisc();
	Media getMedia();
	Log getLogger();
	Time getTime();
	Watchdog getWatchdog();
	Socket getSocket(String hostName, int port);
	DataLogger getDataLogger();
}
