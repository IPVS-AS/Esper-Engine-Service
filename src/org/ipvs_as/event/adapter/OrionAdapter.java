package org.ipvs_as.event.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

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

    public void bind(final String[] topics) {

	super.httpRequester = new Thread(new Runnable() {

	    @Override
	    public void run() {
		while (state_running) {
		    try {
			URL url = new URL(brokerURL + "/" + topics[0]); // FIXME
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "text/plain");

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

    public void publish(final String topic, String message) {
	try {
	    URL url = new URL(brokerURL + "/" + topic);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("PUT");
	    connection.setRequestProperty("Content-Type", "text/plain");
	    connection.setDoInput(true);
	    connection.setDoOutput(true);
	    connection.setUseCaches(false);
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
