package org.ipvs_as.event.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ipvs_as.engine.IEngineCallback;
import org.json.JSONObject;

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class OrionAdapter extends HTTPAdapter {

	public OrionAdapter(String brokerURL) {
		super(brokerURL);
	}

	public OrionAdapter(String endpoint, IEngineCallback esperWrapper) {
		super(endpoint, esperWrapper);
	}

	public OrionAdapter(String endpoint, String[] headers, IEngineCallback esperWrapper) {
		super(endpoint, esperWrapper, headers);
	}

	public OrionAdapter(String endpoint, String[] headers) {
		super(endpoint, headers);
	}

	public void bind(final String[] topics) {
		
		this.isTopicAvailable(topics[0]);
		this.createTopic(topics[0]);

		super.httpRequester = new Thread(new Runnable() {

			@Override
			public void run() {
				while (state_running) {
					try {
						
						
						
						URL url = new URL(brokerURL + "/" + topics[0]); // FIXME
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");
						connection.setRequestProperty("Accept", "text/plain");

						if (headers != null) {
							Map<String, String> headerMap = parseHeadersList();
							for (String key : headerMap.keySet()) {
								connection.setRequestProperty(key, headerMap.get(key));
							}
						}

						// read the HTTP answer
						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

						String value = reader.readLine();
						double valueDouble = Double.parseDouble(value);
						// send it to esper as event
						JSONObject jsonObj = new JSONObject();

						JSONObject valueObject = new JSONObject();
						valueObject.put("value", valueDouble);

						jsonObj.put("TopicIN", valueObject);

						System.out.println(jsonObj.toString());
						engineCallback.sendEvent(jsonObj.toString());

						reader.close();

					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(SLEEP_TIME_MS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});
		httpRequester.start();
	}
	
	private String parseEntityId(final String topic) {
		return topic.substring(0,topic.indexOf("/attrs/"));		
	}
	
	private String parseAttributeName(final String topic) {
		return topic.substring(topic.indexOf("/attrs/") +  "/attrs/".length(), topic.indexOf("/value"));
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
				Map<String, String> headerMap = parseHeadersList();
				for (String key : headerMap.keySet()) {
					connection.setRequestProperty(key, headerMap.get(key));
				}
			}
			
			connection.getResponseCode();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public void publish(final String topic, String message) {
		try {
			
			if(!this.isTopicAvailable(topic)) {
				System.out.println("Topic " + topic + " not available, creating");
				this.createTopic(topic);
			}
			
			URL url = new URL(brokerURL + "/" + topic);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			
			if (headers != null) {
				Map<String, String> headerMap = parseHeadersList();
				for (String key : headerMap.keySet()) {
					connection.setRequestProperty(key, headerMap.get(key));
				}
			}
			
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

			JSONObject obj = new JSONObject(message);
			String cmd = (String) ((JSONObject) obj.get("event")).get("cmd");

			writer.write("\"" + cmd + "\"");
			writer.flush();

			// read the HTTP answer
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
}
