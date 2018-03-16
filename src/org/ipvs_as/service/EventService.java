package org.ipvs_as.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ipvs_as.engine.EsperWrapper;
import org.json.JSONObject;

/**
 * @author Ana Cristina Franco da Silva, University of Stuttgart
 *
 */
@Path("/event")
public class EventService {

    EsperWrapper engine = EsperWrapper.getInstance();

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEventTypes() {
	return engine.getEventTypes();
    }

    @POST
    @Path("/types")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createEventType(String message) {
	String id = engine.createEventType(message);
	if (id != null) {
	    return new JSONObject().put("eventtype_id", id).put("status", "running").toString();
	} else {
	    return new JSONObject().put("status", "failed").toString();
	}
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void sendEvent(String message) {
	engine.sendEvent(message);
    }
}
