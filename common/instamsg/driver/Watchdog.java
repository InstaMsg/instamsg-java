package common.instamsg.driver;

public abstract class Watchdog {
	
	protected boolean watchdogActive = false;
	protected boolean watchdogExpired = false;
	protected boolean immediateReboot = false;
	
	String trackString;
	
	protected void printRebootingMessage()
	{
	    if(trackString == null)
	    {
	        trackString = "";
	    }

	    Log.errorLog("Watch-Dog-Timer is RESETTING DEVICE .... due to hang at [" + trackString + "]");
	}

	
	/**
	 * This method starts the watchdog timer, passing various arguments in the process ::
	 *
	 * n                    :
	 *
	 *      Number of seconds for which the watchdog must be run.
	 *
	 *
	 * callee               :
	 *
	 *      A string to identify the callee-method.
	 *      This is merely for informational/debugging purposes.
	 *
	 *
	 * immediate            :
	 *
	 *      A flag to denote whether the device should be rebooted immediately upon watchdog expiry (indispensable in certain situations).
	 *
	 *      Value of "true" denotes immediate restart.
	 *      Value of "false" denotes not-immediate restart (in this case, device will be rebooted as per the case in "watchdogDisable").
	 *
	 */
	public void watchdogResetAndEnable(int n, String callee, boolean immediate)
	{
	    watchdogActive = true;

	    watchdogExpired = false;
	    trackString = callee;

	    immediateReboot = immediate;
	    doWatchdogResetAndEnable(n);
	}


	
	/**
	 * This method disables the watchdog.
	 *
	 *
	 * Following are the parameters ::
	 *
	 * handler                 :
	 *
	 *      A callback-function that is called, if the watchdog runs to completion (that is, the watchdog has expired).
	 *
	 *
	 *
	 *
	 * Following is the behaviour ::

	 * i)
	 * If "watchdogExpired" remains "false", nothing happens.
	 *
	 * ii)
	 * If "watchdogExpired" has become "true" and "handler" is null, the device is rebooted (via call to "InstaMsg.misc.rebootDevice()").
	 *
	 * iii)
	 * If "watchdogExpired" has become "true" and "handler" is not null, then "handler" is called, and it is the responsibility of 
	 * "handler" to decide whether to reboot the device or not (and actually reboot the device is required).
	 *
	 */
	@SuppressWarnings("rawtypes")
	void watchdogDisable(WatchDogBeforeRebootHandler handler)
	{
	    watchdogActive = false;

	    doWatchdogDisable();
	    if(watchdogExpired == true)
	    {
	        if(handler != null)
	        {
	            /*
	             * Ask Deepak how to handle this.
	             */
	        }
	        else
	        {
	            printRebootingMessage();
	            InstaMsg.misc.rebootDevice();
	        }
	    }
	}

	
	public abstract void watchdogInit();	
	public abstract void doWatchdogResetAndEnable(final int n);
	public abstract void doWatchdogDisable();
}
