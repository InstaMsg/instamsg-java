package common.instamsg.driver.include;


public abstract class Log {
	
	public static int currentLogLevel;
	public static FileLogger fileLogger;
	
	public static class FileLogger
	{
	    public FileSystem fs;
	}
}
