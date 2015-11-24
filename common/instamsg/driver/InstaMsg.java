package common.instamsg.driver;

import java.net.Socket;

import utils.Config;
import common.instamsg.driver.include.FileSystem;
import common.instamsg.driver.include.InstamsgEventHandler;
import common.instamsg.driver.include.OneToOneResult;
import common.instamsg.mqtt.src.MQTTPacket;
import common.instamsg.mqtt.src.MQTTPacket.MQTTString;

public class InstaMsg{
	
	final int MAX_MESSAGE_HANDLERS = 5;
	int next_packetid;
	//unsigned char readbuf[MAX_BUFFER_SIZE];
	MessageHandlers messageHandlers[] = new MessageHandlers[MAX_MESSAGE_HANDLERS];
	ResultHandlers resultHandlers[] = new ResultHandlers[MAX_MESSAGE_HANDLERS];
	OneToOneHandlers oneToOneResponseHandlers[] = new OneToOneHandlers[MAX_MESSAGE_HANDLERS];
	
	void (*defaultMessageHandler) (MessageData*);

	public String filesTopic;
	public String rebootTopic;
	public String enableServerLoggingTopic;
	public String  serverLogsTopic;
	public String fileUploadUrl;
	public String receiveConfigTopic;

	public boolean serverLoggingEnabled;

	public Socket ipstack;
	FileSystem singletonUtilityFs;
	MQTTPacket_connectData connectOptions;
	
	String clientIdComplete;
	String clientIdMachine;
	String username;
	String password;
	boolean connected;
	
	public InstaMsg(){
		if(Config.FILE_SYSTEM_INTERFACE_ENABLED){
			singletonUtilityFs = null;
		}
	}

	public void start(InstamsgEventHandler handler, int businessLogicInterval) {

	    long latestTick = Globals.time.getCurrentTick();
	    long nextSocketTick = latestTick + Globals.NETWORK_INFO_INTERVAL;
	    long nextPingReqTick = latestTick + Globals.pingRequestInterval;
	    long nextBusinessLogicTick = latestTick + businessLogicInterval;

	    boolean socketReadJustNow = false;

	    Globals.pingRequestInterval = 0;
	    Globals.compulsorySocketReadAfterMQTTPublishInterval = 0;

	    while(true){
	        initInstaMsg(handler);

	        Globals.RESET_GLOBAL_BUFFER();
	        String GLOBAL_BUFFER = Globals.misc.get_device_uuid();

	        //sg_sprintf();
	        Log.info_log("Device-UUID :: ["+GLOBAL_BUFFER+"]");

	        while(true)
	        {
	            socketReadJustNow = false;

	            if(this.ipstack.socketCorrupted == 1){
	                Log.error_log("Socket not available at physical layer .. so nothing can be read from socket.");
	            }
	            else
	            {
	                readAndProcessIncomingMQTTPacketsIfAny();
	                socketReadJustNow = true;
	            }

	            if(true)
	            {
	                /*
	                 * We ensure that the business-logic is initiated only after the interval, and do the intermediate
	                 * works as and when the time arrives.
	                 */
	                while(true)
	                {
	                    removeExpiredResultHandlers();
	                    removeExpiredOneToOneResponseHandlers();

	                    if(socketReadJustNow)
	                    {
	                        /*
	                         * Sleep only when the socket was not read in the current iteration.
	                         */
	                        startAndCountdownTimer(1, 0);
	                    }


	                    {
	                        latestTick = getCurrentTick();


	                        /*
	                         * Send network-stats if time has arrived.
	                         */
	                        if(latestTick >= nextSocketTick)
	                        {
	                            sg_sprintf(LOG_GLOBAL_BUFFER, "Time to send network-stats !!!");
	                            info_log(LOG_GLOBAL_BUFFER);

	                            sendClientData(get_network_data, TOPIC_NETWORK_DATA);

	                            nextSocketTick = latestTick + NETWORK_INFO_INTERVAL;
	                        }

	                        /*
	                         * Send PINGREQ, if time has arrived,
	                         */
	                        if((latestTick >= nextPingReqTick) && (common.instamsg.driver.include.Globals.pingRequestInterval != 0))
	                        {
	                            sg_sprintf(LOG_GLOBAL_BUFFER, "Time to play ping-pong with server !!!\n");
	                            info_log(LOG_GLOBAL_BUFFER);

	                            sendPingReqToServer(c);

	                            nextPingReqTick = latestTick + pingRequestInterval;
	                        }

	                        /*
	                         * Time to run the business-logic !!
	                         */
	                        if(latestTick >= nextBusinessLogicTick)
	                        {
	                            if(coreLoopyBusinessLogicInitiatedBySelf != NULL)

	                            {
	                                coreLoopyBusinessLogicInitiatedBySelf(NULL);
	                            }

	                            nextBusinessLogicTick = latestTick + businessLogicInterval;
	                            break;
	                        }
	                    }
	                }
	            }

	            /* This is 1 means physical-socket is fine, AND connection to InstaMsg-Server is fine at protocol level. */
	            if(c->connected == 1)
	            {
	            }
	            else if((c->ipstack).socketCorrupted == 0)
	            {
	                static int connectionAttempts = 0;
	                connectionAttempts++;

	                sg_sprintf(LOG_GLOBAL_BUFFER, "Socket is fine at physical layer, but no connection established (yet) with InstaMsg-Server.");
	                error_log(LOG_GLOBAL_BUFFER);

	                if(connectionAttempts > MAX_CONN_ATTEMPTS_WITH_PHYSICAL_LAYER_FINE)
	                {
	                    connectionAttempts = 0;

	                    sg_sprintf(LOG_GLOBAL_BUFFER,
	                              "Connection-Attempts exhausted ... so trying with re-initializing the socket-physical layer.");
	                    error_log(LOG_GLOBAL_BUFFER);

	                    (c->ipstack).socketCorrupted = 1;
	                }
	            }

	            if((c->ipstack).socketCorrupted == 1)
	            {
	                clearInstaMsg(instaMsg);
	                break;
	            }
	        }
	    }
	}
	
