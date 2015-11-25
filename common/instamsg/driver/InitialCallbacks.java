package common.instamsg.driver;

import common.instamsg.driver.Globals.ReturnCode;
import common.instamsg.driver.include.OneToOneResult;

public interface InitialCallbacks {

	ReturnCode onConnectOneTimeOperations();
	ReturnCode onDisconnect();
	ReturnCode oneToOneMessageReceivedHandler(OneToOneResult oneToOneResult);
}
