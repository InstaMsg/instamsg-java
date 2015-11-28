package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;


public interface InitialCallbacks extends OneToOneHandler {

	/**
	 * 
	 * This method is called ONE TIME after the following series of events have happened ::
	 * 
	 * a)
	 * Device has connected to server at socket-level.
	 * 
	 * b)
	 * Device has sent MQTT-CONNECT (successfully) to server.
	 * 
	 * c)
	 * Device has received MQTT-CONNACK (successfully) from server.
	 */
	ReturnCode onConnectOneTimeOperations();
	
	
	
	/**
	 * 
	 * This method is called ONE TIME after the device has (successfully) disconnected from the server.
	 */
	ReturnCode onDisconnect();
	
	
	
	/**
	 * 
	 * This method is called INDEFINITELY at intervals equal to "businessLogicInterval".
	 * This is perhaps the most important method, containing all the business/application logic (built over the InstaMsg-Kernel). 
	 */
	ReturnCode coreLoopyBusinessLogicInitiatedBySelf();
}
