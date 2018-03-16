package org.ipvs_as.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ipvs_as.engine.EsperWrapper;
import org.json.JSONObject;

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
@Path("/queries")
public class QueryService {

    EsperWrapper engine = EsperWrapper.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getQueries() {
	return engine.getQueries();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getQueryNames() {
	return engine.getQueryNames();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String startEsperQueryAndSubscribe(String message) {
	String id = engine.createQuery(message);
	if (id != null) {
	    return new JSONObject().put("query_id", id).put("status", "running").toString();
	} else {
	    return new JSONObject().put("status", "failed").toString();
	}
    }

    @GET
    @Path("{query_id}")
    public String getQueryState(@PathParam("query_id") String query_id) {
	return engine.getQueryState(query_id);
    }

    @DELETE
    @Path("{query_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteEsperQuery(@PathParam("query_id") String query_id) {
	String status = engine.deleteQuery(query_id);
	return new JSONObject().put("query_id", query_id).put("status", status).toString();
    }

    @POST
    @Path("{query_id}/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public String stopEsperQuery(@PathParam("query_id") String query_id) {
	String status = engine.stopQuery(query_id);
	return new JSONObject().put("query_id", query_id).put("status", status).toString();
    }

    @POST
    @Path("{query_id}/start")
    @Produces(MediaType.APPLICATION_JSON)
    public String startEsperQuery(@PathParam("query_id") String query_id) {
	String status = engine.startQuery(query_id);
	return new JSONObject().put("query_id", query_id).put("status", status).toString();
    }

    @GET
    @Path("{query_id}/subscriptions")
    public String getSubscriptions(@PathParam("query_id") String query_id) {
	return engine.getSubscriptions(query_id);
    }

    @POST
    @Path("{query_id}/subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String subscribeToQuery(@PathParam("query_id") String query_id, String dataSink) {
	String subscription_id = engine.subscribeToQuery(query_id, dataSink);
	return new JSONObject().put("query_id", query_id).put("subscription_id", subscription_id).toString();
    }

    @DELETE
    @Path("{query_id}/subscriptions/{subscription_id}")
    public String removeSubscriptions(@PathParam("query_id") String query_id,
	    @PathParam("subscription_id") String subscription_id) {
	return engine.removeSubscription(query_id, subscription_id);
    }
}
