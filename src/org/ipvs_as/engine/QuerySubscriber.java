package org.ipvs_as.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ipvs_as.event.adapter.DataConnector;
import org.ipvs_as.event.adapter.MQTTAdapter;
import org.ipvs_as.event.types.DistanceMeterEvent;
import org.ipvs_as.event.types.SoundStrength;
import org.ipvs_as.event.types.TemperatureEvent;

import com.espertech.esper.client.EventBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents a subscriber to a query (EPL statement) and gets
 * notified when the query matches.
 * 
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class QuerySubscriber {
    private UUID uuid = UUID.randomUUID();
    private ObjectMapper mapper = new ObjectMapper();
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
		sb.append("\n=============== ALERT - QUERY MATCHED  ===============");
		sb.append("\n subscriber = " + uuid);

		String jsonRoot = "{\"query_id\": \"" + query_id + "\"";

		if (row.size() > 0) {
		    jsonRoot = jsonRoot + ", \"events\": [";

		    List<String> eventsList = new ArrayList<String>();
		    for (Object event : row.values()) {
			if (event instanceof EventBean) {
			    EventBean eventBean = (EventBean) event;
			    if (DistanceMeterEvent.class.getSimpleName()
				    .equalsIgnoreCase(eventBean.getEventType().getName())) {

				DistanceMeterEvent eventObj = (DistanceMeterEvent) eventBean.getUnderlying();
				sb.append("\n event = " + eventObj.toString());
				try {
				    eventsList.add(mapper.writeValueAsString(eventObj));
				} catch (JsonProcessingException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}

			    } else if (TemperatureEvent.class.getSimpleName()
				    .equalsIgnoreCase(eventBean.getEventType().getName())) {
				TemperatureEvent eventObj = (TemperatureEvent) eventBean.getUnderlying();
				try {
				    eventsList.add(mapper.writeValueAsString(eventObj));
				} catch (JsonProcessingException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}

			    } else if (SoundStrength.class.getSimpleName()
				    .equalsIgnoreCase(eventBean.getEventType().getName())) {
				SoundStrength eventObj = (SoundStrength) eventBean.getUnderlying();
				try {
				    eventsList.add(mapper.writeValueAsString(eventObj));
				} catch (JsonProcessingException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}

			    } else {
				sb.append("\n FIXME " + DistanceMeterEvent.class.getSimpleName() + " != "
					+ eventBean.getEventType().getName());
			    }

			    // TODO other event types
			} else if (event instanceof DistanceMeterEvent) {
			    DistanceMeterEvent eventObj = (DistanceMeterEvent) event;
			    sb.append("\n event = " + eventObj.toString());
			    try {
				eventsList.add(mapper.writeValueAsString(eventObj));
			    } catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			} else if (event instanceof TemperatureEvent) {
			    TemperatureEvent eventObj = (TemperatureEvent) event;
			    sb.append("\n event = " + eventObj.toString());
			    try {
				eventsList.add(mapper.writeValueAsString(eventObj));
			    } catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			} else {
			    sb.append("\n event = " + event);
			}
		    }
		    for (String eventStr : eventsList) {
			jsonRoot = jsonRoot + eventStr + ",";
		    }
		    jsonRoot = jsonRoot.substring(0, jsonRoot.lastIndexOf(","));
		    jsonRoot = jsonRoot + "]";
		}
		jsonRoot = jsonRoot + "}";
		System.out.println(jsonRoot);

		for (DataConnector dataSource : dataConnectors.values()) {
		    if (EsperWrapper.PROTOCOL_MQTT.equalsIgnoreCase(dataSource.getProtocol())) {
			MQTTAdapter adapter = new MQTTAdapter(dataSource.getEndpoint());
			adapter.connect();
			for (String topic : dataSource.getTopics()) {
			    adapter.publish(topic, jsonRoot);
			}
			adapter.stop();
		    } else if (EsperWrapper.PROTOCOL_HTTP.equalsIgnoreCase(dataSource.getProtocol())) {
			// TODO
		    }
		}

		sb.append("\n=============== END ALERT =============================\n");
		System.out.println(sb.toString());
	    }
	}).start();
    }
}
