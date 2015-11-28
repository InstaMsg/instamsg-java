package common.instamsg.driver;

import common.instamsg.driver.Config.CONFIG_TYPE;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttException;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttMessage;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPingReq;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttProvack;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRec;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttSuback;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;


@SuppressWarnings("unused")
public class InstaMsg implements MessagingAPIs {
	
	/**
	 * THESE VARIABLES TO BE CHANGED BY DEVICE-VENDOR, AS PER THE DEVICE BEING IMPLEMENTED.
	 */
	public static final String DEVICE_NAME     =   "";
	public static final int LOG_LEVEL          =   Log.INSTAMSG_LOG_LEVEL_INFO;
	public static final boolean SSL_SOCKET     =   false;
	public static final boolean GSM_DEVICE     =   false;
	/**
	 *
	 */	


	
	public static enum QOS {
		QOS0,
		QOS1,
		QOS2
	}
	
	public static enum ReturnCode
	{
	    SOCKET_READ_TIMEOUT,
	    BUFFER_OVERFLOW,
	    FAILURE,
	    SUCCESS
	}

	public static InstaMsg instaMsg;
	
	static int MAX_MESSAGE_HANDLERS = 5;
	static int MAX_PACKET_ID = 10000;
	static String NO_CLIENT_ID = "NONE";
	
	public static int MQTT_RESULT_HANDLER_TIMEOUT = 10;	

	public static ModulesProviderInterface modulesProvideInterface;
	public static common.instamsg.driver.Config config;
	public static Time time;
	public static Log logger;
	public static Misc misc;
	public static Watchdog watchdog;
	public Socket socket;

	
	static final String TOPIC_METADATA      =   "instamsg/client/metadata";
	static final String TOPIC_SESSION_DATA  =   "instamsg/client/session";
	static final String TOPIC_NETWORK_DATA  =   "instamsg/client/signalinfo";
	static final String TOPIC_CONFIG_SEND   =  "instamsg/client/config/clientToServer";
	
	public static final int MAX_BUFFER_SIZE    =   1000;
	
	MessageHandlers[] messageHandlers = new MessageHandlers[MAX_MESSAGE_HANDLERS];
	ResultHandlers[] resultHandlers = new ResultHandlers[MAX_MESSAGE_HANDLERS];
	OneToOneHandlers[] oneToOneHandlers = new OneToOneHandlers[MAX_MESSAGE_HANDLERS];

	int nextPackedId;

	InitialCallbacks initialCallbacks;
	boolean serverLoggingEnabled;
	
	MqttConnectOptions connectOptions = new MqttConnectOptions();
	
	String clientIdComplete;
	String clientIdMachine;
	String username;
	String password;
	
	
	public boolean connected = false;
	
	int connectionAttempts = 0;
	
	byte[] readBuf = new byte[InstaMsg.MAX_BUFFER_SIZE];

	private String filesTopic;
	private String rebootTopic;
	private String serverLogsTopic;
	private String enableServerLoggingTopic;
	private String fileUploadUrl;
	private String receiveConfigTopic;
	
	private static ResultHandler pubCompResultHandler;
	
	ChangeableInt pingRequestInterval = new ChangeableInt(0);
	ChangeableInt compulsorySocketReadAfterMQTTPublishInterval = new ChangeableInt(0);
	
	int publishCount = 0;
	private InitialCallbacks callbacks;
	
	static String ONE_TO_ONE = "[ONE-TO-ONE] ";


	public static int NETWORK_INFO_INTERVAL = 300;
	public static int MAX_CONN_ATTEMPTS_WITH_PHYSICAL_LAYER_FINE = 5;
	public static int MAX_TRIES_ALLOWED_WHILE_READING_FROM_SOCKET_MEDIUM = 1;
	public static int SOCKET_READ_TIMEOUT_SECS = 1;
	
	public static int INSTAMSG_PORT;
	public static String INSTAMSG_HOST;
	


	
	
