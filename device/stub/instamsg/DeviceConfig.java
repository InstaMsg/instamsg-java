package device.stub.instamsg;

import common.instamsg.driver.Config;
import common.instamsg.driver.InstaMsg;

public class DeviceConfig extends Config {

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
		return null;
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
	public InstaMsg.ReturnCode saveConfigValueOnPersistentStorage(String key, String json) {
		return InstaMsg.ReturnCode.FAILURE;
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
	public InstaMsg.ReturnCode deleteConfigValueFromPersistentStorage(String key) {
		return InstaMsg.ReturnCode.FAILURE;
	}
}
