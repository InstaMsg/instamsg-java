package common.instamsg.driver;


public abstract class DataLogger {
	
	public static String DATA_LOGGING        =   "[DATA-LOGGING] ";
	public static String DATA_LOGGING_ERROR  =   "[DATA-LOGGING-ERROR] ";
	
	public abstract void initDataLogger();
	public abstract void saveRecordToPersistentStorage(String record);
	public abstract String getNextRecordFromPersistentStorage();
}
