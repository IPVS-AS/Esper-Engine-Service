package org.ipvs_as.event.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.ipvs_as.engine.IEngineCallback;

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class HTTPAdapter {
    private final int SLEEP_TIME_MS = 5000;
    private IEngineCallback engineCallback = null;
    private String brokerURL;
    private String name;
    private boolean state_running = true;
    private Thread httpRequester = null;

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

    public void bind(final String[] topics) {

	httpRequester = new Thread(new Runnable() {

	    @Override
	    public void run() {
		while (state_running) {
		    try {
			URL url = new URL(brokerURL + "/" + topics[0]);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "text/plain"); // FIXME

			// read the HTTP answer
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			System.out.println(reader.readLine());
			// TODO
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
	// FIXME
    }

    public void stop() {
	state_running = false;
	if (httpRequester != null) {
	    httpRequester.interrupt();
	}
    }

}
