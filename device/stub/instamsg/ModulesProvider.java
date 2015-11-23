package device.stub.instamsg;

import common.instamsg.driver.include.FileSystem;
import common.instamsg.driver.include.Misc;
import common.instamsg.driver.include.ModulesProviderInterface;
import common.instamsg.driver.include.SerialLogger;
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
	public SerialLogger getSerialLogger() {
		return new DeviceSerialLogger();
	}

	@Override
	public Time getTime() {
		return new DeviceTime();
	}

	@Override
	public Watchdog getWatchdog() {
		return new DeviceWatchdog();
	}

}
