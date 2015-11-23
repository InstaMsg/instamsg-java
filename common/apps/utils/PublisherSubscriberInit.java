package common.apps.utils;

import utils.ChangeableString;
import utils.Config;
import common.instamsg.driver.include.Globals;

public class PublisherSubscriberInit {
	
	public static String TOPIC = null;
	
	public static void initPublisherSubscriberParams(String[] argv, ChangeableString logFilePath){
		
		TOPIC = "listener_topic";

		if(Config.DEBUG_MODE){
			if(argv.length >= 2){
				TOPIC = argv[1];
		    }
	
		    if(argv.length >= 3){
		    	Globals.USER_LOG_FILE_PATH = argv[2];
		    	logFilePath.changeTo(Globals.USER_LOG_FILE_PATH);
		    }
	
		    if(argv.length >= 4){
		    	Globals.USER_DEVICE_UUID = argv[3];
		    }
		}
	}
}
