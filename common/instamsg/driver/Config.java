package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

public abstract class Config implements ConfigAPIs {

	public static enum CONFIG_TYPE
	{
	    CONFIG_STRING,
	    CONFIG_INT
	};
	
	public static final String CONFIG                  =  "[CONFIG] ";
	public static final String CONFIG_ERROR            =  "[CONFIG_ERROR] ";
	
	public static final String CONFIG_KEY_KEY          =  "key";
	public static final String CONFIG_TYPE_KEY         =  "type";
	public static final String CONFIG_VALUE_KEY        =  "val";
	public static final String CONFIG_DESCRIPTION_KEY  =  "desc";

	
	static InstaMsg.ReturnCode publishConfig(String topicName, String message)
	{
	    return InstaMsg.instaMsg.publish(topicName,
	                       					 message,
	                       					 InstaMsg.QOS0,
	                       					 false,
	                       					 null,
	                       					 InstaMsg.MQTT_RESULT_HANDLER_TIMEOUT,
	                       					 true);
	}

	public String generateConfigJson(String key, CONFIG_TYPE type, String stringifiedValue, String desc) {
		
		String configMessageToSend = "{'" + CONFIG_KEY_KEY          + "' : '" + key               + "', " +
	                                 " '" + CONFIG_TYPE_KEY         + "' : '" + type.ordinal()    + "', " +
	    		                     " '" + CONFIG_VALUE_KEY        + "' : '" + stringifiedValue + "', " +
	                                 " '" + CONFIG_DESCRIPTION_KEY  + "' : '" + desc              + "'}";
		return configMessageToSend;
	}

	public void processConfig(String configJson) {
		
	    String configKey = Json.getJsonKeyValueIfPresent(configJson, CONFIG_KEY_KEY);

	    /*
	     * Save the config on persistent-storage.
	     */
	    saveConfigValueOnPersistentStorage(configKey, configJson);

	    /*
	     * Finally, publish the config on the server, so that the device and server remain in sync.
	     */
	    InstaMsg.startAndCountdownTimer(1, false);
	    publishConfig(InstaMsg.TOPIC_CONFIG_SEND, configJson);
	}


	public void registerEditableConfig(Object var,
	                        		   String key,
	                            	   CONFIG_TYPE type,
	                            	   String stringifiedValue,
	                            	   String desc) {



	    /*
	     * Check if a config of this key exists in persistent-storage.
	     */
	    String storedConfig = getConfigValueFromPersistentStorage(key);
	    if(storedConfig != null)
	    {
	        /*
	         * Config found on persistent-storage.
	         */
	        stringifiedValue = Json.getJsonKeyValueIfPresent(storedConfig, CONFIG_VALUE_KEY);
	        desc = Json.getJsonKeyValueIfPresent(storedConfig, CONFIG_DESCRIPTION_KEY);

	        Log.infoLog(CONFIG + "Default-config-values overridden by stored-values. Key = [" + key + "], Value = [" +
	                             stringifiedValue + "], Description = [" + desc + "]");
	    }


	    /*
	     * Now, consume the config-value.
	     */
	    if(type == CONFIG_TYPE.CONFIG_STRING)
	    {
	    	((ChangeableString) var).changeTo(stringifiedValue);
	        Log.infoLog(CONFIG + "Using value [" + var + "] for key [" + key + "] of type STRING");
	    }
	    else
	    {
	        ((ChangeableInt) var).changeTo(Integer.parseInt(stringifiedValue));;
	        Log.infoLog(CONFIG + "Using value [" + ((ChangeableInt) var).intValue() + "] for key [" + key + "] of type INTEGER");
	    }


	    /*
	     * Form a Config-JSON, and do the additional-processing required.
	     */
	    String configMessageToSend = generateConfigJson(key, type, stringifiedValue, desc);
	    processConfig(configMessageToSend);
	}

	
	public abstract void initConfig();
	public abstract String getConfigValueFromPersistentStorage(String key);
	public abstract ReturnCode saveConfigValueOnPersistentStorage(String key, String json);
	public abstract ReturnCode deleteConfigValueFromPersistentStorage(String key);
}
