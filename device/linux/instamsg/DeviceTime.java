package device.linux.instamsg;

import java.util.Date;

import common.instamsg.driver.Log;
import common.instamsg.driver.Time;

public class DeviceTime implements Time {

	/*
	 * This method does the global-level-initialization for time (if any).
	 */
	@Override
	public void initGlobalTimer() {

	}

	/*
	 * This method returns the minimum-delay achievable via this device.
	 */
	@Override
	public long getMinimumDelayPossibleInMicroSeconds() {
		return 1000;
	}

	/*
	 * This method ACTUALLY causes the current-device to go to sleep for the minimum-delay possible.
	 */
	@Override
	public void minimumDelay() {
		try {
			Thread.sleep(1);
			
		} catch (InterruptedException e) {

			Log.infoLog("Some error occurred while thread-sleeping");
		}
	}
	
	/*
	 * This method returns the current-tick/timestamp.
	 */
	@Override
	public long getCurrentTick() {
		return (new Date().getTime() / 1000);
	}

}
