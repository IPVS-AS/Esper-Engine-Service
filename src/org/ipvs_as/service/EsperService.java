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
@Path("/")
public class EsperService {

    EsperWrapper engine = EsperWrapper.getInstance();

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String showStatus() {
	return engine.getStatus();
    }

    @Path("datasources")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addDataSource(String message) {
	String datasource_id = engine.addDataSource(message);
	if (!datasource_id.isEmpty()) {
	    return new JSONObject().put("datasource_id", datasource_id).put("status", "BOUND").toString();

	} else {
	    return new JSONObject().toString();
	}
    }

    @GET
    @Path("datasources/{datasource_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDataSource(@PathParam("datasource_id") String datasource_id) {
	return engine.getDataSourceState(datasource_id);
    }

    @DELETE
    @Path("datasources/{datasource_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeDataSource(@PathParam("datasource_id") String datasource_id) {
	return engine.removeDataSource(datasource_id);
    }

    @Path("datasources")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getDataSources() {
	return engine.getDataSources();
    }
}
