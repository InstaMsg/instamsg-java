package common.instamsg.driver;

import utils.ChangeableInt;
import utils.Config;
import common.instamsg.driver.Config.CONFIG_TYPE;
import common.instamsg.driver.Globals.ReturnCode;
import common.instamsg.driver.include.Log;
import common.instamsg.driver.include.ModulesProviderFactory;
import common.instamsg.driver.include.ModulesProviderInterface;
import common.instamsg.driver.include.OneToOneResult;
import common.instamsg.driver.include.Socket;
import common.instamsg.driver.include.Time;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttException;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttMessage;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPingReq;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttProvack;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRec;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;


public class InstaMsg {
	
	private static InstaMsg instaMsg;;
	
	static int MAX_MESSAGE_HANDLERS = 5;
	static int MAX_PACKET_ID = 10000;
	static String NO_CLIENT_ID = "NONE";
	
	public static int MQTT_RESULT_HANDLER_TIMEOUT = 10;	

	public static ModulesProviderInterface modulesProvideInterface;
	public static common.instamsg.driver.Config config;
	public static Time time;
	
	MessageHandlers[] messageHandlers = new MessageHandlers[MAX_MESSAGE_HANDLERS];
	ResultHandlers[] resultHandlers = new ResultHandlers[MAX_MESSAGE_HANDLERS];
	OneToOneHandlers[] oneToOneResponseHandlers = new OneToOneHandlers[MAX_MESSAGE_HANDLERS];

	int nextPackedId;

	InitialCallbacks initialCallbacks;
	boolean serverLoggingEnabled;
	
	MqttConnectOptions connectOptions = new MqttConnectOptions();
	
	String clientIdComplete;
	String clientIdMachine;
	String username;
	String password;
	
	Socket socket;
	
	boolean connected = false;
	
	int connectionAttempts = 0;
	
	byte[] readBuf = new byte[Globals.MAX_BUFFER_SIZE];

	private String filesTopic;
	private String rebootTopic;
	private String serverLogsTopic;
	private String enableServerLoggingTopic;
	private String fileUploadUrl;
	private String receiveConfigTopic;
	
	private static ResultHandler pubCompResultHandler;
	
	ChangeableInt pingRequestInterval = new ChangeableInt(0);
	ChangeableInt compulsorySocketReadAfterMQTTPublishInterval = new ChangeableInt(0);
	
	
	