	public void initInstaMsg(InstamsgEventHandler handler){
		int i;

		Globals.config.init_config();
		if(Config.FILE_SYSTEM_INTERFACE_ENABLED){
			init_file_system(singletonUtilityFs, "");
		}
		
		(c->ipstack).socketCorrupted = 1;
		init_socket(&(c->ipstack), INSTAMSG_HOST, INSTAMSG_PORT);
		if((c->ipstack).socketCorrupted ==1)
		{
		  return;
		}
		
		
		for (i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
		{
		  c->messageHandlers[i].msgId = 0;
		  c->messageHandlers[i].topicFilter = 0;
		
		  c->resultHandlers[i].msgId = 0;
		  c->resultHandlers[i].timeout = 0;
		
		  c->oneToOneResponseHandlers[i].msgId = 0;
		  c->oneToOneResponseHandlers[i].timeout = 0;
		}
		
		c->defaultMessageHandler = NULL;
		c->next_packetid = MAX_PACKET_ID;
		c->onConnectCallback = connectHandler;
		c->onDisconnectCallback = disconnectHandler;
		c->oneToOneMessageHandler = oneToOneMessageHandler;
		
		c->serverLoggingEnabled = 0;
		
		c->connectOptions.willFlag = 0;
		c->connectOptions.MQTTVersion = 3;
		c->connectOptions.cleansession = 1;
		
		
		memset(c->clientIdComplete, 0, sizeof(c->clientIdComplete));
		strcpy(c->clientIdComplete, "");
		
		memset(c->clientIdMachine, 0, sizeof(c->clientIdMachine));
		strcpy(c->clientIdMachine, NO_CLIENT_ID);
		c->connectOptions.clientID.cstring = c->clientIdMachine;
		
		memset(c->username, 0, sizeof(c->username));
		strcpy(c->username, "");
		c->connectOptions.username.cstring = c->username;
		
		memset(c->password, 0, sizeof(c->password));
		get_device_uuid(c->password, sizeof(c->password));
		c->connectOptions.password.cstring = c->password;
		
		c->connected = 0;
		MQTTConnect(c);
	}
	
	public void init_file_system(FileSystem fs, String arg) {
		
		fs.setCallbackHandler(new FileSystemCallbackImpl());
		fs.setFileName(arg);
	    fs.connect_underlying_file_system_medium_guaranteed();
	}
	
	public void release_file_system(FileSystem fs)
	{
	    fs.release_underlying_file_system_medium_guaranteed();
	}
	
	void readAndProcessIncomingMQTTPacketsIfAny()
	{
	    int rc = FAILURE;
	    do
	    {
	        int len = 0;
	        MQTTFixedHeader fixedHeader;

	        rc = readPacket(c, &fixedHeader);
	        if(rc != SUCCESS)
	        {
	            return;
	        }

	        switch (fixedHeader.packetType)
	        {
	            case CONNACK:
	            {
	                unsigned char connack_rc = 255;
	                char sessionPresent = 0;
	                if (MQTTDeserialize_connack((unsigned char*)&sessionPresent, &connack_rc, c->readbuf, sizeof(c->readbuf)) == 1)
	                {
	                    handleConnOrProvAckGeneric(c, connack_rc);
	                }

	                break;
	            }

	            case PROVACK:
	            {
	                MQTTMessage msg;
	                unsigned char connack_rc = 255;
	                char sessionPresent = 0;
	                if (MQTTDeserialize_provack((unsigned char*)&sessionPresent,
	                                             &connack_rc,
	                                             (unsigned char**)&msg.payload,
	                                             (int*)&msg.payloadlen,
	                                             c->readbuf,
	                                             sizeof(c->readbuf)) == 1)
	                {
	                    if(connack_rc == 0x00)  /* Connection Accepted */
	                    {
	                        memcpy(c->clientIdComplete, msg.payload, msg.payloadlen);

	                        sg_sprintf(LOG_GLOBAL_BUFFER, "Received client-id from server via PROVACK [%s]", c->clientIdComplete);
	                        info_log(LOG_GLOBAL_BUFFER);

	                        setValuesOfSpecialTopics(c);

	                        handleConnOrProvAckGeneric(c, connack_rc);
	                    }
	                }

	                break;
	            }

	            case PUBACK:
	            {
	                fireResultHandlerUsingMsgIdAsTheKey(c);
	                break;
	            }

	            case SUBACK:
	            {

	                /*
	                * Remove the message-handlers, if the server was unable to process the subscription-request.
	                */
	                int count = 0, grantedQoS = -1;
	                unsigned short msgId;

	                fireResultHandlerUsingMsgIdAsTheKey(c);

	                if (MQTTDeserialize_suback(&msgId, 1, &count, &grantedQoS, c->readbuf, sizeof(c->readbuf)) != 1)
	                {
	                    goto exit;
	                }

	                if (grantedQoS == 0x80)
	                {
	                    int i;
	                    for (i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
	                    {
	                        if (c->messageHandlers[i].msgId == msgId)
	                        {
	                            c->messageHandlers[i].topicFilter = 0;
	                            break;
	                        }
	                    }
	                }

	                break;
	            }

	            case PUBLISH:
	            {
	                MQTTString topicMQTTString;
	                MQTTMessage msg;
	                char *topicName;
	                unsigned char memoryAllocatedSynamicaaly = 0;

	                if (MQTTDeserialize_publish(&(msg.fixedHeaderPlusMsgId),
	                                            &topicMQTTString,
	                                            (unsigned char**)&msg.payload,
	                                            (int*)&msg.payloadlen,
	                                            c->readbuf,
	                                            sizeof(c->readbuf)) != SUCCESS)
	                {
	                    goto exit;
	                }

	                /*
	                 * At this point, "msg.payload" contains the real-stuff that is passed from the peer ....
	                 */
	                topicName = topicMQTTString.lenstring.data;

	                /*
	                 * Sometimes, topic-name and payload are not separated by above algo.
	                 * So, do another check
	                 */
	                if(strstr(topicName, msg.payload) != NULL)
	                {
	                    topicName = (char*) sg_malloc(MAX_BUFFER_SIZE);
	                    if(topicName == NULL)
	                    {
	                        sg_sprintf(LOG_GLOBAL_BUFFER, "Could not allocate memory for topic");
	                        error_log(LOG_GLOBAL_BUFFER);

	                        goto publish_exit;
	                    }
	                    else
	                    {
	                        memoryAllocatedSynamicaaly = 1;
	                        strncpy(topicName, topicMQTTString.lenstring.data, strlen(topicMQTTString.lenstring.data) - msg.payloadlen);
	                    }
	                }

	                if(topicName != NULL)
	                {
	                    if(strcmp(topicName, c->filesTopic) == 0)
	                    {
	                        handleFileTransfer(c, &msg);
	                    }
	                    else if(strcmp(topicName, c->enableServerLoggingTopic) == 0)
	                    {
	                        serverLoggingTopicMessageArrived(c, &msg);
	                    }
	                    else if(strcmp(topicName, c->rebootTopic) == 0)
	                    {
	                        rebootDevice();
	                    }
	                    else if(strcmp(topicName, c->clientIdComplete) == 0)
	                    {
	                        oneToOneMessageArrived(c, &msg);
	                    }
	                    else if(strcmp(topicName, c->receiveConfigTopic) == 0)
	                    {
	                        handleConfigReceived(c, &msg);
	                    }
	                    else
	                    {
	                        deliverMessageToSelf(c, &topicMQTTString, &msg);
	                    }
	                }
	                else
	                {
	                    deliverMessageToSelf(c, &topicMQTTString, &msg);
	                }

	publish_exit:
	                if(memoryAllocatedSynamicaaly == 1)
	                {
	                    if(topicName)
	                        sg_free(topicName);
	                }

	                break;
	            }

	            case PUBREC:
	            {
	                int msgId = fireResultHandlerUsingMsgIdAsTheKey(c);

	                Globals.RESET_GLOBAL_BUFFER();
	                if ((len = MQTTSerialize_ack(GLOBAL_BUFFER, sizeof(GLOBAL_BUFFER), PUBREL, 0, msgId)) <= 0)
	                {
	                    goto exit;
	                }

	                attachResultHandler(c, msgId, MQTT_RESULT_HANDLER_TIMEOUT, publishQoS2CycleCompleted);
	                sendPacket(c, GLOBAL_BUFFER, len); /* send the PUBREL packet */

	                break;
	            }

	            case PUBCOMP:
	            {
	                fireResultHandlerUsingMsgIdAsTheKey(c);
	                break;
	            }

	            case PINGRESP:
	            {
	                sg_sprintf(LOG_GLOBAL_BUFFER, "PINGRESP received... relations are intact !!\n");
	                info_log(LOG_GLOBAL_BUFFER);

	                break;
	            }
	        }
	    } while(rc == SUCCESS); /* Keep reading packets till the time we are receiving packets fine. */

	exit:
	        return;
	}
	
	void removeExpiredOneToOneResponseHandlers()
	{
	    int i;
	    for (i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
	    {
	        checkAndRemoveExpiredHandler(oneToOneResponseHandlers[i].msgId, oneToOneResponseHandlers[i].timeout, "one-to-one response");
	    }
	}
	
	void removeExpiredResultHandlers(){
	    int i;
	    for (i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
	    {
	        checkAndRemoveExpiredHandler(resultHandlers[i].msgId, resultHandlers[i].timeout, "pub/sub response");
	    }
	}
	
	void checkAndRemoveExpiredHandler(int msgId, int timeout, final String info)
	{
	    if(msgId == 0)
	    {
	        return;
	    }

	    if(timeout <= 0)
	    {
	        Log.info_log("No "+info+" received for msgid ["+msgId+"], removing..");
	        msgId = 0;
	    }
	    else
	    {
	        timeout = timeout - 1;
	    }
	}
}


class MessageHandlers{
	int msgId;
	int timeout;
	String topicFilter;
	MessageHandler<MessageData> handler;
}

class ResultHandlers{
	int msgId;
	int timeout;
	MessageHandler<MQTTFixedHeaderPlusMsgId> handler;
}

class OneToOneHandlers{
	int msgId;
	int timeout;
	MessageHandler<OneToOneResult> handler;
}

interface MessageHandler<T>{
	void onMessage(T message);
}

class MQTTFixedHeader{
    int packetType;
    boolean dup;
    MQTTPacket.QoS qos;
    boolean retain;
}

class MQTTFixedHeaderPlusMsgId{
    MQTTFixedHeader fixedHeader;
    int msgId;
}

class MessageData{
    MQTTMessage message;
    MQTTString topicName;
}

class MQTTMessage{
    MQTTFixedHeaderPlusMsgId fixedHeaderPlusMsgId;
    String payload;
    int payloadlen;
}
