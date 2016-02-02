package common.instamsg.driver;

import common.instamsg.driver.InstaMsg.ReturnCode;

public interface MessagingAPIs {
	
	/**
	 *
	 * topic                            	:
	 *
	 *      Topic on which the message should be published
	 *      FOR SUCCESSFUL PUBLISHING TO A TOPIC, THE TOPIC MUST BE IN THE "Pub Topics" LIST ON INSTAMSG-SERVER.
	 *
	 *
	 * msg                              	:
	 *
	 *      Message-Content
	 *
	 *
	 * qos                                  :
	 *
	 *      One of QOS0, QOS1, QOS2.
	 *      Meanings of these levels as per the spec at http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html
	 *
	 *
	 * dup                                  :
	 *
	 *      "true" or "false".
	 *      Meaning of this variable as per the spec at http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html
	 *
	 *
	 * resultHandler                        :
	 *
	 *      Callback function-pointer.
	 *      Called when the message is published to the server, and the server responds with a PUBACK.
	 *
	 *      No effect for QOS0.
	 *
	 *
	 * resultHandlerTimeout                 :
	 *
	 *      Time for which "resultHandler" remains active.
	 *
	 *      If PUBACK is not received within this interval, "resultHandler" will be removed, and will never be called for the
	 *      message under consideration (even if PUBACK is received at some later stage > "resultHandlerTimeout" seconds).
	 *
	 *
	 * logging                              :
	 *
	 *      "true" or "false".
	 *      Specified whether logging should be done in the client-logger, about the progress of message bein published (or not !!!)
	 *
	 *      Highly recommended to have this value as "true", for easy tracking/debugging.
	 *
	 *
	 *
	 * Returns:
	 *
	 *      SUCCESS   :: If the publish-packet is successfully encoded AND sent over the wire to the InstaMsg-Server.
	 *      FAILURE   :: If any of the above steps fails.
	 *
	 *
	 *      Note that in case of FAILURE,
	 *
	 *      a)
	 *      The application MUST ""NOT"" do any socket-reinitialization (or anything related).
	 *      It will be handled autimatically by the InstaMsg-Driver code (as long as the device-implementor has implemented all
	 *      the InstaMsg-Kernel APIs in accordance with the requirements).
	 *
	 *      b)
	 *      It is the application's duty to re-send the message (if at all required), because there is no guarantee whether
	 *      the message reached the server or not.
	 *
	 *
	 * Kindly see
	 *
	 *                  common/apps/publisher/Main.java
	 *
	 *      for simple (yet complete) example-usage.
	 *
	 */
	public ReturnCode publish(String topic,
			                  String msg,
			                  int qos,
			                  boolean dup,
			                  ResultHandler resultHandler,
			                  int resultHandlerTimeout,
			                  boolean logging);
	
	
	
	/**
	 * A useful-utility function using the "publish" API, that should suffice for most business-applications.
	*/
	public ReturnCode publishMessageWithDeliveryGuarantee(String topic,
														  String payload);

	
	
		/**
	 *
	 * peerClientId                     	:
	 *
	 *      Peer-Id.
	 *
	 *      This value is equal to the client-id of the peer.
	 *      The client-id is generated by the InstaMsg-Server, and so the local-peer must have the exact client-id value of
	 *      the remote-peer.
	 *
	 *      FOR SUCCESSFUL SENDING TO THE PEER, THE PEER CLIENT-ID MUST BE LISTED AS ONE OF THE "Pub Topics" ON INSTAMSG-SERVER.
	 *
	 *
	 * msg                              	:
	 *
	 *      Message-Content
	 *
	 *
	 * replyHandler                      	:
	 *
	 *      Callback function-pointer.
	 *      Called when the remote-peer has sent back a message to the local-peer.
	 *
	 *
	 * replyHandlerTimeout               	:
	 *
	 *      Time for which "oneToOneHandler" remains active.
	 *
	 *      If remote-peer does not respond within this interval, "oneToOneHandler" will be removed, and will never be called
	 *      even if remote-peer sends something at some later stage > "oneToOneHandlerTimeout" seconds).
	 *
	 *
	 *
	 * Returns:
	 *
	 *      SUCCESS   :: If the send-packet is successfully encoded AND sent over the wire to the InstaMsg-Server.
	 *      FAILURE   :: If any of the above steps fails.
	 *
	 *
	 *      Note that in case of FAILURE,
	 *
	 *      a)
	 *      The application MUST ""NOT"" do any socket-reinitialization (or anything related).
	 *      It will be handled autimatically by the InstaMsg-Driver code (as long as the device-implementor has implemented all
	 *      the InstaMsg-Kernel APIs in accordance with the requirements).
	 *
	 *      b)
	 *      It is the application's duty to re-send the message (if at all required), because there is no guarantee whether
	 *      the message reached the server or not.
	 *
	 *
	 * Kindly see
	 *
	 *                  common/apps/oneToOneInitiator/Main.java
	 *                  common/apps/subscriber/Main.java
	 *
	 *      for simple (yet complete) example-usage.
	 *
	 */
	public ReturnCode send(String peerClientId,
            			   String msg,
            			   OneToOneHandler replyHandler,
            			   int replyHandlerTimeout);
	
	
	
