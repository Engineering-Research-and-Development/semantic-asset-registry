package it.eng.perseo.cep.event.adapter;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.eng.rest.CamSparqlRequest;
import it.eng.rest.OrionCreateEntity;
import it.eng.rest.exception.EventAdapterException;

@Path("/")
public class PerseoCEPEventAdapter {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(PerseoCEPEventAdapter.class);

	@POST
	@Path("/AddNotificationEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNotificationEvent(@Context HttpHeaders headers, String json) {
		// check if all form parameters are provided
		log.info("JSON: " + json);

		for (String header : headers.getRequestHeaders().keySet())
			log.info("HEADERS: " + header);

		try {
			OrionCreateEntity.getInstance().addNotificationEvent(headers, json);
		} catch (RuntimeException e) {
			log.error("Unable to create NGSI Entity[NotificationEvent]. Problem with Orion", e);
			throw new EventAdapterException(e.getMessage());
		} catch (Exception e) {
			log.error("Unable to create NGSI Entity[NotificationEvent]", e);
			throw new EventAdapterException(e.getMessage());
		}

		return Response.ok("Entity NotificationEvent successfull created", MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/AddFeedback")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addFeedback(@Context HttpHeaders headers, String json) throws JsonProcessingException, IOException {
		// check if all form parameters are provided
		log.info("JSON: " + json);

		for (String header : headers.getRequestHeaders().keySet())
			log.info("HEADERS: " + header);

		try {
			CamSparqlRequest.getInstance().addFeedback(headers, json);
		} catch (Exception e) {
			log.error("Unable to create an Annotation for the joborder", e);
			throw new EventAdapterException(e.getMessage());
		}

		return Response.ok("Annotation for the joborder successfull created", MediaType.APPLICATION_JSON).build();

	}

	@POST
	@Path("/DeleteFeedback")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFeedback(@Context HttpHeaders headers, String json)
			throws JsonProcessingException, IOException {
		// check if all form parameters are provided
		log.info("JSON: " + json);

		for (String header : headers.getRequestHeaders().keySet())
			log.info("HEADERS: " + header);

		try {
			CamSparqlRequest.getInstance().deleteFeedback(headers, json);
		} catch (Exception e) {
			log.error("Unable to delete an Annotation for the joborder", e);
			throw new EventAdapterException(e.getMessage());
		}

		return Response.ok("Annotation for the joborder successfull deleted", MediaType.APPLICATION_JSON).build();

	}

}
