package device.linux.instamsg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import common.instamsg.driver.Config;
import common.instamsg.driver.FileUtils;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.InstaMsg.ReturnCode;
import common.instamsg.driver.Json;
import common.instamsg.driver.Log;
import config.DeviceConstants;

public class DeviceConfig extends Config {

	String CONFIG_FILE_NAME = DeviceConstants.SENSEGROW_FOLDER + "config.txt";
	


	
	private String getConfigValueFromPersistentStorageAndDeleteIfAsked(String key, boolean deleteConfig) {
		
		String config = null;
		BufferedReader configReader = null;
		
		try {
			configReader = new BufferedReader(new FileReader(CONFIG_FILE_NAME));
			
		} catch (FileNotFoundException e) {
			
			Log.errorLog(CONFIG_ERROR + "Config file [" + CONFIG_FILE_NAME + "] does not exist.");
			return null;
		}
		
		while(true) {
			
			String line = null;
			try {
				line = configReader.readLine();
				
			} catch (IOException e) {
				
				Log.errorLog(CONFIG_ERROR + "Error occurred while reading config .. not continuing ..");
				FileUtils.cleanFileReader(CONFIG_ERROR, configReader);
				
				return null;
			}
			
			if(line != null) {

				String keyFromLine = Json.getJsonKeyValueIfPresent(line, CONFIG_KEY_KEY);
				if(keyFromLine.equals(key)) {

					/*
					 * We found the config.
					 */
					if(deleteConfig == false) {

						config = line;

					} else {

					}

					/*
					 * We keep looking further, just in case there are multiple-values of the config.
					 * If BYYY CHHHANCCE anyone stores multiple values, following will happen ::
					 *
					 * a)
					 * For getting config, the LAST config (for the particular key) will be picked.
					 *
					 * b)
					 * For deleting config, ALL configs (for the particular key) will be deleted.
					 */


				} else {

					if(deleteConfig == true) {
						FileUtils.appendLine(CONFIG_ERROR, FileUtils.TEMP_FILE_NAME, line);
					}
				}

			} else {
				break;
			}
		}
		
		FileUtils.cleanFileReader(CONFIG_ERROR, configReader);
		
		/*
		 * Finally.. move the temp-file.
		 */
		if(deleteConfig == true) {
			new File(FileUtils.TEMP_FILE_NAME).renameTo(new File(CONFIG_FILE_NAME));
		}
		
		return config;
	}
	
	
	/**
	 * This method initializes the Config-Interface for the device.
	 */
	@Override
	public void initConfig() {
		
	}
	
	/**
	 * This method fills in the JSONified-config-value for "key" into "buffer".
	 *
	 * It returns the following ::
	 *
	 * SUCCESS ==> If a config with the specified "key" is found.
	 * FAILURE ==> If no config with the specified "key" is found.
	 */
	@Override
	public String getConfigValueFromPersistentStorage(String key) {
		
		return getConfigValueFromPersistentStorageAndDeleteIfAsked(key, false);
	}

	
	/**
	 * This method saves the JSONified-config-value for "key" onto persistent-storage.
	 * The example value is of the form ::
	 *
	 *      {'key' : 'key_value', 'type' : '1', 'val' : 'value', 'desc' : 'description for this config'}
	 *
	 *
	 * Note that for the 'type' field :
	 *
	 *      '0' denotes that the key-type is of STRING
	 *      '1' denotes that the key-type is of INTEGER (although it is stored in stringified-form in 'val' field).
	 *
	 * It returns the following ::
	 *
	 * SUCCESS ==> If the config was successfully saved.
	 * FAILURE ==> If the config could not be saved.
	 */
	@Override
	public ReturnCode saveConfigValueOnPersistentStorage(String key, String json) {
		
		deleteConfigValueFromPersistentStorage(key);
		FileUtils.appendLine(CONFIG_ERROR, CONFIG_FILE_NAME, json);
		
		
		return InstaMsg.ReturnCode.SUCCESS;
	}

	
	/**
	 * This method deletes the JSONified-config-value for "key" (if at all it exists).
	 *
	 * It returns the following ::
	 *
	 * SUCCESS ==> If a config with the specified "key" was found and deleted successfully.
	 * FAILURE ==> In every other case.
	 */
	@Override
	public ReturnCode deleteConfigValueFromPersistentStorage(String key) {
		
		getConfigValueFromPersistentStorageAndDeleteIfAsked(key, true);
		return InstaMsg.ReturnCode.SUCCESS;
	}

}
