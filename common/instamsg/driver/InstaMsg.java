package common.instamsg.driver;

import common.instamsg.driver.Config.CONFIG_TYPE;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttException;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttMessage;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttConnack;
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
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubscribe;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import config.DeviceConstants;
import config.ModulesProviderFactory;


@SuppressWarnings("unused")
public class InstaMsg implements MessagingAPIs {
	
	public static enum ReturnCode
	{
	    SOCKET_READ_TIMEOUT,
	    BUFFER_OVERFLOW,
	    FAILURE,
	    SUCCESS
	}

	public static String INSTAMSG_VERSION = "1.5.5";
	
	public static int QOS0 = 0;
	public static int QOS1 = 1;
	public static int QOS2 = 2;

	public static InstaMsg instaMsg;
	
	static int MAX_MESSAGE_HANDLERS = 5;
	static int MAX_PACKET_ID = 10000;
	
	static String EMPTY_CLIENT_ID 				= "EMPTY";
	static String PROVISIONING_CLIENT_ID    	= "PROVISIONING";
	static String PROVISIONED     				= "PROVISIONED";
	static String CONNECTED 					= "CONNECTED";
	
	static int MAX_CYCLES_TO_WAIT_FOR_PUBACK = 10;
	static int pubAckMsgId;
	static int pubAckRecvAttempts;
	static String lastPubTopic;
	static String lastPubPayload;
	
	static enum PUBACK_STATE
	{
	    WAITING_FOR_PUBACK,
	    NOT_WAITING_FOR_PUBACK,
	    PUBACK_TIMEOUT
	};
	static PUBACK_STATE waitingForPuback;

	
	static enum MESSAGE_SOURCE
	{
	    PERSISTENT_STORAGE,
	    GENERAL
	};
	
	static MESSAGE_SOURCE msgSource;
	static boolean rebootPending;
	
	public static int MQTT_RESULT_HANDLER_TIMEOUT = 10;	

	public static ModulesProviderInterface modulesProvideInterface;
	public static common.instamsg.driver.Config config;
	public static Time time;
	public static Log logger;
	public static Misc misc;
	public static Media media;
	public static Watchdog watchdog;
	public Socket socket;
	public static DataLogger dataLogger;

	static boolean mqttConnectFlag = false;
	static boolean notifyServerOfSecretReceived = false;
	static String DATA_LOG_TOPIC   = "topic";
	static String DATA_LOG_PAYLOAD = "payload";
	
	static final String TOPIC_SESSION_DATA  =   "instamsg/client/session";
	static final String TOPIC_NETWORK_DATA  =   "instamsg/client/signalinfo";
	static final String TOPIC_CONFIG_SEND   =   "instamsg/client/config/clientToServer";
	static final String TOPIC_NOTIFICATION  =   "instamsg/client/notifications";
	static final String TOPIC_INFO     		=   "instamsg/client/info";
	
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
	public static boolean mediaStreamingErrorOccurred = false;
	
	byte[] readBuf = new byte[InstaMsg.MAX_BUFFER_SIZE];

	private String filesTopic;
	private String rebootTopic;
	public String serverLogsTopic;
	private String enableServerLoggingTopic;
	private String fileUploadUrl;
	private String receiveConfigTopic;
	private String mediaTopic;
	private String mediaReplyTopic;
	private String mediaStopTopic;
	private String mediaPauseTopic;
	private String mediaStreamsTopic;
	
	static String streamId;
	
	private static ResultHandler pubCompResultHandler;
	
	int networkInfoInterval = 300;
	ChangeableInt pingRequestInterval = new ChangeableInt(0);
	ChangeableInt compulsorySocketReadAfterMQTTPublishInterval = new ChangeableInt(0);
	ChangeableInt mediaStreamingEnabledAtRuntime = new ChangeableInt(0);
	ChangeableInt editableBusinessLogicInterval = new ChangeableInt(0);
	
	int publishCount = 0;
	private InitialCallbacks callbacks;

	static long FRESH_SERVER_LOGS_TIME = -1;
	static long serverLogsStartTime = FRESH_SERVER_LOGS_TIME;
	
	public static boolean runBusinessLogicImmediately = false;
	public static boolean businessLogicRunOnceAtStart = false;
	
	static String ONE_TO_ONE     = "[ONE-TO-ONE] ";
	static String MEDIA          = "[MEDIA] ";
	static String SERVER_LOGGING = "[SERVER-LOGGING] ";
	
	static String SECRET         = "SECRET";


	public static int NETWORK_INFO_INTERVAL = 300;
	public static int MAX_CONN_ATTEMPTS_WITH_PHYSICAL_LAYER_FINE = 5;
	public static int MAX_TRIES_ALLOWED_WHILE_READING_FROM_SOCKET_MEDIUM = 1;
	public static int SOCKET_READ_TIMEOUT_SECS = 1;
	
