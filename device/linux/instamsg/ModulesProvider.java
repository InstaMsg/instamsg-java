package device.linux.instamsg;

import common.instamsg.driver.Config;
import common.instamsg.driver.DataLogger;
import common.instamsg.driver.Log;
import common.instamsg.driver.Media;
import common.instamsg.driver.Misc;
import common.instamsg.driver.ModulesProviderInterface;
import common.instamsg.driver.Socket;
import common.instamsg.driver.Time;
import common.instamsg.driver.Upgrade;
import common.instamsg.driver.Watchdog;

public class ModulesProvider implements ModulesProviderInterface {

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

	@Override
	public Media getMedia() {
		return new DeviceMedia();
	}

	@Override
	public DataLogger getDataLogger() {
		return new DeviceDataLogger();
	}

	@Override
	public Upgrade getUpgrade() {
		return new DeviceUpgrade();
	}
}
