package common.apps.publisher;

import utils.ChangeableString;
import utils.Config;

import common.apps.utils.PublisherSubscriberInit;
import common.instamsg.driver.Instamsg;
import common.instamsg.driver.include.Globals;

public class Main {
	
	public static void main(String[] args) {
		ChangeableString logFilePath = new ChangeableString(null);
		
		if(Config.FILE_SYSTEM_INTERFACE_ENABLED){
			logFilePath.changeTo(Globals.LOG_FILE_PATH);
		}
		
		PublisherSubscriberInit.initPublisherSubscriberParams(args, logFilePath);
		common.instamsg.driver.Globals.globalSystemInit(logFilePath.toString());
		Instamsg instamsg = new Instamsg();
		instamsg.start(null, 3);
	}
}
