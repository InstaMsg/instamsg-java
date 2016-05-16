package common.instamsg.driver;




public abstract class Upgrade {
	
	public static final String FILE_UPGRADE     = "[FILE_UPGRADE] ";
	public static final String NEW_FILE_KEY     = "NEW_FILE_ARRIVED";
	public static final String NEW_FILE_ARRIVED = "YES";

	public void checkForUpgrade() {
		
		boolean reboot = false;
 
		String newFileConfig =  InstaMsg.config.getConfigValueFromPersistentStorage(NEW_FILE_KEY);
		if(newFileConfig == null) {
			Log.errorLog(FILE_UPGRADE + "Config not found on persistent storage ... so proceeding with old file");
			return;
		}

		String newFileArrived = Json.getJsonKeyValueIfPresent(newFileConfig, Config.CONFIG_VALUE_KEY);
		if(newFileArrived.equals(NEW_FILE_ARRIVED) == true) {
			
			removeOldExecutableBinary();
			copyNewExecutableBinaryFromTempLocation();

			Log.infoLog(FILE_UPGRADE + "Binary upgraded, restarting to take effect");
			reboot = true;
		}

		InstaMsg.config.deleteConfigValueFromPersistentStorage(NEW_FILE_KEY);

    
		if(reboot == true) {
			InstaMsg.misc.rebootDevice();
		}
	}
    
	public abstract void prepareForNewBinaryDownload();
	public abstract void copyNextChar(char c);
	public abstract void tearDownBinaryDownload();
    public abstract void removeOldExecutableBinary();
    public abstract void copyNewExecutableBinaryFromTempLocation();
}
