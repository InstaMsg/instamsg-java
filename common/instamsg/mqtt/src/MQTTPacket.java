package common.instamsg.mqtt.src;

public class MQTTPacket {
	public static enum QoS { QOS0, QOS1, QOS2 };
	
	public static class MQTTString{
		String cstring;
		MQTTLenString lenstring;
	}
	
	public static class MQTTLenString{
		int len;
		String data;
	}
}



