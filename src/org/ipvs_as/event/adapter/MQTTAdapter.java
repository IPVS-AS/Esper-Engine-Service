package org.ipvs_as.event.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.ipvs_as.engine.IEngineCallback;

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class MQTTAdapter implements MqttCallback {

    private static final Log log = LogFactory.getLog(MQTTAdapter.class);
    private MqttClient client;
    private IEngineCallback engineCallback = null;
    private String brokerURL;
    private String name;

    public MQTTAdapter(final String brokerURL, final IEngineCallback engineCallback) {
	this.brokerURL = brokerURL;
	this.engineCallback = engineCallback;
	this.name = MqttClient.generateClientId();
    }

    public MQTTAdapter(final String brokerURL) {
	this.brokerURL = brokerURL;
	this.name = MqttClient.generateClientId();
    }

    @Override
    public void connectionLost(Throwable arg0) {
	System.out.println("connectionLost. ");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
	System.out.println("deliveryComplete. ");
	stop();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
	System.out.println("messageArrived " + message);
	String payload = new String(message.getPayload());

	if (engineCallback != null) {
	    engineCallback.sendEvent(payload);
	}
    }

    /**
     * connects to MQTT Broker and subscribe to topics
     */
    public void connect() {

	if (this.brokerURL == null || this.brokerURL.isEmpty()) {
	    System.out.println("ERROR: broker url cannot be null");
	} else {
	    try {
		client = new MqttClient(this.brokerURL, this.name);
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(false);
		options.setAutomaticReconnect(true);
		client.connect(options);
		client.setCallback(this);

	    } catch (IllegalArgumentException e) {
		System.out.println("Illegal Arguments" + e);
	    } catch (MqttException e) {
		System.out.println("NO CONNECTION TO MQTT BROKER" + e);
	    }
	}
    }

    public void bind(final String[] topics) {
	this.connect();
	try {
	    if (topics.length == 0) {
		System.out.println("ERROR: there is no topics to subscribe");
	    } else {
		client.subscribe(topics);
	    }
	} catch (MqttException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void publish(final String topic, String message) {
	try {
	    client.publish(topic, message.getBytes(), 0, false);

	} catch (MqttPersistenceException e) {
	    // TODO Auto-generated catch block
	    System.out.println(e);
	} catch (MqttException e) {
	    // TODO Auto-generated catch block
	    System.out.println(e);
	}
    }

    public void stop() {
	try {
	    client.disconnect();
	} catch (MqttException e) {
	    log.error("error while disconneting from MQTT broker", e);

	}
    }

    public String getName() {
	return this.name;
    }
}
