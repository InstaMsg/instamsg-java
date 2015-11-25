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
	
	public static InstaMsg instaMsg = new InstaMsg();
	
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
		return ReturnCode.FAILURE;
	}
	
	public static ReturnCode MQTTConnect(InstaMsg c) {
		
		MqttConnect connectMsg = new MqttConnect(c.connectOptions);
		
		byte[] packet = getEncodedMqttMessageAsByteStream(connectMsg);
		if(packet == null) {
			return ReturnCode.FAILURE;
		}
		
		return sendPacket(c, packet);
	}	

	public static void initInstaMsg(InstaMsg c, InitialCallbacks callbacks, String platform) {

		modulesProvideInterface = ModulesProviderFactory.getModulesProvider(platform);
		
		c.socket = modulesProvideInterface.getSocket(Globals.INSTAMSG_HOST, Globals.INSTAMSG_PORT);		
		c.socket.socketCorrupted = true;
		
		c.socket.initSocket();
		if(c.socket.socketCorrupted == true) {
			return;
		}


		for (int i = 0; i < MAX_MESSAGE_HANDLERS; ++i)
		{
			c.messageHandlers[i].msgId = 0;
			c.messageHandlers[i].topicFilter = null;

			c.resultHandlers[i].msgId = 0;
			c.resultHandlers[i].timeout = 0;

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
	
    int packetType;
    boolean dup;
    int qos;
    boolean retain;
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
