package common.instamsg.driver;

public interface LogAPIs {

	/**
	 * Logging at INFO level.
	 */
	public void infoLog(String log);
	
	
	/**
	 * Logging at ERROR level.
	 */
	public void errorLog(String log);
	
	
	/**
	 * Logging at DEBUG level.
	 */
	public void debugLog(String log);
}
