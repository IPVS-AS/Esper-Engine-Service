package org.ipvs_as.engine;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    //
    
    private String brokerURL = "http://81.14.203.235:1026/v2/entities/";
    
    
    protected String[] headers = {"FIWARE-Service=hackboi"};
    
    private String parseEntityId(final String topic) {
		return topic.substring(0,topic.indexOf("/attrs/"));		
	}
	
	private String parseAttributeName(final String topic) {
		return topic.substring(topic.indexOf("/attrs/") +  "/attrs/".length(), topic.indexOf("/value"));
	}
	
	protected Map<String,String> parseHeadersList() {
		Map<String,String> headerMap = new HashMap<String,String>();
		for(String header : headers) {
			String[] headerSplit = header.split("=");
			headerMap.put(headerSplit[0], headerSplit[1]);
		}
		return headerMap;
	}
    
    private boolean isTopicAvailable(final String topic) {
		URL url;
		try {
			url = new URL(brokerURL + "/" + topic);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			
			if (headers != null) {
				System.out.println("Setting Headers:");
				Map<String, String> headerMap = parseHeadersList();
				for (String key : headerMap.keySet()) {
					System.out.println("Key: " + key + " Val: " + headerMap.get(key));
					connection.setRequestProperty(key, headerMap.get(key));
				}
			}
			
			int respCode = connection.getResponseCode();
			
			if(respCode < 400) {
				return true;
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
    
    private void createTopic(final String topic) {
		try {
			//  /<entityID>/attrs/<attributeName>/value
			//# types: /Entity             /Atribute
			//# where topic = "/<entityID>/attrs/<attributeName>/value"
			//#
			// {"id": entityID, "type":"Topic", attributeName: {"value": "", "type": "Float"}}
			
			String jsonBodyString = "{\"id\": "+this.parseEntityId(topic)+", \"type\":\"Topic\", "+this.parseAttributeName(topic)+": {\"value\": \"\", \"type\": \"String\"}}";
						
			// create JSON Body			
			URL url = new URL(brokerURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			
			if (headers != null) {
				System.out.println("Setting headers");
				Map<String, String> headerMap = parseHeadersList();
				for (String key : headerMap.keySet()) {
					System.out.println("Key: " + key);
					connection.setRequestProperty(key, headerMap.get(key));
					System.out.println("Val: " + headerMap.get(key));
				}
			}
			
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			
			writer.write(jsonBodyString);
			writer.flush();
			
			int responseCode = connection.getResponseCode();
			System.out.println("deliveryComplete. Response Code: " + responseCode);
			writer.close();
	
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    // 
    
    
    private EsperWrapper() {

    	boolean test = this.isTopicAvailable("hack2/attrs/cmd1/value");
    	
    	System.out.println("Topic available: " + test);
    	
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
	    String query = epService.getEPAdministrator().getStatement(queryName).getText();

	    if (!query.toLowerCase().contains("create schema")) {

		String queryState = epService.getEPAdministrator().getStatement(queryName).getState().name();
		queries.put(new JSONObject().put("query_id", queryName).put("query", query).put("status", queryState));
	    }
	}
	return queries.toString();
    }

    public String getQueryNames() {
	String[] currentQueries = epService.getEPAdministrator().getStatementNames();
	List<String> queriesOnly = new ArrayList<String>();

	for (String queryName : currentQueries) {
	    String query = epService.getEPAdministrator().getStatement(queryName).getText();

	    if (!query.toLowerCase().contains("create schema")) {
		queriesOnly.add(queryName);
	    }
	}
	return String.join(";", queriesOnly);
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
		    OrionAdapter adapter = new OrionAdapter(dataSourceObj.getEndpoint(), dataSourceObj.getHeaders(), this);
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

	    } else if (PROTOCOL_HTTP_ORION.equals(dataSource.getProtocol().toUpperCase())) {
		OrionAdapter adapter = (OrionAdapter) adapters.get(datasource_id);
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
