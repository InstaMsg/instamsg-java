package device.stub.instamsg;

import common.instamsg.driver.Config;
import common.instamsg.driver.include.FileSystem;
import common.instamsg.driver.include.Log;
import common.instamsg.driver.include.Misc;
import common.instamsg.driver.include.ModulesProviderInterface;
import common.instamsg.driver.include.Socket;
import common.instamsg.driver.include.Time;
import common.instamsg.driver.include.Watchdog;

public class ModulesProvider implements ModulesProviderInterface {

	@Override
	public FileSystem getFileSystem() {
		return new DeviceFileSystem();
	}

	@Override
	public Misc getMisc() {
		return new DeviceMisc();
	}

	@Override
	public Log getLogger() {
		return new DeviceLogger();
	}

	@Override
	public Time getTime() {
		return new DeviceTime();
	}

	@Override
	public Watchdog getWatchdog() {
		return new DeviceWatchdog();
	}


	@Override
	public Socket getSocket(String hostName, int port) {
		return new DeviceSocket(hostName, port);
	}

	@Override
	public Config getConfig() {
		return new DeviceConfig();
	}

}
