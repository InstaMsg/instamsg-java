package common.instamsg.driver;



import common.instamsg.driver.Globals.ReturnCode;
import common.instamsg.driver.include.ModulesProviderFactory;
import common.instamsg.driver.include.ModulesProviderInterface;
import common.instamsg.driver.include.OneToOneResult;
import common.instamsg.driver.include.Socket;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.MqttException;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import common.instamsg.mqtt.org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;


public class InstaMsg {
	
	private static InstaMsg instaMsg;;
	
	static int MAX_MESSAGE_HANDLERS = 5;
	static int MAX_PACKET_ID = 10000;
	static String NO_CLIENT_ID = "NONE";
	

	static ModulesProviderInterface modulesProvideInterface;
	
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
	
	
	
	public static void log(String log) {
		System.out.println(log);
	}
	
	public static void infoLog(String log) {
		log(log);
	}
	
	public static void errorLog(String log) {
		log(log);
	}
	
	static {
		
	}
	
	
	private static byte[] getEncodedMqttMessageAsByteStream(MqttWireMessage message) {
		
		byte[] bytes;
		try {
			bytes = message.getHeader();
			
		} catch (MqttException e) {
			errorLog("Could not fetch header from message.");
			return null;
		}
		
		byte[] pl;
		try {
			pl = message.getPayload();
			
		} catch (MqttException e) {
			errorLog("Could not fetch payload from message");
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
			errorLog("Socket not available at physical layer .. so packet cannot be sent to server.");
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
			
			errorLog("Socket not available at physical layer .. so packet cannot be read from server.");
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
	    System.out.println("Total length of packet received = " + (len + rem_len));
	    return ReturnCode.SUCCESS;
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
				System.out.println("MIL GAYA PROVACK");
				System.exit(0);
			} else {
				System.out.println("KUCHH AUR MILA .. BYE BYE " + fixedHeader.packetType);
				System.exit(0);
			}
		
		} while (rc == ReturnCode.SUCCESS);
		
		
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

	
	public static void main(String[] args) {
		
		instaMsg = new InstaMsg();
		modulesProvideInterface = ModulesProviderFactory.getModulesProvider("linux");

		boolean socketReadJustNow = false;
		
		while(true) {
			
			initInstaMsg(instaMsg, null);			
			infoLog("Device-UUID :: [" + modulesProvideInterface.getMisc().getDeviceUuid() + "]");
			
			while(true) {
				
				socketReadJustNow = false;
				
				if(instaMsg.socket.socketCorrupted == true) {
					errorLog("Socket not available at physical layer .. so nothing can be read from socket.");
					
				} else {
					readAndProcessIncomingMQTTPacketsIfAny(instaMsg);
					socketReadJustNow = true;
				}
				
				if(true) {
					
				}
				
	            if(instaMsg.socket.socketCorrupted == true) {
	            	
	            } else if(instaMsg.socket.socketCorrupted == false) {
	            	
	                instaMsg.connectionAttempts++;
	                errorLog("Socket is fine at physical layer, but no connection established (yet) with InstaMsg-Server.");

	                if(instaMsg.connectionAttempts > Globals.MAX_CONN_ATTEMPTS_WITH_PHYSICAL_LAYER_FINE)
	                {
	                    instaMsg.connectionAttempts = 0;
	                    
	                    errorLog("Connection-Attempts exhausted ... so trying with re-initializing the socket-physical layer.");
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
}


class MessageHandlers {
	
	int msgId;
	int timeout;
	String topicFilter;
	MessageHandler<MessageData> handler;
}

class ResultHandlers {
	
	int msgId;
	int timeout;
	MessageHandler<MQTTFixedHeaderPlusMsgId> handler;
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

class MQTTFixedHeaderPlusMsgId {
	
    MQTTFixedHeader fixedHeader;
    int msgId;
}

class MessageData {
	
    MQTTMessage message;
    String topicName;
}

class MQTTMessage {
	
    MQTTFixedHeaderPlusMsgId fixedHeaderPlusMsgId;
    String payload;
    int payloadlen;
}
