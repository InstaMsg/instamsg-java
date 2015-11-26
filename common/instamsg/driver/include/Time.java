package common.instamsg.driver.include;

public interface Time {
	
	void initGlobalTimer();
	long getMinimumDelayPossibleInMicroSeconds();
	void minimumDelay();
	long getCurrentTick();
}