	static {
		pubCompResultHandler = new ResultHandler() {
			
			@Override
			public void handle(int msgId) {
				
				Log.infoLog("PUBCOMP received for msg-id [" + msgId + "]");
			}
		};
		
		modulesProvideInterface = ModulesProviderFactory.getModulesProvider(Config.DEVICE_NAME);
		config = modulesProvideInterface.getConfig();
		time = modulesProvideInterface.getTime();
	}
	
	
	private static int getNextPackedId(InstaMsg c) {
		
		if(c.nextPackedId == MAX_PACKET_ID) {
			c.nextPackedId = 1;
		} else {
			c.nextPackedId++;
		}
		
		return c.nextPackedId;
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
	
	
	private static ReturnCode sendPacket(InstaMsg c, byte[] packet) {
		
		if(c.socket.socketCorrupted == true) {
			Log.errorLog("Socket not available at physical layer .. so packet cannot be sent to server.");
			return ReturnCode.FAILURE;
		}
		
		if(c.socket.socketWrite(packet, packet.length) == ReturnCode.FAILURE) {
			c.socket.socketCorrupted = true;
			return ReturnCode.FAILURE;
		}
		
		return ReturnCode.SUCCESS;
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
	
	
	private static ReturnCode readPacket(InstaMsg c, MQTTFixedHeader fixedHeader) {
		
		if(c.socket.socketCorrupted == true) {
			
			Log.errorLog("Socket not available at physical layer .. so packet cannot be read from server.");
			return ReturnCode.FAILURE;
		}
		
		c.readBuf = new byte[Globals.MAX_BUFFER_SIZE];
		
	    /*
	     * 1. read the header byte.  This has the packet type in it.
	     */
		int numRetries = Globals.MAX_TRIES_ALLOWED_WHILE_READING_FROM_SOCKET_MEDIUM;
		ReturnCode rc = ReturnCode.FAILURE;
	    do
	    {
	        rc = c.socket.socketRead(c.readBuf, 1, false);
	        if(rc == ReturnCode.FAILURE)
	        {
	            c.socket.socketCorrupted = true;
	            return ReturnCode.FAILURE;
	        }

	        if(rc == ReturnCode.SOCKET_READ_TIMEOUT)
	        {
	            numRetries--;
	        }
	    } while((rc == ReturnCode.SOCKET_READ_TIMEOUT) && (numRetries > 0));	    
	    
	    
	    /*
	     * If at this point, we still had a socket-timeout, it means we really have nothing to read.
	     */
	    if(rc == ReturnCode.SOCKET_READ_TIMEOUT)
	    {
	        return ReturnCode.SOCKET_READ_TIMEOUT;
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
	    	if(c.socket.socketRead(i, 1, true) == ReturnCode.FAILURE)
	        {
	            c.socket.socketCorrupted = true;
	            return ReturnCode.FAILURE;
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

	    	if(c.socket.socketRead(remainingBytes, rem_len, true) == ReturnCode.FAILURE)
	        {
	            c.socket.socketCorrupted = true;
	            return ReturnCode.FAILURE;
	        }
	    	
	    	for(int j = 0; j < remainingBytes.length; j++) {
	    		c.readBuf[len + j] = remainingBytes[j];
	    	}
	    }

	    fillFixedHeaderFieldsFromPacketHeader(fixedHeader, c.readBuf[0]);
	    return ReturnCode.SUCCESS;
	}
	
	private static void handleConnOrProvAckGeneric(InstaMsg c, int connackRc)
	{
	    if(connackRc == 0x00)  /* Connection Accepted */
	    {
	        Log.infoLog("\n\nConnected successfully to InstaMsg-Server.\n\n");
	        c.connected = true;
	        
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

	        /*
	        sendClientData(get_client_session_data, TOPIC_SESSION_DATA);
	        sendClientData(get_client_metadata, TOPIC_METADATA);
	        sendClientData(get_network_data, TOPIC_NETWORK_DATA);


	        if(c->onConnectCallback != NULL)
	        {
	            c->onConnectCallback();
	            c->onConnectCallback = NULL;
	        }
	        */
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


	private static void readAndProcessIncomingMQTTPacketsIfAny(InstaMsg c) {
		ReturnCode rc = ReturnCode.FAILURE;
		
		do {
			
			MQTTFixedHeader fixedHeader = new MQTTFixedHeader();
			
			rc = readPacket(c, fixedHeader);
			if(rc != ReturnCode.SUCCESS) {
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
                    if(topicName.equals(c.receiveConfigTopic)) {
                    	
                        handleConfigReceived(c, new String(pubMsg.getPayload()));
                        
                    } else {
						Log.infoLog("Not handling received-topic-message for topic = [" + topicName + "]");

					}
					
				} catch (MqttException e) {
					rc = handleMessageDecodingFailure(c, "MQTT-PUBLISH");
				}

			} else if(fixedHeader.packetType == MqttWireMessage.MESSAGE_TYPE_PINGRESP) {
				
				Log.infoLog("PINGRESP received... relations are intact !!\n");				
				
			} else {
				
				Log.infoLog("Packet received of type = " + fixedHeader.packetType);
			}
		
		} while (rc == ReturnCode.SUCCESS);		
		
	}

	private static void handleConfigReceived(InstaMsg c, String payload) {
		
		Log.infoLog(common.instamsg.driver.Config.CONFIG + "Received the config-payload [" + payload + "] from server");
		config.processConfig(payload);
	}


	private static ReturnCode handleMessageDecodingFailure(InstaMsg c, String messageType) {
		ReturnCode rc;
		Log.errorLog("Error occurred while decoding " + messageType + " message");
		
		c.socket.socketCorrupted = true;
		rc = ReturnCode.FAILURE;
		return rc;
	}
	
	
	public static ReturnCode MQTTPublish(String topicName,
										 String payload,
										 int qos,
										 boolean dup,
										 ResultHandler resultHandler,
										 int resultHandlerTimeout,
										 boolean retain,
										 boolean logging) {
		
		MqttMessage baseMessage = new MqttMessage();
		baseMessage.setPayload(payload.getBytes());
		baseMessage.setQos(qos);
		baseMessage.setDuplicate(dup);
		baseMessage.setRetained(retain);
		
		MqttPublish pubMsg = new MqttPublish(topicName, baseMessage);
		if((qos == 1) || (qos == 2))
		{
			int msgId = getNextPackedId(instaMsg);
			
			pubMsg.setMessageId(msgId);
			attachResultHandler(instaMsg, msgId, resultHandlerTimeout, resultHandler);
		}
		
		byte[] packet = getEncodedMqttMessageAsByteStream(pubMsg);
		if(packet == null) {
			return ReturnCode.FAILURE;
		}
		
		return sendPacket(instaMsg, packet);		
	}

	
	public static ReturnCode MQTTConnect(InstaMsg c) {
		
		MqttConnect connectMsg = new MqttConnect(c.connectOptions);
		
		byte[] packet = getEncodedMqttMessageAsByteStream(connectMsg);
		if(packet == null) {
			return ReturnCode.FAILURE;
		}
		
		return sendPacket(c, packet);
	}	
	

	public static void clearInstaMsg(InstaMsg c) {
		Log.infoLog("CLEARING INSTAMSG !!!!");
		
		c.socket.releaseSocket();		
		c.connected = false;
	}
	

	public static void initInstaMsg(InstaMsg c, InitialCallbacks callbacks) {
		
		c.socket = modulesProvideInterface.getSocket(Globals.INSTAMSG_HOST, Globals.INSTAMSG_PORT);		
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

			c.oneToOneResponseHandlers[i] = new OneToOneHandlers();
			c.oneToOneResponseHandlers[i].msgId = 0;
			c.oneToOneResponseHandlers[i].timeout = 0;
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
						
						if(socketReadJustNow == false) {
							Globals.startAndCountdownTimer(1, false);
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

				if(instaMsg.connectionAttempts > Globals.MAX_CONN_ATTEMPTS_WITH_PHYSICAL_LAYER_FINE)
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
	ResultHandler resultHandler;
}

class ResultHandlers {
	
	int msgId;
	int timeout;
	ResultHandler resultHandler;
}

class OneToOneHandlers {
	
	int msgId;
	int timeout;
	MessageHandler<OneToOneResult> handler;
}

interface MessageHandler<T>{
	void onMessage(T message);
}

class MQTTFixedHeader{
	
    byte packetType;
    
    /*
    boolean dup;
    int qos;
    boolean retain;
    */
}

/*
class MessageData {
	
    MQTTMessage message;
    String topicName;
}
*/

/*
class MQTTMessage {
	
    MQTTFixedHeaderPlusMsgId fixedHeaderPlusMsgId;
    String payload;
    int payloadlen;
}
*/