	public static int INSTAMSG_PORT;
	


	
	
	static {
		pubCompResultHandler = new ResultHandler() {
			
			@Override
			public void handle(int msgId) {
				
				Log.infoLog("PUBCOMP received for msg-id [" + msgId + "]");
			}
		};
		
		modulesProvideInterface = ModulesProviderFactory.getModulesProvider(DeviceConstants.DEVICE_NAME);
		
		config = modulesProvideInterface.getConfig();
		config.initConfig();
		
		time = modulesProvideInterface.getTime();
		time.initGlobalTimer();
		
		logger = modulesProvideInterface.getLogger();
		logger.initLogger();
		
		misc = modulesProvideInterface.getMisc();
		
		media = modulesProvideInterface.getMedia();

		watchdog = modulesProvideInterface.getWatchdog();
		watchdog.watchdogInit();
		
		dataLogger = modulesProvideInterface.getDataLogger();
		
		
		if(DeviceConstants.SSL_SOCKET == true) {
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
	
	
	public static ReturnCode doMqttSendPublish(int msgId, OneToOneHandler oneToOneHandler, int timeout, String peer, String message) {
		
  		attachOneToOneHandler(instaMsg, msgId, timeout, oneToOneHandler);
		return instaMsg.publish(peer,
                  		            message,
                		            QOS0,
                		            false,
                		            new ResultHandler() {
			
										@Override
										public void handle(int msgId) {
											Log.infoLog("[DEFAULT-PUBLISH-HANDLER] PUBACK received for msg-id [" + msgId + "]");
									
										}
									},
									InstaMsg.MQTT_RESULT_HANDLER_TIMEOUT,
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
	
	private static void freeLastPubMessageResources() {
		
		if(lastPubTopic != null) {
			lastPubTopic = null;
	    }
	
	    if(lastPubPayload != null)
	    {
	        lastPubPayload = null;
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
				
				if(msgId == pubAckMsgId) {
					
					if(lastPubPayload != null) {
						Log.infoLog("PUBACK received for message [" + lastPubPayload + "]");
					}
			
					freeLastPubMessageResources();
					waitingForPuback = PUBACK_STATE.NOT_WAITING_FOR_PUBACK;
					pubAckRecvAttempts = 0;
				}

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
		
		ReturnCode rc = ReturnCode.FAILURE;
		
		watchdog.watchdogResetAndEnable(30, "sendPacket", true);
		
		if(c.socket.socketCorrupted == true) {
			
			Log.errorLog("Socket not available at physical layer .. so packet cannot be sent to server.");

		} else if((mqttConnectFlag == false) && (c.connected == false)) {
			
			Log.errorLog("No CONNACK received from server .. so packet cannot be sent to server.");
			
	    } else {
			
			rc = c.socket.socketWrite(packet, packet.length);
			
			if(rc == ReturnCode.FAILURE) {
				c.socket.socketCorrupted = true;
			}
		}		
		
		mqttConnectFlag = false;
		
		watchdog.watchdogDisable(null);
		return rc;
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
		fixedHeader.packetType = (byte) ((packetHeader >> 4) & 0x0F);
	}
	
	
	private static ReturnCode readPacket(InstaMsg c, MQTTFixedHeader fixedHeader) {
		
		watchdog.watchdogResetAndEnable(30 * MAX_TRIES_ALLOWED_WHILE_READING_FROM_SOCKET_MEDIUM * SOCKET_READ_TIMEOUT_SECS,
				                         "readPacket", true);
		ReturnCode rc = readPacketActual(c, fixedHeader);
		watchdog.watchdogDisable(null);
		
		return rc;
	}
	
	
	private static ReturnCode readPacketActual(InstaMsg c, MQTTFixedHeader fixedHeader) {
		
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
			instaMsg.publish(topicName,
					             data,
					             QOS0,
					             false,
					             null,
					             MQTT_RESULT_HANDLER_TIMEOUT,
					             true);
			
		} else {			
			Log.infoLog("Not publishing empty-message to topic [" + topicName + "]");

		}
	}
	

	private static String sendPreviouslyUnsentData() {
		
		String record = null;	
	    /*
	     * Also, try sending the records stored in the persistent-storage (if any).
	     */
	    if(true) {
	        record = dataLogger.getNextRecordFromPersistentStorage();
	
	        if(record != null) {
	        	
	            /*
	             * We got the record.
	             */	
	            String topic = Json.getJsonKeyValueIfPresent(record, DATA_LOG_TOPIC);
	            String payload = Json.getJsonKeyValueIfPresent(record, DATA_LOG_PAYLOAD);
	
	            startAndCountdownTimer(1, false);
	
	            Log.infoLog("Sending data that could not be sent earlier [" + payload + "] over [" + topic + "]");
	
	            if(InstaMsg.instaMsg.publishMessageWithDeliveryGuarantee(topic, payload) != ReturnCode.SUCCESS) {
	                Log.infoLog("Since the data could not be sent to InstaMsg-Server, so not retrying sending data from persistent-storage");
	            }
	            
	        } else {
	        	
	            /*
	             * We did not get any record.
	             */
	            Log.infoLog("\n\nNo more pending-data to be sent from persistent-storage\n\n");
	        }
	    }	    
	
	    return record;
	}

	
	private static void handleConnOrProvAckGeneric(InstaMsg c, int connackRc, String mode)
	{
	    if(connackRc == 0x00)  /* Connection Accepted */
	    {
	        Log.infoLog("\n\n" + mode + " successfully to InstaMsg-Server.\n\n");
	        c.connected = true;
	        
	        
	        sendClientData(misc.getClientSessionData(), TOPIC_SESSION_DATA);
	        sendClientData(misc.getNetworkData(), TOPIC_NETWORK_DATA);
	        sendClientData(misc.getClientInfo(), TOPIC_INFO);
		
	        
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
	        
            config.registerEditableConfig(c.editableBusinessLogicInterval,
            							  "BUSINESS_LOGIC_INTERVAL",
            							  CONFIG_TYPE.CONFIG_INT,
            							  c.editableBusinessLogicInterval.intValue() + "",
                                          "Business-Logic Interval (in seconds)");
	        
	        if(DeviceConstants.MEDIA_STREAMING_ENABLED == true) {
	        
	        	config.registerEditableConfig(c.mediaStreamingEnabledAtRuntime,
	        								  "MEDIA_STREAMING_ENABLED",
	        								  CONFIG_TYPE.CONFIG_INT,
	        								  "0",
                    					  	  "0 - Disabled; 1 - Enabled");
	        	
	        	if(c.mediaStreamingEnabledAtRuntime.intValue() == 1) {
	        		initiateStreaming();
	        	}
	        }
	        
	        if(notifyServerOfSecretReceived == true)
	        {
	        	mqttConnectFlag = true;
				c.publish(TOPIC_NOTIFICATION,
						  "SECRET RECEIVED",
						  QOS0,
						  false,
						  null,
						  MQTT_RESULT_HANDLER_TIMEOUT,
						  true);
	        }
	        c.callbacks.onConnectOneTimeOperations();
	    }
	    else
	    {
	        Log.infoLog("Client-" + mode + " failed with code [" + connackRc + "]");
	    }
	}
	
	
	private static void logJsonFailureMessageAndReturn(String module, String key, String msg)
	{
	    Log.errorLog(module + "Could not find key [" + key + "] in message-payload [" + msg + "] .. " +
	                 "not proceeding further");
	}

	
	private static void broadcastMedia(String sdpAnswer) {
		/*
		 * We hard-code the media-server-ip-address.
		 */
		String mediaServerAddress = "23.253.42.123";
		
		/*
		 * We need to extract the media-server-port from the line of type ::
		 * 
		 *                 m=video 12345 RTP/AVP 96
		 */
		String textToSearch = "m=video ";
		int index = sdpAnswer.indexOf(textToSearch);
		
		String[] tokens = sdpAnswer.substring(index + textToSearch.length()).split(" ");
		String mediaServerPort = tokens[0];		
	
		if(mediaServerPort != null) {	
			Log.infoLog(MEDIA + "Media-Server IP-Address and Port being used for streaming [" + mediaServerAddress + 
					            "], [" + mediaServerPort + "]");
			media.createAndStartStreamingPipeline(mediaServerAddress, mediaServerPort);
			
		} else {
			Log.errorLog(MEDIA + "Could not find server-port for streaming.. not doing anything else !!!");
			return;
		}
	}

	
	private static void handleMediaReplyMessage(InstaMsg c, String payload) {
		
	    Log.infoLog(MEDIA + "Received media-reply-message [" + payload + "]");

	    {
	        String STREAM_ID = "stream_id";
	        String SDP_ANSWER = "sdp_answer";

	        streamId = Json.getJsonKeyValueIfPresent(payload, STREAM_ID);
	        String sdpAnswer = Json.getJsonKeyValueIfPresent(payload, SDP_ANSWER);

	        if(sdpAnswer.length() > 0) {
	            broadcastMedia(sdpAnswer);
	        }
	    }
	}


	private static void handleMediaStreamsMessage(InstaMsg c, String payload) {
		
	    Log.infoLog(MEDIA + "Received media-streams-message [" + payload + "]");

	    {
	        String REPLY_TO = "reply_to";
	        String MESSAGE_ID = "message_id";
	        String METHOD = "method";

	        String replyTopic = Json.getJsonKeyValueIfPresent(payload, REPLY_TO);
	        String messageId = Json.getJsonKeyValueIfPresent(payload, MESSAGE_ID);
	        String method = Json.getJsonKeyValueIfPresent(payload, METHOD);

	        if(replyTopic.length() == 0) {
	            logJsonFailureMessageAndReturn(MEDIA, REPLY_TO, payload);
	            return;
	        }

	        if(messageId.length() == 0) {
	            logJsonFailureMessageAndReturn(MEDIA, MESSAGE_ID, payload);
	            return;
	        }

	        if(method.length() == 0) {
	            logJsonFailureMessageAndReturn(MEDIA, METHOD, payload);
	            return;
	        }

	        if(method.equals("GET") == true) {
	        	String message = "{\"response_id\": \"" + messageId + "\", \"status\": 1, \"streams\": \"[" + streamId + "]\"}";

	            c.publish(replyTopic,
	                      message,
	                      QOS0,
	                      false,
	                      null,
	                      MQTT_RESULT_HANDLER_TIMEOUT,
	                      true);
	        }
	    }
	}
	
	
	private static void handleMediaStopMessage(InstaMsg c) {
		
		Log.infoLog(MEDIA + "Stopping .....");
		media.stopStreaming();
		
	    String message = "{'to':'" + c.clientIdComplete + "','from':'" + c.clientIdComplete + "','type':3,'stream_id': '" + streamId + "'}";
	    c.publish(c.mediaTopic,
	              message,
	              QOS0,
	              false,
	              null,
	              MQTT_RESULT_HANDLER_TIMEOUT,
	              true);
	}
	
	
	private static void handleMediaPauseMessage(InstaMsg c) {
		
		Log.infoLog(MEDIA + "Pausing .....");
		media.pauseStreaming();
	}

	
	
	private static void initiateStreaming() {

		String selfIpAddress = misc.getDeviceIpAddress();		
		String sdpOffer = "";
		
	    sdpOffer = "v=0\r\n";
	    sdpOffer += "o=- 0 0 IN IP4 " + selfIpAddress + "\r\n";
	    sdpOffer += "s=\r\n";
	    sdpOffer += "c=IN IP4 " + selfIpAddress + "\r\n";
	    sdpOffer += "t=0 0\r\n";
	    sdpOffer += "a=charset:UTF-8\n";
	    sdpOffer += "a=recvonly\r\n";
	    sdpOffer += "m=video 50004 RTP/AVP 96\r\n";
	    sdpOffer += "a=rtpmap:96 H264/90000\r\n";
	        
	    String message = "{"									         				+
	    						"'to': '" + instaMsg.clientIdComplete + "', "   		+
	    						"'sdp_offer' : '" + sdpOffer + "', "     				+
	    						"'from': '" + instaMsg.clientIdComplete + "', " 		+
	    						"'protocol' : 'rtp', "                   				+
	    						"'type':'7', "                           				+
	    						"'stream_id':'" + instaMsg.clientIdComplete + "', "     + 
	    						"'record': True"                         				+
	    				 "}";

		instaMsg.publish(instaMsg.mediaTopic,
				  		 message,
				  		 QOS0,
				  		 false,
				  		 null,
				  		 MQTT_RESULT_HANDLER_TIMEOUT,
				  		 true);
				
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
	    
	    if(DeviceConstants.MEDIA_STREAMING_ENABLED == true) {
	    	
		    c.mediaTopic           = "instamsg/clients/" + c.clientIdComplete + "/media";
		    c.mediaReplyTopic      = "instamsg/clients/" + c.clientIdComplete + "/mediareply";
		    c.mediaStopTopic       = "instamsg/clients/" + c.clientIdComplete + "/mediastop";
		    c.mediaPauseTopic      = "instamsg/clients/" + c.clientIdComplete + "/mediapause";
		    c.mediaStreamsTopic    = "instamsg/clients/" + c.clientIdComplete + "/mediastreams";
	    }


	    Log.infoLog("\nThe special-topics value :: \n");
	    Log.infoLog("FILES_TOPIC = [" + c.filesTopic + "]");
	    Log.infoLog("REBOOT_TOPIC = [" + c.rebootTopic + "]");
	    Log.infoLog("ENABLE_SERVER_LOGGING_TOPIC = [" + c.enableServerLoggingTopic + "]");
	    Log.infoLog("SERVER_LOGS_TOPIC = [" + c.serverLogsTopic + "]");
	    Log.infoLog("FILE_UPLOAD_URL = [" + c.fileUploadUrl + "]");
	    Log.infoLog("CONFIG_FROM_SERVER_TO_CLIENT = [" + c.receiveConfigTopic + "]");
	    
	    if(DeviceConstants.MEDIA_STREAMING_ENABLED == true) {
	    	
		    Log.infoLog("MEDIA_TOPIC = [" + c.mediaTopic + "]");
		    Log.infoLog("MEDIA_REPLY_TOPIC = [" + c.mediaReplyTopic + "]");
		    Log.infoLog("MEDIA_STOP_TOPIC = [" + c.mediaStopTopic + "]");
		    Log.infoLog("MEDIA_PAUSE_TOPIC = [" + c.mediaPauseTopic + "]");
		    Log.infoLog("MEDIA_STREAMS_TOPIC = [" + c.mediaStreamsTopic + "]");
	    }
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
		if(receivedMsgQos == QOS1) {
			packet = getEncodedMqttMessageAsByteStream(new MqttPubAck(pubMsg));
			
		} else if(receivedMsgQos == QOS2) {
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
			
			if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_CONNACK) {
				try {
					MqttConnack msg = (MqttConnack) MqttWireMessage.createWireMessage(c.readBuf);
					if(msg.getReturnCode() == 0) {
						handleConnOrProvAckGeneric(c, msg.getReturnCode(), CONNECTED);
					}
					
				} catch (MqttException e) {					
					rc = handleMessageDecodingFailure(c, "MQTT-CONNACK");
				}
				
			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PROVACK) {
				
				try {
					MqttProvack msg = (MqttProvack) MqttWireMessage.createWireMessage(c.readBuf);
					if(msg.getReturnCode() == 0) {

						/*
						 * Connection was established successfully;
						 */
						c.clientIdComplete = msg.getClientId();						
						if(msg.getSecret() != null) {

							Log.infoLog("Received client-secret from server via PROVACK [" + msg.getSecret() + "]");

							String secretConfig = config.generateConfigJson(InstaMsg.SECRET,
																			CONFIG_TYPE.CONFIG_STRING,
																			msg.getCompletePayload(),
																			"");
							config.saveConfigValueOnPersistentStorage(InstaMsg.SECRET, secretConfig);



							/*
							 * Send notification to the server, that the secret-password has been saved.
							 */
							notifyServerOfSecretReceived = true;
						}

						Log.infoLog("Received client-id from server via PROVACK [" + c.clientIdComplete + "]");
						setValuesOfSpecialTopics(c);
						
                        /*
                         * Reboot the device, so that the next time the CONNECT-cycle takes place.
                         */
                        misc.rebootDevice();
					}													

					handleConnOrProvAckGeneric(c, msg.getReturnCode(), PROVISIONED);

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
				
			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PUBACK) {
				
				try {
					MqttPubAck pubAckMsg = (MqttPubAck) MqttWireMessage.createWireMessage(c.readBuf);
					fireResultHandlerUsingMsgIdAsTheKey(instaMsg, pubAckMsg.getMessageId());
					
				} catch (MqttException e) {
					rc = handleMessageDecodingFailure(c, "MQTT-PUBACK");
				}
				
			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PUBLISH) {
				
				try {
					MqttPublish pubMsg = (MqttPublish) MqttWireMessage.createWireMessage(c.readBuf);
					
					String topicName = pubMsg.getTopicName();
					if(topicName.equals(c.enableServerLoggingTopic)) {
						serverLoggingTopicMessageArrived(c, new String(pubMsg.getPayload()));
					}
					
					else if(topicName.equals(c.rebootTopic)) {
						Log.infoLog("Received REBOOT request from server.. rebooting !!!");
						misc.rebootDevice();
						
					} else if(topicName.equals(c.clientIdComplete)) {
						oneToOneMessageArrived(c, new String(pubMsg.getPayload()));
						
					} else if(topicName.equals(c.receiveConfigTopic)) {                    	
                        handleConfigReceived(c, new String(pubMsg.getPayload()));
                        
                    } else if(DeviceConstants.MEDIA_STREAMING_ENABLED == true) {
                    	
                    	if(topicName.equals(c.mediaReplyTopic)) {                    	
                    		handleMediaReplyMessage(c, new String(pubMsg.getPayload()));
                        
                    	} else if(topicName.equals(c.mediaStreamsTopic)) {                    	
                    		handleMediaStreamsMessage(c, new String(pubMsg.getPayload()));
                        
                    	} else if(topicName.equals(c.mediaStopTopic)) {
                    		handleMediaStopMessage(c);
                    		
                    	} else if(topicName.equals(c.mediaPauseTopic)) {
                    		handleMediaPauseMessage(c);
                    		
                    	}
                    	
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
					rc = handleMessageDecodingFailure(c, "MQTT-SUBACK");
		
				}
			
			} else {
				
			}
		
		} while (rc == InstaMsg.ReturnCode.SUCCESS);		
	}

	
	private static void serverLoggingTopicMessageArrived(InstaMsg c, String payload) {
		
	    String CLIENT_ID = "client_id";
	    String LOGGING = "logging";
	    
	    String clientId = Json.getJsonKeyValueIfPresent(payload, CLIENT_ID);
	    String logging = Json.getJsonKeyValueIfPresent(payload, LOGGING);

	    if( (clientId.length() > 0) && (logging.length() > 0) ) {
	    	
	        if(logging.equals("1")) {
	            c.serverLoggingEnabled = true;
	            InstaMsg.serverLogsStartTime = FRESH_SERVER_LOGS_TIME;
	            
	            Log.infoLog(SERVER_LOGGING + "Enabled.");
	            
	        } else {
	            c.serverLoggingEnabled = false;
	            Log.infoLog(SERVER_LOGGING + "Disabled.");
	            
	        }
	    }		
	}


	private static void oneToOneMessageArrived(InstaMsg c, String payload) {
		
		Log.infoLog(ONE_TO_ONE + " Payload == [" + payload + "]");
		
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
		                          "Peer = ["            + oneToOneResult.peerClientId      + "], " +
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
	
	private static void saveFailedPublishedMessage() {
		
	    rebootPending = true;
	
	    if(true) {
	    	
	        if(true) {
	        	
	            String messageSavingJson =  "{'" + DATA_LOG_TOPIC   + "' : '" + lastPubTopic + "', " +
	                                         "'" + DATA_LOG_PAYLOAD + "' : '" + lastPubPayload + "'}";
	
	            dataLogger.saveRecordToPersistentStorage(messageSavingJson);	
	            Log.errorLog(DataLogger.DATA_LOGGING_ERROR + "Either message-sending failed over wire, " +
	                         "or PUBACK was not received for message [" + lastPubPayload + "] within time");
	
	            waitingForPuback = PUBACK_STATE.PUBACK_TIMEOUT;
	            freeLastPubMessageResources();
	        }
	    }
	}
	
	
	private static void waitForPubAck()
	{
	    if(true) {
	    	
	        if(true) {
	        	
	            if(true) {
	            	
	                while(true) {
	                	
	                    readAndProcessIncomingMQTTPacketsIfAny(instaMsg);
	                    if(waitingForPuback == PUBACK_STATE.WAITING_FOR_PUBACK)
	                    {
	                        pubAckRecvAttempts = pubAckRecvAttempts + 1;
	                        if(pubAckRecvAttempts >= MAX_CYCLES_TO_WAIT_FOR_PUBACK)
	                        {
	                            pubAckRecvAttempts = 0;
	                            saveFailedPublishedMessage();
	
	                            break;
	                        }
	                    }
	                    else
	                    {
	                        break;
	                    }
	                }
	            }
	        }
	    }
	}
	

	public InstaMsg.ReturnCode publish(String topicName,
			 						   String payload,
			 						   int qos,
			 						   boolean dup,
			 						   ResultHandler resultHandler,
			 						   int resultHandlerTimeout,
			 						   boolean logging) {
		
		MqttMessage baseMessage = new MqttMessage();
		baseMessage.setPayload(payload.getBytes());
		baseMessage.setQos(qos);
		baseMessage.setDuplicate(dup);
		baseMessage.setRetained(false);
		
		startAndCountdownTimer(1, false);
		
		MqttPublish pubMsg = new MqttPublish(topicName, baseMessage);
		if((qos == QOS1) || (qos == QOS2)) {
			
			int msgId = getNextPackedId(instaMsg);
			
			pubAckMsgId = msgId;
			waitingForPuback = PUBACK_STATE.WAITING_FOR_PUBACK;
			
			lastPubTopic = topicName;
			lastPubPayload = payload;
					
			pubMsg.setMessageId(msgId);
			attachResultHandler(instaMsg, msgId, resultHandlerTimeout, resultHandler);
		}
		
		if(logging == true) {
			Log.infoLog("\nPublishing message [" + payload + "] to topic [" + topicName + "]");
		}
		
		byte[] packet = getEncodedMqttMessageAsByteStream(pubMsg);
		if(packet == null) {
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		InstaMsg.ReturnCode rc = sendPacket(instaMsg, packet);	
		if(rc == InstaMsg.ReturnCode.SUCCESS) {
			instaMsg.publishCount++;
		}
		
	    if(rc == InstaMsg.ReturnCode.SUCCESS) {
	    	
	        if(logging == true) {
	            Log.infoLog("Published successfully pver socket.");
	        }
	        
	        if(instaMsg.compulsorySocketReadAfterMQTTPublishInterval.intValue() != 0) {
	            if((instaMsg.publishCount % instaMsg.compulsorySocketReadAfterMQTTPublishInterval.intValue()) == 0) {
	            	
	            	if(logging == true) {
	            		Log.infoLog("Doing out-of-order socket-read, as [" + 
	            					instaMsg.compulsorySocketReadAfterMQTTPublishInterval.intValue() + "] " +
	                 		    	" MQTT-Publishes have been done");
	            	}

	                readAndProcessIncomingMQTTPacketsIfAny(instaMsg);
	            }
	        }
	        
	        if((qos == QOS1) || (qos == QOS2)) {
	        	waitForPubAck();
	        }
	        
	    } else {
	    	
	    	if(logging == true) {
	    		Log.errorLog("Publishing failed over socket.\n");
	    	}
	    	
	        if((qos == QOS1) || (qos == QOS2)) {
	        	saveFailedPublishedMessage();
	        }
	    }
	    
	    return rc;
	}
	
	
	public ReturnCode subscribe(String topicName,
									int qos,
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
		qosValues[0] = qos;
		
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

	
	public ReturnCode send(String peer,
			                   String payload,
			                   OneToOneHandler oneToOneHandler,
			                   int timeout) {
		
		InstaMsg c = instaMsg;
		int id = getNextPackedId(c);

		String message = "{\"message_id\": \""  + id + 
				         "\", \"reply_to\": \"" + c.clientIdComplete +
				         "\", \"body\": \""     + payload + "\"}";

  		return doMqttSendPublish(id, oneToOneHandler, timeout, peer, message);
	}
	
	
	public ReturnCode unsubscribe(String topicName) {
		
		InstaMsg c = instaMsg;
		int msgId = getNextPackedId(c);
		
		String[] topicNames = new String[1];
		topicNames[0] = topicName;
		
		MqttUnsubscribe unsubMsg = new MqttUnsubscribe(topicNames);
		unsubMsg.setMessageId(msgId);
		
		byte[] packet = getEncodedMqttMessageAsByteStream(unsubMsg);
		if(packet == null) {
			return ReturnCode.FAILURE;
		}
		
		return sendPacket(c, packet);
	}
	

	public static InstaMsg.ReturnCode MQTTConnect(InstaMsg c) {
		
		String secretConfig = config.getConfigValueFromPersistentStorage(SECRET);
		if(secretConfig != null) {
			
			/*
			 * We will receive CONNACK for this leg.
			 */
			String secret = Json.getJsonKeyValueIfPresent(secretConfig, Config.CONFIG_VALUE_KEY);
			
			c.clientIdComplete = secret.substring(0, 36);
			setValuesOfSpecialTopics(c);
			
			c.clientIdMachine = EMPTY_CLIENT_ID;
			c.username = secret.substring(0, 36);
			c.password = secret.substring(37, secret.length());
			
			notifyServerOfSecretReceived = true;
		} else {
			
			/*
			 * We will receive PROVACK for this leg.
			 */
			c.clientIdMachine = PROVISIONING_CLIENT_ID;
			
			if(DeviceConstants.GSM_DEVICE == true) {
				c.password = InstaMsg.instaMsg.socket.provPin;
			} else {
				c.password = misc.getProvPinForNonGsmDevices();
			}
			c.username = misc.getDeviceUuid();
		}
		
		c.connectOptions.setClientId(c.clientIdMachine);
		c.connectOptions.setUserName(c.username);
		c.connectOptions.setPassword(c.password.toCharArray());
		
		MqttConnect connectMsg = new MqttConnect(c.connectOptions);
		
		byte[] packet = getEncodedMqttMessageAsByteStream(connectMsg);
		if(packet == null) {
			return InstaMsg.ReturnCode.FAILURE;
		}
		
		mqttConnectFlag = true;
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
	        	Log.infoLog(seconds - i + "");
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
		
		c.socket = modulesProvideInterface.getSocket(DeviceConstants.INSTAMSG_HOST, InstaMsg.INSTAMSG_PORT);		
		c.socket.socketCorrupted = true;
		
		c.socket.initSocket();

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
		
		if(c.socket.socketCorrupted == true) {
			return;
		}


		c.nextPackedId = MAX_PACKET_ID;		    
		c.initialCallbacks = callbacks;
		c.serverLoggingEnabled = false;

		c.connectOptions.setMqttVersion(3);
		c.connectOptions.setCleanSession(true);
		

		c.clientIdComplete = "";

		c.connected = false;
		
		
		dataLogger.initDataLogger();
		pubAckRecvAttempts = 0;
		waitingForPuback = PUBACK_STATE.NOT_WAITING_FOR_PUBACK;
		msgSource = MESSAGE_SOURCE.GENERAL;
		rebootPending = false;		

		MQTTConnect(c);
	}	

	
	public static void start(InitialCallbacks callbacks, int businessLogicInterval) {

		instaMsg = new InstaMsg();
		
		instaMsg.callbacks = callbacks;
		
		long currentTick = time.getCurrentTick();
		long nextNetworkInfoTick = currentTick + instaMsg.networkInfoInterval;
		long nextPingReqTick = currentTick + instaMsg.pingRequestInterval.intValue();
		
		instaMsg.editableBusinessLogicInterval = new ChangeableInt(businessLogicInterval);
		long nextBusinessLogicTick = currentTick + instaMsg.editableBusinessLogicInterval.intValue();

		while(true) {

			initInstaMsg(instaMsg, callbacks);	
			
			Log.infoLog("Device-UUID :: [" + modulesProvideInterface.getMisc().getDeviceUuid() + "]");
			Log.infoLog("IP-Address :: [" + modulesProvideInterface.getMisc().getDeviceIpAddress() + "]");

			while(true) {

				InstaMsg.startAndCountdownTimer(1, false);

				if(instaMsg.socket.socketCorrupted == true) {
					Log.errorLog("Socket not available at physical layer .. so nothing can be read from socket.");

				} else {
					
					if(true) {
						
						readAndProcessIncomingMQTTPacketsIfAny(instaMsg);
						
						if((msgSource == MESSAGE_SOURCE.PERSISTENT_STORAGE) && 
						   (waitingForPuback != PUBACK_STATE.WAITING_FOR_PUBACK) &&
						   (rebootPending == false)) {
							
							String retrievedFromPersistenceMessage = sendPreviouslyUnsentData();
							if(retrievedFromPersistenceMessage == null) {
								msgSource = MESSAGE_SOURCE.GENERAL;
						    }
						}
						
						if(rebootPending == true) {
							msgSource = MESSAGE_SOURCE.GENERAL;
						}
					}
				}

				if(true) {
					
					if(true) {
						removeExpiredResultHandlers(instaMsg);
						removeExpiredOneToOneHandlers(instaMsg);
						
						long latestTick = time.getCurrentTick();
						
						if(latestTick >= nextNetworkInfoTick) {
							Log.infoLog("Time to send network-stats !!!");
							sendClientData(misc.getNetworkData(), TOPIC_NETWORK_DATA);
							
							nextNetworkInfoTick = latestTick + instaMsg.networkInfoInterval;
						}
						
						if((latestTick >= nextPingReqTick) && (instaMsg.pingRequestInterval.intValue() != 0)) {
							
                            Log.infoLog("Time to play ping-pong with server !!!\n");
                            sendPingReqToServer(instaMsg);

                            nextPingReqTick = latestTick + instaMsg.pingRequestInterval.intValue();
						}
						
						if((latestTick >= nextBusinessLogicTick) || (runBusinessLogicImmediately == true) ||
						   (businessLogicRunOnceAtStart == false)) {
							
							
                            if(msgSource == MESSAGE_SOURCE.GENERAL) {

                            	callbacks.coreLoopyBusinessLogicInitiatedBySelf();
    							runBusinessLogicImmediately = false;

                                if(businessLogicRunOnceAtStart == false)
                                {
                                    msgSource = MESSAGE_SOURCE.PERSISTENT_STORAGE;
                                }

                                if((rebootPending == true) && (businessLogicRunOnceAtStart == true))
                                {
                                    Log.errorLog("There were some messages which did not complete send-cum-ack cycle, so rebooting");
                                    misc.rebootDevice();
                                }

    							businessLogicRunOnceAtStart = true;
    							nextBusinessLogicTick = latestTick + instaMsg.editableBusinessLogicInterval.intValue();
                            }							
						}
						
						if(DeviceConstants.MEDIA_STREAMING_ENABLED == true) {
							
							if(mediaStreamingErrorOccurred == true) {
								Log.errorLog(MEDIA + "Error occurred in media-streaming ... rebooting device to reset everything");
								misc.rebootDevice();
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


	@Override
	public ReturnCode publishMessageWithDeliveryGuarantee(String topic, String payload) {
	    return publish(topic,
	    			   payload,
	    			   QOS1,
	    			   false,
	    			   new ResultHandler() {
						
							@Override
							public void handle(int msgId) {
								
							}
					   },
	    			   MQTT_RESULT_HANDLER_TIMEOUT,
	    			   true);
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

