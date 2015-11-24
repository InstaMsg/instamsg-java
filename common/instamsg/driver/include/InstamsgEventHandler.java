package common.instamsg.driver.include;

public interface InstamsgEventHandler {
	void onConnect();
	void onDisconnect();
	void onOneToOneMessage(OneToOneResult message);
	void coreLoopyBusinessLogicInitiatedBySelf();
	
}