	static {
		pubCompResultHandler = new ResultHandler() {
			
			@Override
			public void handle(int msgId) {
				
				Log.infoLog("PUBCOMP received for msg-id [" + msgId + "]");
			}
		};
		
		modulesProvideInterface = ModulesProviderFactory.getModulesProvider(InstaMsg.DEVICE_NAME);
		
		config = modulesProvideInterface.getConfig();
		config.initConfig();
		
		time = modulesProvideInterface.getTime();
		time.initGlobalTimer();
		
		logger = modulesProvideInterface.getLogger();
		logger.initLogger();
		
		misc = modulesProvideInterface.getMisc();

		watchdog = modulesProvideInterface.getWatchdog();
		watchdog.watchdogInit();
		
		
		INSTAMSG_HOST = "platform.instamsg.io";
		if(SSL_SOCKET == true) {
			INSTAMSG_PORT = 8883;
		} else {
			INSTAMSG_PORT = 1883;
		}
	}
	
	
	public static int getNextPackedId(InstaMsg c) {
		
		if(c.nextPackedId == MAX_PACKET_ID) {
			c.nextPackedId = 1;
		} else {
			c.nextPackedId++;
		}
		
		return c.nextPackedId;
	}
	
	
	public static ReturnCode doMqttSendPublish(String peer, String message) {
		return instaMsg.MQTTPublish(peer,
                  		            message,
                		            QOS.QOS2,
                		            false,
                		            new ResultHandler() {
			
										@Override
										public void handle(int msgId) {
											Log.infoLog("[DEFAULT-PUBLISH-HANDLER] PUBACK received for msg-id [" + msgId + "]");
									
										}
									},
									InstaMsg.MQTT_RESULT_HANDLER_TIMEOUT,
									false,
									true);
	}
	
	
	private static void attachResultHandler(InstaMsg c, int msgId, int timeout, ResultHandler resultHandler)
	{
	    if(resultHandler == null)
	    {
	        return;
	    }

	    for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
	    {
	        if (c.resultHandlers[i].msgId == 0)
	        {
	            c.resultHandlers[i].msgId = msgId;
	            c.resultHandlers[i].timeout = timeout;
	            c.resultHandlers[i].resultHandler = resultHandler;

	            break;
	        }
	    }
	}
	
