package common.apps.mediaStreamer;

import common.instamsg.driver.InitialCallbacks;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.OneToOneResult;


public class Main {
	
	public static void main(String[] args) {
		
		
		InitialCallbacks callbacks = new InitialCallbacks() {

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
				
				InstaMsg.instaMsg.initiateStreaming();
				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode coreLoopyBusinessLogicInitiatedBySelf() {
				return InstaMsg.ReturnCode.SUCCESS;
			}
		};
		
		InstaMsg.start(callbacks, 3);
	}
}
