package common.apps.publisher;

import common.instamsg.driver.Globals.ReturnCode;
import common.instamsg.driver.InstaMsg.QOS;
import common.instamsg.driver.InitialCallbacks;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.ResultHandler;
import common.instamsg.driver.include.Log;
import common.instamsg.driver.include.OneToOneResult;


public class Main {
	
	public static void main(String[] args) {
		
		InitialCallbacks callbacks = new InitialCallbacks() {

			int counter = 0;
			
			@Override
			public ReturnCode oneToOneMessageReceivedHandler(OneToOneResult oneToOneResult) {
				return ReturnCode.SUCCESS;
			}

			@Override
			public ReturnCode onDisconnect() {
				return ReturnCode.SUCCESS;
			}

			@Override
			public ReturnCode onConnectOneTimeOperations() {
				return ReturnCode.SUCCESS;
			}

			@Override
			public ReturnCode coreLoopyBusinessLogicInitiatedBySelf() {

				counter++;
				
				return 
				InstaMsg.MQTTPublish("listener_topic",
									 "Hi.. Ajay testing java-client " + counter,
									 QOS.QOS2,
									 false,
									 new ResultHandler() {

										@Override
										public void handle(int msgId) {
											
											Log.infoLog("PUBACK received for msg-id [" + msgId +"]");
										}
									},
									InstaMsg.MQTT_RESULT_HANDLER_TIMEOUT,
									false,
									true);
			}
		};
		
		InstaMsg.start(callbacks, 3);
	}
}
