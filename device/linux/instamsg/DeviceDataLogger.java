package device.linux.instamsg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import common.instamsg.driver.DataLogger;
import common.instamsg.driver.FileUtils;
import common.instamsg.driver.Log;



public class DeviceDataLogger extends DataLogger {	
	
	String DATA_FILE_NAME = "/home/sensegrow/data.txt";
	
	
	/**
	 * This method initializes the data-logger-interface for the device.
	 */
	public void initDataLogger()
	{
	}


	/**
	 * This method saves the record on the device.
	 *
	 * If and when the device-storage becomes full, the device MUST delete the oldest record, and instead replace
	 * it with the current record. That way, we will maintain a rolling-data-logger.
	 */
	public void saveRecordToPersistentStorage(String record)
	{
		FileUtils.appendLine(DATA_LOGGING_ERROR, DATA_FILE_NAME, record);		
	}


	/**
	 * The method returns the next available record.
	 * If a record is available, following must be done ::
	 *
	 * 1)
	 * The record must be deleted from the storage-medium (so as not to duplicate-fetch this record later).
	 *
	 * 2)
	 * Then actually return the record.
	 *
	 * Obviously, there is a rare chance that step 1) is finished, but step 2) could not run to completion.
	 * That would result in a data-loss, but we are ok with it, because we don't want to send duplicate-records to InstaMsg-Server.
	 *
	 * We could have done step 2) first and then step 1), but in that scenario, we could have landed in a scenario where step 2)
	 * was done but step 1) could not be completed. That could have caused duplicate-data on InstaMsg-Server, but we don't want
	 * that.
	 *
	 *
	 * One of the following statuses must be returned ::
	 *
	 * a)
	 * SUCCESS, if a record is successfully returned.
	 *
	 * b)
	 * FAILURE, if no record is available.
	 */
	public String getNextRecordFromPersistentStorage()
	{
		String data = null;
		String temp = null;
		BufferedReader configReader = null;
		
		try {
			configReader = new BufferedReader(new FileReader(DATA_FILE_NAME));
			
		} catch (FileNotFoundException e) {
			
			Log.errorLog(DATA_LOGGING_ERROR + "Data file [" + DATA_FILE_NAME + "] does not exist.");
			return null;
		}
		
		
	    FileUtils.createEmptyFile(DATA_LOGGING_ERROR, FileUtils.TEMP_FILE_NAME);
	    
		boolean lineRead = false;
	    while(true)
	    {
  	
			try {
				temp = configReader.readLine();
				
			} catch (IOException e) {
				
				Log.errorLog(DATA_LOGGING_ERROR + "Error occurred while reading config .. not continuing ..");
				FileUtils.cleanFileReader(DATA_LOGGING_ERROR, configReader);
				
				return null;
			}
			
			if(temp != null) {

				if(temp.length() > 0) {

					if(lineRead == false) {
						
						data = temp;
						lineRead = true;
						
					} else {
						FileUtils.appendLine(DATA_LOGGING_ERROR, FileUtils.TEMP_FILE_NAME, temp);
					}

				} else {
					break;
				}

			} else {
				break;
			}
		}
		
		FileUtils.cleanFileReader(DATA_LOGGING_ERROR, configReader);
		new File(FileUtils.TEMP_FILE_NAME).renameTo(new File(DATA_FILE_NAME));
		
		return data;		
	}
}