	/**
	 *
	 * topic                            	:
	 *
	 *      Topic on which client needs to subscribe.
	 *      FOR SUCCESSFUL SUBSCRIBING TO A TOPIC, THE TOPIC MUST BE IN THE "Sub Topics" LIST ON INSTAMSG-SERVER.
	 *
	 *
	 * qos                                  :
	 *
	 *      One of QOS0, QOS1, QOS2.
	 *      Meanings of these levels as per the spec at http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html
	 *
	 *
	 * messageHandler                       :
	 *
	 *      Callback function-pointer.
	 *      Called whenever a message arrives on the subscribed-topic from InstaMsg-Server.
	 *
	 *
	 * resultHandler                        :
	 *
	 *      Callback function-pointer.
	 *      Called when the message is published to the server, and the server responds with a SUBACK.
	 *
	 *      No effect for QOS0.
	 *
	 *
	 * resultHandlerTimeout                 :
	 *
	 *      Time for which "resultHandler" remains active.
	 *
	 *      If SUBACK is not received within this interval, "resultHandler" will be removed, and will never be called for the
	 *      message under consideration (even if PUBACK is received at some later stage > "resultHandlerTimeout" seconds).
	 *
	 *
	 * logging                              :
	 *
	 *      "true" or "false".
	 *      Specified whether logging should be done in the client-logger, about the progress of message bein published (or not !!!)
	 *
	 *      Highly recommended to have this value as "true", for easy tracking/debugging.
	 *
	 *
	 *
	 * Returns:
	 *
	 *      SUCCESS   :: If the subscription-packet is successfully encoded AND sent over the wire to the InstaMsg-Server.
	 *      FAILURE   :: If any of the above steps fails.
	 *
	 *
	 *      Note that in case of FAILURE,
	 *
	 *      a)
	 *      The application MUST ""NOT"" do any socket-reinitialization (or anything related).
	 *      It will be handled autimatically by the InstaMsg-Driver code (as long as the device-implementor has implemented all
	 *      the InstaMsg-Kernel APIs in accordance with the requirements).
	 *
	 *      b)
	 *      It is the application's duty to re-send the message (if at all required), because there is no guarantee whether
	 *      the message reached the server or not.
	 *
	 *
	 * Kindly see
	 *
	 *                  common/apps/subscriber/Main.java
	 *
	 *      for simple (yet complete) example-usage.
	 *
	 */
	public ReturnCode subscribe(String topic,
							    int qos,
								MessageHandler messageHandler,
								ResultHandler resultHandler,
								int resultHandlerTimeout,
								boolean logging);
	
	
	
	/**
	 * topic                            	:
	 *
	 *      The topic on which the client needs to unsubscribe.
	 *
	 *
	 * Returns:
	 *
	 *      SUCCESS   :: If the unsubscribe-packet is successfully encoded AND sent over the wire to the InstaMsg-Server.
	 *      FAILURE   :: If any of the above steps fails.
	 *
	 *
	 *      Note that in case of FAILURE,
	 *
	 *      a)
	 *      The application MUST ""NOT"" do any socket-reinitialization (or anything related).
	 *      It will be handled autimatically by the InstaMsg-Driver code (as long as the device-implementor has implemented all
	 *      the InstaMsg-Kernel APIs in accordance with the requirements).
	 *
	 *      b)
	 *      It is the application's duty to re-send the message (if at all required), because there is no guarantee whether
	 *      the message reached the server or not.
	 */
	public ReturnCode unsubscribe(String topic);
}
