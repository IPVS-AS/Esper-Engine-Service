package org.ipvs_as.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.ipvs_as.event.adapter.DataConnector;
import org.ipvs_as.event.adapter.HTTPAdapter;
import org.ipvs_as.event.adapter.MQTTAdapter;
import org.ipvs_as.event.adapter.OrionAdapter;
import org.json.JSONObject;

/**
 * This class represents a subscriber to a query (EPL statement) and gets
 * notified when the query matches.
 * 
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class QuerySubscriber {
    private UUID uuid = UUID.randomUUID();
    private String query_id;
    private Map<String, DataConnector> dataConnectors = new HashMap<String, DataConnector>();

    // Constructors
    public QuerySubscriber(final String query_id) {
	this.query_id = query_id;
    }

    public DataConnector removeDataConnector(final String subscription_id) {
	return dataConnectors.remove(subscription_id);
    }

    public String addDataConnector(DataConnector subscriber) {
	String subscription_id = "sub" + System.currentTimeMillis();
	dataConnectors.put(subscription_id, subscriber);
	return subscription_id;
    }

    public Map<String, DataConnector> getDataConnectors() {
	return dataConnectors;
    }

    /**
     * Notification from Esper are received here in this method.
     * 
     * @param row
     */
    public void update(Map<String, Object> row) {
	// processing is done in another thread to avoid blocking Esper
	new Thread(new Runnable() {
	    public void run() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n============= ALERT - QUERY MATCHED  ===============");
		sb.append("\n subscriber = " + uuid);

		JSONObject root = new JSONObject();
		root.put("query_id", query_id);
		JSONObject eventProperties = new JSONObject();
		root.put("event", eventProperties);

		Iterator<Entry<String, Object>> it = row.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry<String, Object> pair = it.next();
		    eventProperties.put(pair.getKey(), pair.getValue());
		}
		System.out.println("Message to subscribers:" + root.toString());
		for (DataConnector dataSink : dataConnectors.values()) {
		    if (EsperWrapper.PROTOCOL_MQTT.equalsIgnoreCase(dataSink.getProtocol())) {
			MQTTAdapter adapter = new MQTTAdapter(dataSink.getEndpoint());
			adapter.connect();
			for (String topic : dataSink.getTopics()) {
			    adapter.publish(topic, root.toString());
			}
			adapter.stop();
		    } else if (EsperWrapper.PROTOCOL_HTTP.equalsIgnoreCase(dataSink.getProtocol())) {
			HTTPAdapter adapter = new HTTPAdapter(dataSink.getEndpoint());
			for (String topic : dataSink.getTopics()) {
			    adapter.publish(topic, root.toString());
			}
		    } else if (EsperWrapper.PROTOCOL_HTTP_ORION.equalsIgnoreCase(dataSink.getProtocol())) {
		    	OrionAdapter adapter = null;
		    	if(dataSink.getHeaders() != null) {
		    		adapter = new OrionAdapter(dataSink.getEndpoint(), dataSink.getHeaders());		    		
		    	} else {
		    		adapter = new OrionAdapter(dataSink.getEndpoint());
		    	}
			for (String topic : dataSink.getTopics()) {
			    adapter.publish(topic, root.toString());
			}
		    }
		}

		sb.append("\n=============== END ALERT ==========================\n");
		System.out.println(sb.toString());
	    }
	}).start();
    }

}
