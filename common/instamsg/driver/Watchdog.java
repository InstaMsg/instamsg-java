package common.instamsg.driver;

public interface Watchdog {
	void watchdogInit();
	void watchdogResetAndEnable(int n, String callee);
	public void watchdogDisable();
}
