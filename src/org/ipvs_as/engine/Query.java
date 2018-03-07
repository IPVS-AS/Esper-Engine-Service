package org.ipvs_as.engine;

import org.ipvs_as.event.adapter.DataConnector;

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
public class Query {
    private String query;
    private DataConnector subscriber;

    public Query() {
    }

    public String getQuery() {
	return query;
    }

    public void setQuery(String query) {
	this.query = query;
    }

    public DataConnector getSubscriber() {
	return subscriber;
    }

    public void setSubscriber(DataConnector subscriber) {
	this.subscriber = subscriber;
    }

}
