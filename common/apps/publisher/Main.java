package common.apps.publisher;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import utils.ChangeableString;
import utils.Config;
import common.apps.utils.PublisherSubscriberInit;


public class Main {
	
	public static void main(String[] args) {
		
		/*
		ChangeableString logFilePath = new ChangeableString(null);
		
		if(Config.FILE_SYSTEM_INTERFACE_ENABLED){
			logFilePath.changeTo(Globals.LOG_FILE_PATH);
		}
		
		PublisherSubscriberInit.initPublisherSubscriberParams(args, logFilePath);
		common.instamsg.driver.Globals.globalSystemInit(logFilePath.toString());
		Instamsg instamsg = new Instamsg();
		instamsg.start(null, 3);
		*/
		
		
        String topic        = "listener_topic";
        String content      = "Message from MqttPublishSample";
        int qos             = 2;
        String broker       = "tcp://platform.instamsg.io:1883";
        String clientId     = "1ce2ced0-8149-11e5-8f9f-bc764e102b63";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}
}
