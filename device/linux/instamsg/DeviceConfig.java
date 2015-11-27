package device.linux.instamsg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import common.instamsg.driver.Config;
import common.instamsg.driver.Globals.ReturnCode;
import common.instamsg.driver.Json;
import common.instamsg.driver.include.Log;

public class DeviceConfig extends Config {

	String CONFIG_FILE_NAME = "/home/sensegrow/config.txt";
	String TEMP_FILE_NAME   = "/home/sensegrow/temp";
	
	private void cleanFileReader(BufferedReader configReader) {
		
		if(configReader != null) {
			
			try {
				configReader.close();
				
			} catch (IOException e) {				
				Log.errorLog(CONFIG_ERROR + "Error happened while cleaning a file-reader");
				
			}
		}
	}
	
	private void appendLine(String filePath, String line) {
		BufferedWriter configWriter = null;
		
		try {
			configWriter = new BufferedWriter(new FileWriter(filePath, true));
			
		} catch (IOException e) {
			Log.errorLog(CONFIG_ERROR + "Could not open file [" + filePath + "] for writing config.");
			return;
		}
		
		try {
			configWriter.write(line + "\n");
			
		} catch (IOException e) {
			Log.errorLog(CONFIG_ERROR + "Could not write config.");
			
		} finally {
			
			if(configWriter != null) {
				try {
					configWriter.close();
					
				} catch (IOException e) {
					Log.errorLog(CONFIG_ERROR + "Failed to properly close config-file");
				}
			}
		}
	}
	
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
				cleanFileReader(configReader);
				
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
						appendLine(TEMP_FILE_NAME, line);
					}
				}

			} else {
				break;
			}
		}
		
		cleanFileReader(configReader);
		
		/*
		 * Finally.. move the temp-file.
		 */
		if(deleteConfig == true) {
			new File(TEMP_FILE_NAME).renameTo(new File(CONFIG_FILE_NAME));
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
		appendLine(CONFIG_FILE_NAME, json);
		
		
		return ReturnCode.SUCCESS;
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
		return ReturnCode.SUCCESS;
	}

}
