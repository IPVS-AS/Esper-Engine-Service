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

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class HTTPAdapter {
	protected final int SLEEP_TIME_MS = 5000;
	protected IEngineCallback engineCallback = null;
	protected String brokerURL;
	protected String name;
	protected boolean state_running = true;
	protected Thread httpRequester = null;
	protected String[] headers = null;
	
	public HTTPAdapter(final String brokerURL, final String[] headers) {
		this.brokerURL = brokerURL;
		this.headers = headers;
		this.name = "ha" + System.currentTimeMillis();
	}

	public HTTPAdapter(final String brokerURL, final IEngineCallback engineCallback, final String[] headers) {
		this.brokerURL = brokerURL;
		this.engineCallback = engineCallback;
		this.name = "ha" + System.currentTimeMillis();
		this.headers = headers;
	}

	public HTTPAdapter(final String brokerURL, final IEngineCallback engineCallback) {
		this.brokerURL = brokerURL;
		this.engineCallback = engineCallback;
		this.name = "ha" + System.currentTimeMillis();
	}

	public HTTPAdapter(final String brokerURL) {
		this.brokerURL = brokerURL;
		this.name = "ha" + System.currentTimeMillis();
	}

	public String getName() {
		return this.name;
	}
	
	protected Map<String,String> parseHeadersList() {
		Map<String,String> headerMap = new HashMap<String,String>();
		for(String header : headers) {
			String[] headerSplit = header.split("=");
			headerMap.put(headerSplit[0], headerSplit[1]);
		}
		return headerMap;
	}

	public void bind(final String[] topics) {

		httpRequester = new Thread(new Runnable() {

			@Override
			public void run() {
				while (state_running) {
					try {
						URL url = new URL(brokerURL + "/" + topics[0]); // FIXME
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");
						connection.setRequestProperty("Accept", "application/json"); // FIXME

						// read the HTTP answer
						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						System.out.println(reader.readLine());
						// TODO send it to esper as event
						engineCallback.sendEvent(reader.readLine());
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

	public void publish(final String topic, String message) {
		try {
			URL url = new URL(brokerURL + "/" + topic);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

			writer.write(message);
			writer.flush();
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

	public void stop() {
		state_running = false;
		if (httpRequester != null) {
			httpRequester.interrupt();
		}
	}

}
