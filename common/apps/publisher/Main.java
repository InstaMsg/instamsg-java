package common.apps.publisher;

import common.instamsg.driver.InitialCallbacks;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.OneToOneResult;


public class Main {
	
	public static void main(String[] args) {
		
		InitialCallbacks callbacks = new InitialCallbacks() {

			int counter = 0;
			
			@Override
			public InstaMsg.ReturnCode oneToOneMessageHandler(OneToOneResult result) {
				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode onDisconnect() {
				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode onConnectOneTimeOperations() {
				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode coreLoopyBusinessLogicInitiatedBySelf() {

				counter++;				
				return 	InstaMsg.instaMsg.publishMessageWithDeliveryGuarantee("listener_topic", "Test " + counter);
			}
		};
		
		InstaMsg.start(callbacks, 3);
	}
}
