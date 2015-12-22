package common.apps.oneToOneInitiator;

import common.instamsg.driver.InitialCallbacks;
import common.instamsg.driver.InstaMsg;
import common.instamsg.driver.InstaMsg.ReturnCode;
import common.instamsg.driver.Log;
import common.instamsg.driver.OneToOneHandler;
import common.instamsg.driver.OneToOneResult;


public class Main {
	
	/**
	 * THESE VARIABLES TO BE CHANGED BY DEVICE-VENDOR, AS PER THE DEVICE BEING IMPLEMENTED.
	 */
	static String remotePeerId = "";
	/**
	 * 
	 */
	
	
	static boolean onceDone = false;
	
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
				
			    if(onceDone == true)
			    {
			        return ReturnCode.SUCCESS;
			    }


			    if(InstaMsg.instaMsg.connected == true)
			    {
			        onceDone = true;

			        if((remotePeerId == null) || (remotePeerId.length() == 0)) {
			        	
			        	Log.errorLog("Remote Peer-Id (as topic) is empty .. MQTT-SEND cannot be sent... rebooting the device");
			        	InstaMsg.misc.rebootDevice();
			        }
			        
			        InstaMsg.instaMsg.send(remotePeerId,
			                          		   "Hi... this is one-to-one initiator !!",
			                          		   new OneToOneHandler() {
										
													@Override
													public ReturnCode oneToOneMessageHandler(OneToOneResult result) {
												
														Log.infoLog("Received [" + result.peerMsg + "] from peer [" + result.peerClientId + "]");
														return ReturnCode.SUCCESS;
													}
											},
											3600);
			    }
			    else
			    {
			    }

				return InstaMsg.ReturnCode.SUCCESS;
			}

			@Override
			public InstaMsg.ReturnCode coreLoopyBusinessLogicInitiatedBySelf() {
				return ReturnCode.SUCCESS;
			}
		};
		
		InstaMsg.start(callbacks, 3);
	}
}
