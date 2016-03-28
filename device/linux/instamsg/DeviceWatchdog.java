package device.linux.instamsg;

import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.Watchdog;

public class DeviceWatchdog extends Watchdog {

	boolean watchdogActive = false;
	boolean immediateReboot = false;
	
	
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
	 * In this case, the base-class-variable "watchdogExpired" variable must be set to "true".
	 * Also, if base-class-variable "immediate" is "true", the device must be reboooted immediately.
	 *
	 * b)
	 * "watchdogDisable()" (the global API-function) is called by the callee.
	 *
	 * In this case, the countdown-timer stops, and the device must not be reset/restarted.
	 *
	 */
	@Override
	public void doWatchdogResetAndEnable(final int n) {
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
				
				
				watchdogExpired = true;
				
				if(immediateReboot == true) {
					printRebootingMessage();
					InstaMsg.misc.rebootDevice();	
				}
			}
		}).start();;
	}
	
	
	/**
	 * This method disables the watchdog-timer.
	 */
	@Override
	public void doWatchdogDisable() {
		watchdogActive = false;
	}
}