	private static void attachOneToOneHandler(InstaMsg c, int msgId, int timeout, OneToOneHandler oneToOneHandler) {
		
		if(oneToOneHandler == null) {
			return;
		}


		for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i) {
			
			if (c.oneToOneHandlers[i].msgId == 0) {
				c.oneToOneHandlers[i].msgId = msgId;
				c.oneToOneHandlers[i].timeout = timeout;
				c.oneToOneHandlers[i].oneToOneHandler = oneToOneHandler;

				break;
			}
		}
	}
	
	
	private static void fireResultHandlerUsingMsgIdAsTheKey(InstaMsg c, int msgId)
	{       
		for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
		{
			if (c.resultHandlers[i].msgId == msgId)
			{
				c.resultHandlers[i].resultHandler.handle(msgId);
				c.resultHandlers[i].msgId = 0;

				break;
			}
		}
	}
	
	
	private static ReturnCode fireOneToOneHandlerUsingMsgIdAsTheKey(InstaMsg c, int msgId, OneToOneResult result)
	{
	    for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
	    {
	        if (c.oneToOneHandlers[i].msgId == msgId)
	        {
	            c.oneToOneHandlers[i].oneToOneHandler.oneToOneMessageHandler(result);
	            c.oneToOneHandlers[i].msgId = 0;

	            return ReturnCode.SUCCESS;
	        }
	    }

	    return ReturnCode.FAILURE;
	}

	
	private static byte[] getEncodedMqttMessageAsByteStream(MqttWireMessage message) {
		
		byte[] bytes;
		try {
			bytes = message.getHeader();
			
		} catch (MqttException e) {
			Log.errorLog("Could not fetch header from message.");
			return null;
		}
		
		byte[] pl;
		try {
			pl = message.getPayload();
			
		} catch (MqttException e) {
			Log.errorLog("Could not fetch payload from message");
			return null;
		}
		
		byte[] result = new byte[bytes.length + pl.length];
		
		int index = 0;
		for(int j = 0; j < bytes.length; j++) {
			result[index] = bytes[j];
			index++;
		}
		for(int j = 0; j < pl.length; j++) {
			result[index] = pl[j];
			index++;
		}
		
		return result;      
	}
	
	
	private static InstaMsg.ReturnCode sendPacket(InstaMsg c, byte[] packet) {
		
		if(c.socket.socketCorrupted == true) {
			Log.errorLog("Socket not available at physical layer .. so packet cannot be sent to server.");
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		if(c.socket.socketWrite(packet, packet.length) == InstaMsg.ReturnCode.FAILURE) {
			c.socket.socketCorrupted = true;
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		return InstaMsg.ReturnCode.SUCCESS;
	}
	
	
	private static int encodeLength(byte[] buffer, int length) {
	    int rc = 0;

	    do
	    {
	        byte d = (byte) (length % 128);
	        length /= 128;
	        
	        /* if there are more digits to encode, set the top bit of this digit */
	        if (length > 0) {
	            d |= 0x80;
	        }
	        
	        buffer[1 + rc] = d;
	        rc++;
	        
	    } while (length > 0);
	    
	    return rc;
	}
	
	
	private static void fillFixedHeaderFieldsFromPacketHeader(MQTTFixedHeader fixedHeader, byte packetHeader) {
		//System.out.println(String.format("0x%02X", packetHeader));
		fixedHeader.packetType = (byte) ((packetHeader >> 4) & 0x0F);
		//System.out.println(String.format("0x%02X", fixedHeader.packetType));
	}
	
	
	private static InstaMsg.ReturnCode readPacket(InstaMsg c, MQTTFixedHeader fixedHeader) {
		
		if(c.socket.socketCorrupted == true) {
			
			Log.errorLog("Socket not available at physical layer .. so packet cannot be read from server.");
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		c.readBuf = new byte[InstaMsg.MAX_BUFFER_SIZE];
		
	    /*
	     * 1. read the header byte.  This has the packet type in it.
	     */
		int numRetries = InstaMsg.MAX_TRIES_ALLOWED_WHILE_READING_FROM_SOCKET_MEDIUM;
		InstaMsg.ReturnCode rc = InstaMsg.ReturnCode.FAILURE;
	    do
	    {
	        rc = c.socket.socketRead(c.readBuf, 1, false);
	        if(rc == InstaMsg.ReturnCode.FAILURE)
	        {
	            c.socket.socketCorrupted = true;
	            return InstaMsg.ReturnCode.FAILURE;
	        }

	        if(rc == InstaMsg.ReturnCode.SOCKET_READ_TIMEOUT)
	        {
	            numRetries--;
	        }
	    } while((rc == InstaMsg.ReturnCode.SOCKET_READ_TIMEOUT) && (numRetries > 0));	    
	    
	    
	    /*
	     * If at this point, we still had a socket-timeout, it means we really have nothing to read.
	     */
	    if(rc == InstaMsg.ReturnCode.SOCKET_READ_TIMEOUT)
	    {
	        return InstaMsg.ReturnCode.SOCKET_READ_TIMEOUT;
	    }

	    
	    int len = 1;
	    
	    /*
	     *  2. read the remaining length.  This is variable in itself
	     */
	    int rem_len = 0;
	    int multiplier = 1;
	    byte[] i = new byte[1];
	    do
	    {
	    	if(c.socket.socketRead(i, 1, true) == InstaMsg.ReturnCode.FAILURE)
	        {
	            c.socket.socketCorrupted = true;
	            return InstaMsg.ReturnCode.FAILURE;
	        }

	        rem_len += (i[0] & 127) * multiplier;
	        multiplier *= 128;
	    } while ((i[0] & 128) != 0);
	    
	    
	    
	    len += encodeLength(c.readBuf, rem_len);
	

	    /*
	     *  3. read the rest of the buffer 
	     */
	    if(rem_len > 0)
	    {
		    byte[] remainingBytes = new byte[rem_len];

	    	if(c.socket.socketRead(remainingBytes, rem_len, true) == InstaMsg.ReturnCode.FAILURE)
	        {
	            c.socket.socketCorrupted = true;
	            return InstaMsg.ReturnCode.FAILURE;
	        }
	    	
	    	for(int j = 0; j < remainingBytes.length; j++) {
	    		c.readBuf[len + j] = remainingBytes[j];
	    	}
	    }

	    fillFixedHeaderFieldsFromPacketHeader(fixedHeader, c.readBuf[0]);
	    return InstaMsg.ReturnCode.SUCCESS;
	}
	
	
	private static void sendClientData(String data, String topicName) {
		
		/*
		 * This method sends the data upon client's connect.
		 *
		 * If the message(s) are not sent from this method, that means that the connection is not (fully) completed.
		 * Thus, the InstaMsg-Driver code will try again for the connection, and then these messages will be sent (again).
		 *
		 * Bottom-line : We do not need to re-attempt the message(s) sent by this method.
		 */

		if((data != null) && (data.length() > 0)) {			
			instaMsg.MQTTPublish(topicName,
					             data,
					             QOS.QOS1,
					             false,
					             null,
					             MQTT_RESULT_HANDLER_TIMEOUT,
					             false,
					             true);
			
		} else {			
			Log.infoLog("Not publishing empty-message to topic [" + topicName + "]");

		}
	}
	

	private static void handleConnOrProvAckGeneric(InstaMsg c, int connackRc)
	{
	    if(connackRc == 0x00)  /* Connection Accepted */
	    {
	        Log.infoLog("\n\nConnected successfully to InstaMsg-Server.\n\n");
	        c.connected = true;
	        
	        
	        sendClientData(misc.getClientSessionData(), TOPIC_SESSION_DATA);
	        sendClientData(misc.getClientMetadata(), TOPIC_METADATA);
	        sendClientData(misc.getNetworkData(), TOPIC_NETWORK_DATA);
		
	        
	        config.registerEditableConfig(c.pingRequestInterval,
	        		                      "PING_REQ_INTERVAL",
                                          CONFIG_TYPE.CONFIG_INT,
                                          "180",
                                          "Keep-Alive Interval between Device and InstaMsg-Server");
	        

	        config.registerEditableConfig(c.compulsorySocketReadAfterMQTTPublishInterval,
	                                      "COMPULSORY_SOCKET_READ_AFTER_MQTT_PUBLISH_INTERVAL",
	                                      CONFIG_TYPE.CONFIG_INT,
	                                      "3",
	                                      "This variable controls after how many MQTT-Publishes a compulsory socket-read is done. " +
	                                      "This prevents any socket-pverrun errors (particularly in hardcore embedded-devices");


	        c.callbacks.onConnectOneTimeOperations();
	    }
	    else
	    {
	        Log.infoLog("Client-Connection failed with code [" + connackRc + "]");
	    }
	}


	static void removeExpiredResultHandlers(InstaMsg c)
	{
	    for (int i = 0; i < MAX_MESSAGE_HANDLERS; i++)
	    {
	    	if(c.resultHandlers[i].msgId == 0) {
	    		continue;
	    	}
	    	
	    	if(c.resultHandlers[i].timeout < 0) {
	    		Log.infoLog("No pub/sub response received for msgid [" + c.resultHandlers[i].msgId + "], removing..");
	    		c.resultHandlers[i].msgId = 0;
	    	}
	    	else {
	    		c.resultHandlers[i].timeout = c.resultHandlers[i].timeout - 1;
	    	}
	    }
	}
	
	
	static void removeExpiredOneToOneHandlers(InstaMsg c)
	{
	    for (int i = 0; i < MAX_MESSAGE_HANDLERS; i++)
	    {
	    	if(c.oneToOneHandlers[i].msgId == 0) {
	    		continue;
	    	}
	    	
	    	if(c.oneToOneHandlers[i].timeout < 0) {
	    		Log.infoLog("No one-to-one response received for msgid [" + c.oneToOneHandlers[i].msgId + "], removing..");
	    		c.oneToOneHandlers[i].msgId = 0;
	    	}
	    	else {
	    		c.oneToOneHandlers[i].timeout = c.oneToOneHandlers[i].timeout - 1;
	    	}
	    }
	}

	
	private static void sendPingReqToServer(InstaMsg c)
	{
		if(c.socket.socketCorrupted == true) {
			
			Log.errorLog("Socket not available at physical layer .. so server cannot be pinged for maintaining keep-alive.");
			return;
		}
		
		MqttPingReq pingReqMsg = new MqttPingReq();
		
		byte[] packet = getEncodedMqttMessageAsByteStream(pingReqMsg);
		if(packet != null) {
			sendPacket(c, packet);
		}
	}

	
	private static void setValuesOfSpecialTopics(InstaMsg c)
	{
	    c.filesTopic               = "instamsg/clients/" + c.clientIdComplete + "/files";
	    c.rebootTopic              = "instamsg/clients/" + c.clientIdComplete + "/reboot";
	    c.enableServerLoggingTopic = "instamsg/clients/" + c.clientIdComplete + "/enableServerLogging";
	    c.serverLogsTopic          = "instamsg/clients/" + c.clientIdComplete + "/logs";
	    c.receiveConfigTopic       = "instamsg/clients/" + c.clientIdComplete + "/config/serverToClient";
	    c.fileUploadUrl            = "/api/beta/clients/" + c.clientIdComplete + "/files";


	    Log.infoLog("\nThe special-topics value :: \n");
	    Log.infoLog("FILES_TOPIC = [" + c.filesTopic + "]");
	    Log.infoLog("REBOOT_TOPIC = [" + c.rebootTopic + "]");
	    Log.infoLog("ENABLE_SERVER_LOGGING_TOPIC = [" + c.enableServerLoggingTopic + "]");
	    Log.infoLog("SERVER_LOGS_TOPIC = [" + c.serverLogsTopic + "]");
	    Log.infoLog("FILE_UPLOAD_URL = [" + c.fileUploadUrl + "]");
	    Log.infoLog("CONFIG_FROM_SERVER_TO_CLIENT = [" + c.receiveConfigTopic + "]");
	}


	private static ReturnCode deliverMessageToSelf(InstaMsg c, MqttPublish pubMsg) {
		
		String topicName = pubMsg.getTopicName();		
		for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
		{
			if (topicName.equals(c.messageHandlers[i].topicFilter))
			{
				try {
					c.messageHandlers[i].messageHandler.handle(new MessageData(topicName, new String(pubMsg.getPayload())));
					
				} catch (MqttException e) {
					Log.errorLog("Error occurred while handling PUBLISH message for topic [" + topicName + "]");
				}
				
				break;
			}
		}
		
		/*
		 * Also, send ack if applicable.
		 */
		byte[] packet = null;
		
		int receivedMsgQos = pubMsg.getMessage().getQos();
		if(receivedMsgQos == QOS.QOS1.ordinal()) {
			packet = getEncodedMqttMessageAsByteStream(new MqttPubAck(pubMsg));
			
		} else {
			packet = getEncodedMqttMessageAsByteStream(new MqttPubRec(pubMsg));
		}
		
		if(packet == null) {
			return ReturnCode.FAILURE;
		}
		
		return sendPacket(c, packet);
	}
	
	
	private static void readAndProcessIncomingMQTTPacketsIfAny(InstaMsg c) {
		InstaMsg.ReturnCode rc = InstaMsg.ReturnCode.FAILURE;
		
		do {
			
			MQTTFixedHeader fixedHeader = new MQTTFixedHeader();
			
			rc = readPacket(c, fixedHeader);
			if(rc != InstaMsg.ReturnCode.SUCCESS) {
				return;
			}
			
			if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PROVACK) {
				
				try {
					MqttProvack msg = (MqttProvack) MqttWireMessage.createWireMessage(c.readBuf);
					if(msg.getReturnCode() == 0) {
						/*
						 * Connection was established successfully;
						 */
						c.clientIdComplete = msg.getClientId();
						Log.infoLog("Received client-id from server via PROVACK [" + c.clientIdComplete + "]");
						
						setValuesOfSpecialTopics(c);
						handleConnOrProvAckGeneric(c, msg.getReturnCode());
					}
					
				} catch (MqttException e) {					
					rc = handleMessageDecodingFailure(c, "MQTT-PROVACK");
				}

			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PUBREC) {
				
				try {
					MqttPubRec pubRecMsg = (MqttPubRec) MqttWireMessage.createWireMessage(c.readBuf);
					fireResultHandlerUsingMsgIdAsTheKey(instaMsg, pubRecMsg.getMessageId());
					
					attachResultHandler(instaMsg, pubRecMsg.getMessageId(), MQTT_RESULT_HANDLER_TIMEOUT, pubCompResultHandler);

					MqttPubRel pubRelMsg = new MqttPubRel(pubRecMsg);
					byte[] pubRelPacket = getEncodedMqttMessageAsByteStream(pubRelMsg);
					if(pubRelPacket != null) {
						sendPacket(instaMsg, pubRelPacket);
					}
					
				} catch (MqttException e) {
					rc = handleMessageDecodingFailure(c, "MQTT-PUBREC");
				}
				
			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PUBCOMP) {
				
				try {
					MqttPubComp pubCompMsg = (MqttPubComp) MqttWireMessage.createWireMessage(c.readBuf);
					fireResultHandlerUsingMsgIdAsTheKey(instaMsg, pubCompMsg.getMessageId());
					
				} catch (MqttException e) {
					rc = handleMessageDecodingFailure(c, "MQTT-PUBCOMP");
				}
				
			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PUBLISH) {
				
				try {
					MqttPublish pubMsg = (MqttPublish) MqttWireMessage.createWireMessage(c.readBuf);
					
					String topicName = pubMsg.getTopicName();
					if(topicName.equals(c.rebootTopic)) {
						Log.infoLog("Received REBOOT request from server.. rebooting !!!");
						misc.rebootDevice();
						
					} else if(topicName.equals(c.clientIdComplete)) {
						oneToOneMessageArrived(c, new String(pubMsg.getPayload()));
						
					} else if(topicName.equals(c.receiveConfigTopic)) {                    	
                        handleConfigReceived(c, new String(pubMsg.getPayload()));
                        
                    } else {
						deliverMessageToSelf(c, pubMsg);

					}
					
				} catch (MqttException e) {
					rc = handleMessageDecodingFailure(c, "MQTT-PUBLISH");
				}

			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PINGRESP) {
				
				Log.infoLog("PINGRESP received... relations are intact !!\n");				
				
			} else if (fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_SUBACK) {
				
				try {
					MqttSuback subAckMsg = (MqttSuback) MqttWireMessage.createWireMessage(c.readBuf);
					fireResultHandlerUsingMsgIdAsTheKey(c, subAckMsg.getMessageId());
					
					/*
					 * TODO: handle this case.. present in C.
	                if (subAckMsg. == 0x80)
	                {
	                    for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
	                    {
	                        if (c.messageHandlers[i].msgId == subAckMsg.getMessageId())
	                        {
	                            c.messageHandlers[i].topicFilter = null;
	                            break;
	                        }
	                    }
	                }
	                */

					
				} catch (MqttException e) {
					rc = handleMessageDecodingFailure(c, "MQTT-PUBCOMP");
		
				}
			
			} else {
				
			}
		
		} while (rc == InstaMsg.ReturnCode.SUCCESS);		
		
	}

	
	private static void oneToOneMessageArrived(InstaMsg c, String payload) {
		
		Log.infoLog(ONE_TO_ONE + " Payload == [" + payload + "s]");
		
		String peerMessage = Json.getJsonKeyValueIfPresent(payload, "body");
		String peer = Json.getJsonKeyValueIfPresent(payload, "reply_to");
		String peerMsgId = Json.getJsonKeyValueIfPresent(payload, "message_id");
		String responseMsgId = Json.getJsonKeyValueIfPresent(payload, "response_id");

		if(peerMsgId.length() == 0) {
			Log.errorLog(ONE_TO_ONE + "Peer-Message-Id not received ... not proceeding further");
			return;
		}
		
		if(peer.length() == 0) {
			Log.errorLog(ONE_TO_ONE + "Peer-value not received ... not proceeding further");
			return;
		}
		
		OneToOneResult oneToOneResult = new OneToOneResult(peer, Integer.parseInt(peerMsgId), true, peerMessage);
		Log.debugLog(ONE_TO_ONE + "Peer-Message = ["    + oneToOneResult.peerMsg   + "], " +
		                          "Peer = ["            + oneToOneResult.peer      + "], " +
				                  "Peer-Message-Id = [" + oneToOneResult.peerMsgId + "]");
		
		if(responseMsgId.length() == 0) {
			
			/*
		     * This is a fresh message, so use the global callback.
		     */
			c.callbacks.oneToOneMessageHandler(oneToOneResult);
			
		} else {
			
			/*
		     * This is for an already exisiting message, that was sent by the current-client to the peer.
		     * Call its handler (if at all it exists).
		     */
			if(fireOneToOneHandlerUsingMsgIdAsTheKey(c, Integer.parseInt(responseMsgId), oneToOneResult) == ReturnCode.FAILURE) {
				
				Log.errorLog(ONE_TO_ONE + "No handler found for one-to-one for message-id [" + responseMsgId + "]");
		    }
		}
	}
	
	
	private static void handleConfigReceived(InstaMsg c, String payload) {
		
		Log.infoLog(common.instamsg.driver.Config.CONFIG + "Received the config-payload [" + payload + "] from server");
		config.processConfig(payload);
	}


	private static InstaMsg.ReturnCode handleMessageDecodingFailure(InstaMsg c, String messageType) {
		InstaMsg.ReturnCode rc;
		Log.errorLog("Error occurred while decoding " + messageType + " message");
		
		c.socket.socketCorrupted = true;
		rc = InstaMsg.ReturnCode.FAILURE;
		return rc;
	}
	
	
	public InstaMsg.ReturnCode MQTTPublish(String topicName,
			 							   String payload,
										   InstaMsg.QOS qos,
										   boolean dup,
										   ResultHandler resultHandler,
										   int resultHandlerTimeout,
										   boolean retain,
										   boolean logging) {
		
		MqttMessage baseMessage = new MqttMessage();
		baseMessage.setPayload(payload.getBytes());
		baseMessage.setQos(qos.ordinal());
		baseMessage.setDuplicate(dup);
		baseMessage.setRetained(retain);
		
		MqttPublish pubMsg = new MqttPublish(topicName, baseMessage);
		if((qos == InstaMsg.QOS.QOS1) || (qos == InstaMsg.QOS.QOS2))
		{
			int msgId = getNextPackedId(instaMsg);
			
			pubMsg.setMessageId(msgId);
			attachResultHandler(instaMsg, msgId, resultHandlerTimeout, resultHandler);
		}
		
		if(logging == true) {
			Log.infoLog("Publishing message [" + payload + "] to topic [" + topicName + "]");
		}
		
		byte[] packet = getEncodedMqttMessageAsByteStream(pubMsg);
		if(packet == null) {
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		InstaMsg.ReturnCode rc = sendPacket(instaMsg, packet);	
		if(rc == InstaMsg.ReturnCode.SUCCESS) {
			instaMsg.publishCount++;
		}
		
	    if(logging == true)
	    {
	        if(rc == InstaMsg.ReturnCode.SUCCESS)
	        {
	            Log.infoLog("Published successfully.\n");

	            if(instaMsg.compulsorySocketReadAfterMQTTPublishInterval.intValue() != 0)
	            {
	                if((instaMsg.publishCount % instaMsg.compulsorySocketReadAfterMQTTPublishInterval.intValue()) == 0)
	                {
	                    Log.infoLog("Doing out-of-order socket-read, as [" + 
	                                instaMsg.compulsorySocketReadAfterMQTTPublishInterval.intValue() + "] " +
	                    		    " MQTT-Publishes have been done");

	                    readAndProcessIncomingMQTTPacketsIfAny(instaMsg);
	                }
	            }
	        }
	        else
	        {
	            Log.infoLog("Publishing failed, error-code = [" + rc.ordinal() + "]\n");
	        }
	    }
	    
	    return rc;
	}
	
	
	public ReturnCode MQTTSubscribe(String topicName,
									QOS qos,
									MessageHandler messageHandler,
									ResultHandler resultHandler,
									int resultHandlerTimeout,
									boolean logging) {
		
		InstaMsg c = instaMsg;
		
		ReturnCode rc = ReturnCode.FAILURE;
		
		if(logging == true) {
			Log.infoLog("Subscribing to topic [" + topicName + "]");
		}

		int msgId = getNextPackedId(instaMsg);
		attachResultHandler(instaMsg, msgId, resultHandlerTimeout, resultHandler);

		
		/*
		 * We follow optimistic approach, and assume that the subscription will be successful, and accordingly assign the
		 * message-handlers.
		 *
		 * If the subscription is unsuccessful, we would then remove/unsubscribe the topic.
		 */
		for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
		{
			if (c.messageHandlers[i].topicFilter == null)
			{
				c.messageHandlers[i].msgId = msgId;
				c.messageHandlers[i].topicFilter = topicName;
				c.messageHandlers[i].messageHandler = messageHandler;

				break;
			}
		}
   
		String[] topicNames = new String[1];
		int[] qosValues = new int[1];		
		topicNames[0] = topicName;
		qosValues[0] = qos.ordinal();
		
		MqttSubscribe subMsg = new MqttSubscribe(topicNames, qosValues);
		subMsg.setMessageId(msgId);
		
		byte[] packet = getEncodedMqttMessageAsByteStream(subMsg);
		if(packet == null) {
			rc = ReturnCode.FAILURE;
		}
		
		if(packet != null) {
			rc = sendPacket(c, packet);
		}	
		
		if(logging == true) {
			
			if(rc == ReturnCode.SUCCESS) {				
				Log.infoLog("Subscribed successfully.\n");
				
			} else {
				Log.infoLog("Subscribing failed, error-code = [" + rc.ordinal() + "]\n");
			}
		}
		
		return rc;
	}

	
	public ReturnCode MQTTSend(String peer,
			                   String payload,
			                   OneToOneHandler oneToOneHandler,
			                   int timeout) {
		
		InstaMsg c = instaMsg;
		int id = getNextPackedId(c);

		String message = "{\"message_id\": \""  + id + 
				         "\", \"reply_to\": \"" + c.clientIdComplete +
				         "\", \"body\": \""     + payload + "\"}";

  		attachOneToOneHandler(c, id, timeout, oneToOneHandler);
  		return doMqttSendPublish(peer, message);
	}
	

	public static InstaMsg.ReturnCode MQTTConnect(InstaMsg c) {
		
		MqttConnect connectMsg = new MqttConnect(c.connectOptions);
		
		byte[] packet = getEncodedMqttMessageAsByteStream(connectMsg);
		if(packet == null) {
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		return sendPacket(c, packet);
	}	
	
	
	/*
	 * This method causes the current thread to wait for "n" seconds.
	 */
	public static void startAndCountdownTimer(int seconds, boolean showRunningStatus)
	{
	    int i;
	    long j;
	    long cycles = 1000000 / time.getMinimumDelayPossibleInMicroSeconds();
	
	    for(i = 0; i < seconds; i++)
	    {
	        if(showRunningStatus == true)
	        {
	        	Log.infoLog(seconds - 1 + "");
	        }
	
	        for(j = 0; j < cycles; j++)
	        {
	            time.minimumDelay();
	        }
	    }
	}
	

	public static void clearInstaMsg(InstaMsg c) {
		Log.infoLog("CLEARING INSTAMSG !!!!");
		
		c.socket.releaseSocket();		
		c.connected = false;
	}
	

	public static void initInstaMsg(InstaMsg c, InitialCallbacks callbacks) {
		
		c.socket = modulesProvideInterface.getSocket(InstaMsg.INSTAMSG_HOST, InstaMsg.INSTAMSG_PORT);		
		c.socket.socketCorrupted = true;
		
		c.socket.initSocket();
		if(c.socket.socketCorrupted == true) {
			return;
		}


		for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
		{
			c.messageHandlers[i] = new MessageHandlers();			
			c.messageHandlers[i].msgId = 0;
			c.messageHandlers[i].topicFilter = null;

			c.resultHandlers[i] = new ResultHandlers();
			c.resultHandlers[i].msgId = 0;
			c.resultHandlers[i].timeout = 0;

			c.oneToOneHandlers[i] = new OneToOneHandlers();
			c.oneToOneHandlers[i].msgId = 0;
			c.oneToOneHandlers[i].timeout = 0;
		}


		c.nextPackedId = MAX_PACKET_ID;		    
		c.initialCallbacks = callbacks;
		c.serverLoggingEnabled = false;

		c.connectOptions.setMqttVersion(3);
		c.connectOptions.setCleanSession(true);
		

		c.clientIdComplete = "";

		c.clientIdMachine = NO_CLIENT_ID;
		c.connectOptions.setClientId(c.clientIdMachine);

		c.username = "";
		c.connectOptions.setUserName(c.username);

		c.password = modulesProvideInterface.getMisc().getDeviceUuid();
		c.connectOptions.setPassword(c.password.toCharArray());

		c.connected = false;
		MQTTConnect(c);
	}	

	
	public static void start(InitialCallbacks callbacks, int businessLogicInterval) {

		instaMsg = new InstaMsg();
		
		instaMsg.callbacks = callbacks;
		
		long currentTick = time.getCurrentTick();		
		long nextPingReqTick = currentTick + instaMsg.pingRequestInterval.intValue();
		long nextBusinessLogicTick = currentTick + businessLogicInterval;

		boolean socketReadJustNow = false;

		while(true) {

			initInstaMsg(instaMsg, null);			
			Log.infoLog("Device-UUID :: [" + modulesProvideInterface.getMisc().getDeviceUuid() + "]");

			while(true) {

				socketReadJustNow = false;

				if(instaMsg.socket.socketCorrupted == true) {
					Log.errorLog("Socket not available at physical layer .. so nothing can be read from socket.");
					break;

				} else {
					readAndProcessIncomingMQTTPacketsIfAny(instaMsg);
					socketReadJustNow = true;
				}

				if(true) {
					
					while(true) {
						removeExpiredResultHandlers(instaMsg);
						removeExpiredOneToOneHandlers(instaMsg);
						
						if(socketReadJustNow == false) {
							InstaMsg.startAndCountdownTimer(1, false);
						}
						
						long latestTick = time.getCurrentTick();
						
						if((latestTick >= nextPingReqTick) && (instaMsg.pingRequestInterval.intValue() != 0)) {
							
                            Log.infoLog("Time to play ping-pong with server !!!\n");
                            sendPingReqToServer(instaMsg);

                            nextPingReqTick = latestTick + instaMsg.pingRequestInterval.intValue();
						}
						
						if(latestTick >= nextBusinessLogicTick) {
							
							callbacks.coreLoopyBusinessLogicInitiatedBySelf();
							
							nextBusinessLogicTick = latestTick + businessLogicInterval;
							break;
						}
					}
				}
			}

			if(instaMsg.connected == true) {

			} else if(instaMsg.socket.socketCorrupted == false) {

				instaMsg.connectionAttempts++;
				Log.errorLog("Socket is fine at physical layer, but no connection established (yet) with InstaMsg-Server.");

				if(instaMsg.connectionAttempts > InstaMsg.MAX_CONN_ATTEMPTS_WITH_PHYSICAL_LAYER_FINE)
				{
					instaMsg.connectionAttempts = 0;

					Log.errorLog("Connection-Attempts exhausted ... so trying with re-initializing the socket-physical layer.");
					instaMsg.socket.socketCorrupted = true;
				}
			}

			if(instaMsg.socket.socketCorrupted == true) {

				clearInstaMsg(instaMsg);
				break;
			}				
		}

	}
}


class MessageHandlers {
	
	int msgId;
	int timeout;
	String topicFilter;
	MessageHandler messageHandler;
}

class ResultHandlers {
	
	int msgId;
	int timeout;
	ResultHandler resultHandler;
}

class OneToOneHandlers {
	
	int msgId;
	int timeout;
	OneToOneHandler oneToOneHandler;
}

class MQTTFixedHeader{
	
    byte packetType;    
    /*
    boolean dup;
    int qos;
    boolean retain;
    */
}

