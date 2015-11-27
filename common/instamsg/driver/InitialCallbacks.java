package common.instamsg.driver;


public interface InitialCallbacks {

	InstaMsg.ReturnCode onConnectOneTimeOperations();
	InstaMsg.ReturnCode onDisconnect();
	InstaMsg.ReturnCode oneToOneMessageReceivedHandler(OneToOneResult oneToOneResult);
	InstaMsg.ReturnCode coreLoopyBusinessLogicInitiatedBySelf();
}
