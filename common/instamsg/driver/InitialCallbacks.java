package common.instamsg.driver;


public interface InitialCallbacks extends OneToOneHandler {

	InstaMsg.ReturnCode onConnectOneTimeOperations();
	InstaMsg.ReturnCode onDisconnect();
	InstaMsg.ReturnCode coreLoopyBusinessLogicInitiatedBySelf();
}
