package common.apps.subscriber;

import common.instamsg.driver.InitialCallbacks;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.InstaMsg.ReturnCode;
import common.instamsg.driver.MessageData;
import common.instamsg.driver.MessageHandler;
import common.instamsg.driver.OneToOneHandler;
import common.instamsg.driver.OneToOneResult;
import common.instamsg.driver.Log;
import common.instamsg.driver.ResultHandler;


public class Main {
	
	public static void main(String[] args) {
		
		InitialCallbacks callbacks = new InitialCallbacks() {

			@Override
			public InstaMsg.ReturnCode oneToOneMessageHandler(OneToOneResult result) {

				if(result.succeeded == true) {
					Log.infoLog("Received [" + result.peerMsg + "] from peer [" + result.peer + "]");
			    	result.reply("Got your response ==> " + result.peerMsg + " :)", new OneToOneHandler() {
						
						@Override
						public ReturnCode oneToOneMessageHandler(OneToOneResult result) {
							Log.infoLog("Received ==> " + result.peerMsg);
							return ReturnCode.SUCCESS;
						}
					}, 3600);
			    
					return ReturnCode.SUCCESS;
				}
				
				return ReturnCode.FAILURE;
			}

			@Override
			public InstaMsg.ReturnCode onDisconnect() {
				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode onConnectOneTimeOperations() {

				return 
				InstaMsg.instaMsg.MQTTSubscribe("listener_topic",
									   			InstaMsg.QOS2,
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
