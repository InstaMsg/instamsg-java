package common.apps.subscriber;

import common.instamsg.driver.InitialCallbacks;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.InstaMsg.ReturnCode;
import common.instamsg.driver.MessageData;
import common.instamsg.driver.MessageHandler;
import common.instamsg.driver.OneToOneResult;
import common.instamsg.driver.InstaMsg.QOS;
import common.instamsg.driver.Log;
import common.instamsg.driver.ResultHandler;


public class Main {
	
	public static void main(String[] args) {
		
		InitialCallbacks callbacks = new InitialCallbacks() {

			@Override
			public InstaMsg.ReturnCode oneToOneMessageReceivedHandler(OneToOneResult oneToOneResult) {
				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode onDisconnect() {
				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode onConnectOneTimeOperations() {

				return 
				InstaMsg.MQTTSubscribe("listener_topic",
									   QOS.QOS2,
									   new MessageHandler() {
										
											@Override
											public void handle(MessageData md) {
												
												Log.infoLog(md.getPayload());
											}
										},
									   new ResultHandler() {

											@Override
											public void handle(int msgId) {
											
												Log.infoLog("SUBACK received for msg-id [" + msgId +"]");
											}
										},
										InstaMsg.MQTT_RESULT_HANDLER_TIMEOUT,
										true);
			
			}

			@Override
			public InstaMsg.ReturnCode coreLoopyBusinessLogicInitiatedBySelf() {
				return ReturnCode.SUCCESS;
			}
		};
		
		InstaMsg.start(callbacks, 1);
	}
}
