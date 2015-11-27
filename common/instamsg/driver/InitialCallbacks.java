package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

public interface InitialCallbacks {

	InstaMsg.ReturnCode onConnectOneTimeOperations();
	InstaMsg.ReturnCode onDisconnect();
	InstaMsg.ReturnCode oneToOneMessageReceivedHandler(OneToOneResult oneToOneResult);
	InstaMsg.ReturnCode coreLoopyBusinessLogicInitiatedBySelf();
}
