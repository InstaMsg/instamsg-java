package common.instamsg.driver;

public interface Watchdog {
	void watchdogInit();
	void watchdogResetAndEnable(final int n, String callee, WatchDogBeforeRebootHandler handler);
	public void watchdogDisable();
}
