package org.ipvs_as.event.adapter;

import java.util.Arrays;

/**
 * This class describes how to connect to a data entity, i.e., data sources or
 * data sinks.
 * 
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class DataConnector {
    private String protocol;
    private String endpoint;
    private String[] topics;
    private String[] headers;

	public DataConnector() {
    }

    public DataConnector(final String protocol, final String serverURL, final String[] topics) {
	this.protocol = protocol;
	this.endpoint = serverURL;
	this.topics = topics;
    }
    
    public DataConnector(final String protocol, final String serverURL, final String[] topics, final String[] headers) {
    	this.protocol = protocol;
    	this.endpoint = serverURL;
    	this.topics = topics;
    	this.headers = headers;
        }

    public String getProtocol() {
	return protocol;
    }

    public void setProtocol(String protocol) {
	this.protocol = protocol;
    }

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    public String[] getTopics() {
	return topics;
    }

    public void setTopics(String[] topics) {
	this.topics = topics;
    }
    
    public String[] getHeaders() {
		return headers;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

    @Override
    public String toString() {
	return "DataConnector [protocol=" + protocol + ", endpoint=" + endpoint + ", topics=" + Arrays.toString(topics)
		+ "]";
    }

}
