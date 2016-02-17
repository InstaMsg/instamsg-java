package device.stub.instamsg;

import common.instamsg.driver.WatchDogBeforeRebootHandler;
import common.instamsg.driver.Watchdog;

public class DeviceWatchdog implements Watchdog {

	
	/**
	 * This method initializes the watchdog-timer.
	 */
	@Override
	public void watchdogInit() {

	}
	
	
	/**
	 * This method resets the watchdog-timer.
	 *
	 * Once this is completed, the watchdog-timer starts counting down from "n" seconds to 0.
	 * Then either of the following must happen ::
	 *
	 * a)
	 * Counter reaches 0.
	 *
	 * The device must then be reset/restarted.
	 *
	 * b)
	 * "watchdogDisable()" is called.
	 *
	 * In this case, the countdown-timer stops, and the device must never be reset/restarted (until the entire
	 * "watchdogResetAndEnable" loop is repeated).
	 *
	 */
	@Override
	public void watchdogResetAndEnable(final int n, String callee, WatchDogBeforeRebootHandler handler) {
		
	}
	
	
	/**
	 * This method disables the watchdog-timer.
	 */
	@Override
	public void watchdogDisable() {
		
	}
}
