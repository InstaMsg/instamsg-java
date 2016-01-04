package device.linux.instamsg;

import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.Log;
import common.instamsg.driver.Watchdog;

public class DeviceWatchdog implements Watchdog {

	boolean watchdogActive = false;
	
	
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
	public void watchdogResetAndEnable(final int n, String callee) {
		watchdogActive = true;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				for(int i = n; i >= 0; i--) {
					
					if(watchdogActive == false) {
						return;
					}
					
					InstaMsg.startAndCountdownTimer(1, false);
				}
				
				/*
				 * If control reaches here.. it means that the loop has run to completion, and the
				 * watchdog is still active.
				 */
				Log.infoLog("Watchdog-timer of interval [" + n + "] seconds expired for callee [" + callee + "]... rebooting device.");
				InstaMsg.misc.rebootDevice();
			}
		}).start();;
	}
	
	
	/**
	 * This method disables the watchdog-timer.
	 */
	@Override
	public void watchdogDisable() {
		watchdogActive = false;
	}
}
