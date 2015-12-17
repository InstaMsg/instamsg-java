package common.instamsg.driver;

import common.instamsg.driver.Config.CONFIG_TYPE;

public interface ConfigAPIs {
	
	/**
	 * This method registers a configuration, which is then editable at the InstaMsg-Server/Application-Server.
	 * 
	 * Once done, this configuration will be visible/editable in the "Configuration" tab on the Clients-page
	 * (at the InstaMsg-Server).
	 *
	 * The steps for editing and pushing the changed-configuration from server to device, are detailed in the
	 * "Configuration" tab.
	 *
	 *
	 * Register-Method-Explanation ::
	 * ===============================
	 *
	 *
	 * var                  :
	 *
	 *      Variable, that will store the value.
	 *
	 *		For integer-variable types, "var" must be a reference to "ChangeableInt".
	 *		For string-variable types, "var" must be a reference to "ChangeableString".
	 *
	 *		Both "ChangeableInt.java" and "ChangeableString.java" files are shipped with InstaMsg-Java-client.
	 *
	 *
	 *
	 * key                  :
	 *
	 *      String value, that will serve as the index to this config.
	 *
	 *
	 * type                 :
	 *
	 *      One of CONFIG_STRING or CONFIG_INT.
	 *
	 *
	 * stringifiedValue    :
	 *
	 *      For type of CONFIG_STRING, this will be a simple string.
	 *      For type of CONFIG_INT, this will be a stringified integer value.
	 *
	 *
	 * desc                 :
	 *
	 *      A brief description of what this configuration is for.
	 *
	 *
	 *
	 * Please see "private static void handleConnOrProvAckGeneric(InstaMsg c, int connackRc)" method in
	 *          common/instamsg/driver/Instamsg.java
	 *
	 * for simple example on how to register an editable-configuration.
	 *
	 */
	public void registerEditableConfig(Object var,
 		   							   String key,
 		   							   CONFIG_TYPE type,
 		   							   String stringifiedValue,
 		   							   String desc);
}
