package common.apps.publisher;

import common.instamsg.driver.InitialCallbacks;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.OneToOneResult;
import common.instamsg.driver.Log;
import common.instamsg.driver.ResultHandler;


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
				
				return 
				InstaMsg.instaMsg.publish("listener_topic",
									          "Test " + counter,
									          InstaMsg.QOS2,
									          false,
									          new ResultHandler() {

												@Override
												public void handle(int msgId) {
											
													Log.infoLog("PUBACK received for msg-id [" + msgId +"]");
												}
											  },
											  InstaMsg.MQTT_RESULT_HANDLER_TIMEOUT,
											  true);
			}
		};
		
		InstaMsg.start(callbacks, 3);
	}
}
