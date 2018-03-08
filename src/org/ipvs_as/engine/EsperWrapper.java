package org.ipvs_as.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ipvs_as.event.adapter.DataConnector;
import org.ipvs_as.event.adapter.HTTPAdapter;
import org.ipvs_as.event.adapter.MQTTAdapter;
import org.ipvs_as.event.adapter.OrionAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementState;
import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.client.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class EsperWrapper implements IEngineCallback {
    // Constants
    public final static String PROTOCOL_MQTT = "MQTT";
    public final static String PROTOCOL_HTTP = "HTTP";
    public final static String PROTOCOL_HTTP_ORION = "HTTP-ORION";

    private final EPServiceProvider epService;
    private ObjectMapper mapper = new ObjectMapper();
    private static final EsperWrapper instance = new EsperWrapper();

    private Map<String, DataConnector> dataSources = new HashMap<String, DataConnector>();
    private Map<String, Object> adapters = new HashMap<String, Object>();
    private Map<String, QuerySubscriber> querySubscribers = new HashMap<String, QuerySubscriber>();

    private EsperWrapper() {

	epService = EPServiceProviderManager.getDefaultProvider();
	epService.initialize();
    }

    public static EsperWrapper getInstance() {
	return instance;
    }

    public String createQuery(String message) {
	try {
	    Query queryObj = mapper.readValue(message, Query.class);

	    String queryStr = queryObj.getQuery();
	    DataConnector dataConnector = queryObj.getSubscriber();

	    // creates and starts running the query
	    EPStatement epStatement = epService.getEPAdministrator().createEPL(queryStr);
	    String queryID = epStatement.getName();

	    if (dataConnector != null) {
		QuerySubscriber querySubscriber = new QuerySubscriber(queryID);
		querySubscriber.addDataConnector(dataConnector);
		epStatement.setSubscriber(querySubscriber); // sets subscriber
		this.querySubscribers.put(queryID, querySubscriber);
	    }

	    return queryID;
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;
    }

    public String stopQuery(String query_id) {
	EPStatement epStatement = epService.getEPAdministrator().getStatement(query_id);
	if (epStatement != null && epStatement.getState() == EPStatementState.STARTED) {
	    epStatement.stop();
	    return epStatement.getState().name();
	} else {
	    return EPStatementState.DESTROYED.name();
	}
    }

    public String startQuery(String query_id) {
	EPStatement epStatement = epService.getEPAdministrator().getStatement(query_id);
	if (epStatement != null && epStatement.getState() == EPStatementState.STOPPED) {
	    epStatement.start();
	    return epStatement.getState().name();
	} else {
	    return EPStatementState.DESTROYED.name();
	}
    }

    public String getQueryState(String query_id) {
	EPStatement epStatement = epService.getEPAdministrator().getStatement(query_id);
	if (epStatement != null) {
	    return epStatement.getState().name();
	} else {
	    return EPStatementState.DESTROYED.name();
	}
    }

    public String deleteQuery(String query_id) {
	EPStatement epStatement = epService.getEPAdministrator().getStatement(query_id);
	if (epStatement != null && epStatement.getState() != EPStatementState.DESTROYED) {
	    epStatement.destroy();
	    return epStatement.getState().name();
	} else {
	    return EPStatementState.DESTROYED.name();
	}
    }

    public String getStatus() {
	StringBuilder sb = new StringBuilder("Esper is running!!!\n\n");
	sb.append("Configured event types: \n\n");
	sb.append(getEventTypes());
	sb.append("\n\n");
	sb.append("Configured continuous queries: \n\n");
	sb.append(getQueries());
	return sb.toString();
    }

    public String getQueries() {
	String[] currentQueries = epService.getEPAdministrator().getStatementNames();

	JSONArray queries = new JSONArray();
	for (String queryName : currentQueries) {
	    String queryState = epService.getEPAdministrator().getStatement(queryName).getState().name();
	    queries.put(new JSONObject().put("query_id", queryName).put("status", queryState));

	}
	return queries.toString();
    }

    /**
     * By adding a data source, an input adapter is instantiated to extract data
     * from this data source and to forward the data to the Esper engine.
     * 
     * Example of dataSource format:
     * 
     * {"protocol": "MQTT", "endpoint":"tcp://192.168.209.190:1883",
     * "topics":["TemperatureEvent"]}
     * 
     * @param dataSource
     */
    public String addDataSource(String dataSource) {
	try {
	    DataConnector dataSourceObj = mapper.readValue(dataSource, DataConnector.class);
	    String dataSourceProtocol = dataSourceObj.getProtocol();

	    if (dataSourceProtocol != null) {
		if (PROTOCOL_MQTT.equals(dataSourceProtocol.toUpperCase())) {
		    MQTTAdapter adapter = new MQTTAdapter(dataSourceObj.getEndpoint(), this);
		    adapter.bind(dataSourceObj.getTopics());

		    dataSources.put(adapter.getName(), dataSourceObj);
		    adapters.put(adapter.getName(), adapter);

		    return adapter.getName();

		} else if (PROTOCOL_HTTP.equals(dataSourceProtocol.toUpperCase())) {
		    HTTPAdapter adapter = new HTTPAdapter(dataSourceObj.getEndpoint(), this);
		    adapter.bind(dataSourceObj.getTopics());
		    dataSources.put(adapter.getName(), dataSourceObj);
		    adapters.put(adapter.getName(), adapter);
		    return adapter.getName();

		} else if (PROTOCOL_HTTP_ORION.equals(dataSourceProtocol.toUpperCase())) {
		    OrionAdapter adapter = new OrionAdapter(dataSourceObj.getEndpoint(), this);
		    adapter.bind(dataSourceObj.getTopics());
		    dataSources.put(adapter.getName(), dataSourceObj);
		    adapters.put(adapter.getName(), adapter);
		    return adapter.getName();

		}
	    }

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return "";
    }

    public String getDataSources() {
	Iterator<Entry<String, DataConnector>> it = dataSources.entrySet().iterator();
	JSONArray dataSourcesArray = new JSONArray();
	while (it.hasNext()) {
	    Map.Entry<String, DataConnector> pair = it.next();
	    JSONObject ob = new JSONObject(pair.getValue());
	    dataSourcesArray.put(new JSONObject().put(pair.getKey(), ob));
	}
	return dataSourcesArray.toString();
    }

    public String getDataSourceState(String datasource_id) {
	DataConnector dataSource = dataSources.get(datasource_id);
	if (dataSource != null) {
	    try {
		return mapper.writeValueAsString(dataSource);
	    } catch (JsonProcessingException e) {
		return new JSONObject().toString();
	    }
	} else {
	    return new JSONObject().toString();
	}
    }

    public String removeDataSource(String datasource_id) {
	DataConnector dataSource = dataSources.get(datasource_id);
	if (dataSource != null) {
	    if (dataSource.getProtocol().equals(PROTOCOL_MQTT)) {
		MQTTAdapter adapter = (MQTTAdapter) adapters.get(datasource_id);
		adapter.stop();
		adapters.remove(datasource_id);
		dataSources.remove(datasource_id);
	    }
	    try {
		return mapper.writeValueAsString(dataSource);
	    } catch (JsonProcessingException e) {
		return new JSONObject().toString();
	    }
	} else {
	    return new JSONObject().toString();
	}
    }

    public String subscribeToQuery(String query_id, String dataSink) {
	String subscription_id = "";
	try {
	    DataConnector obj = mapper.readValue(dataSink, DataConnector.class);
	    QuerySubscriber subscriber = querySubscribers.get(query_id);
	    if (subscriber != null) {
		subscription_id = subscriber.addDataConnector(obj);
	    } else {
		QuerySubscriber querySubscriber = new QuerySubscriber(query_id);
		subscription_id = querySubscriber.addDataConnector(obj);
		EPStatement epStatement = epService.getEPAdministrator().getStatement(query_id);
		epStatement.setSubscriber(querySubscriber); // sets subscriber
		this.querySubscribers.put(query_id, querySubscriber);
	    }

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return subscription_id;
	}
	return subscription_id;
    }

    public String getEventTypes() {
	EventType[] eventTypes = epService.getEPAdministrator().getConfiguration().getEventTypes();

	JSONArray eventTypesArray = new JSONArray();
	for (EventType eventType : eventTypes) {
	    if (eventType.getName().equals("java.lang.Object")) {
		continue;
	    }
	    // String properties = "";
	    JSONObject properties = new JSONObject();
	    for (EventPropertyDescriptor propertyDescriptor : eventType.getPropertyDescriptors()) {
		properties.put(propertyDescriptor.getPropertyName(),
			propertyDescriptor.getPropertyType().getSimpleName());
	    }
	    eventTypesArray
		    .put(new JSONObject().put("eventtype_id", eventType.getName()).put("properties", properties));

	}
	return eventTypesArray.toString();
    }

    public void sendEvent(String message) {

	JSONObject eventJSON = new JSONObject(message);
	String[] names = JSONObject.getNames(eventJSON);

	for (String eventName : names) {
	    Map<String, Object> event = new HashMap<String, Object>();
	    JSONObject dataJson = (JSONObject) eventJSON.get(eventName);
	    String[] attNames = JSONObject.getNames(dataJson);

	    for (String attName : attNames) {
		event.put(attName, dataJson.get(attName));
	    }

	    epService.getEPRuntime().sendEvent(event, eventName);
	}
    }

    public String getSubscriptions(String query_id) {
	QuerySubscriber subscriber = querySubscribers.get(query_id);
	JSONArray subscriptionsArray = new JSONArray();
	if (subscriber != null) {
	    Iterator<Entry<String, DataConnector>> it = subscriber.getDataConnectors().entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry<String, DataConnector> pair = it.next();
		JSONObject ob = new JSONObject(pair.getValue());
		subscriptionsArray.put(new JSONObject().put(pair.getKey(), ob));
	    }
	}
	return subscriptionsArray.toString();
    }

    public String removeSubscription(String query_id, String subscription_id) {
	QuerySubscriber subscriber = querySubscribers.get(query_id);
	if (subscriber != null) {
	    if (subscriber.getDataConnectors().containsKey(subscription_id)) {
		DataConnector dc = subscriber.getDataConnectors().remove(subscription_id);
		JSONObject ob = new JSONObject(dc);
		return new JSONObject().put(subscription_id, ob).toString();
	    }
	}
	return null;
    }

    public String createEventType(String message) {
	JSONObject msgJsonObj = new JSONObject(message);
	String query = (String) msgJsonObj.get("eventtype");

	if (query != null && !query.isEmpty()) {
	    // creates and starts running the query
	    EPStatement epStatement = epService.getEPAdministrator().createEPL(query);
	    String queryID = epStatement.getName();

	    return queryID;
	}
	return null;
    }

}
