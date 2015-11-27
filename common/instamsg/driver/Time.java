package common.instamsg.driver;

public interface Time {
	
	void initGlobalTimer();
	long getMinimumDelayPossibleInMicroSeconds();
	void minimumDelay();
	long getCurrentTick();
}
