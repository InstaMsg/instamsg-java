package common.instamsg.driver;

public class MessageData {

	String topicName;
	String payload;
	
	public MessageData(String topicName, String payload) {
		super();
		this.topicName = topicName;
		this.payload = payload;
	}
	
	public String getTopicName() {
		return topicName;
	}
	public String getPayload() {
		return payload;
	}
}
