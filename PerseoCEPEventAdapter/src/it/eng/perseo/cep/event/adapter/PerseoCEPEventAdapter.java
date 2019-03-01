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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.rest.CamSparqlRequest;
import it.eng.rest.OrionCreateEntity;
import it.eng.rest.OrionUpdateContext;
import it.eng.rest.exception.EventAdapterException;

@Path("/")
public class PerseoCEPEventAdapter {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(PerseoCEPEventAdapter.class);

	@POST
	@Path("/NotificationEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response notificationEvent(@Context HttpHeaders headers, String json) {
		// check if all form parameters are provided
		log.info("JSON: " + json);

		for (String header : headers.getRequestHeaders().keySet())
			log.info("HEADERS: " + header);

		try {
			OrionUpdateContext.getInstance().updateContextforNEvent(headers, json);
		} catch (RuntimeException e) {
			log.error("Unable to update context. Problem with Orion", e);
			throw new EventAdapterException(e.getMessage());
		} catch (Exception e) {
			log.error("Unable to update context. Problem with Orion", e);
			throw new EventAdapterException(e.getMessage());
		}

		return Response.ok("Orion UpdateContext OK", MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/Login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpHeaders headers, String json) {
		// check if all form parameters are provided
		log.info("JSON: " + json);

		for (String header : headers.getRequestHeaders().keySet())
			log.info("HEADERS: " + header);

		try {
			OrionCreateEntity.getInstance().login(headers, json);
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
	@Path("/FeedBack")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response feedBack(@Context HttpHeaders headers, String json) throws JsonProcessingException, IOException {
		// check if all form parameters are provided
		log.info("JSON: " + json);

		for (String header : headers.getRequestHeaders().keySet())
			log.info("HEADERS: " + header);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		if (jsonAttributes.get("eventtype").get("value").textValue().equalsIgnoreCase("JOBORDER")) {
			try {
				CamSparqlRequest.getInstance().createAnnotationForJobOrder(headers, actualObj);
			} catch (RuntimeException e) {
				log.error("Unable to execute SPARQL query to create an Annotation for a joborder", e);
				throw new EventAdapterException(e.getMessage());
			} catch (Exception e) {
				log.error("Unable to execute SPARQL query to create an Annotation for a joborder", e);
				throw new EventAdapterException(e.getMessage());
			}

			return Response.ok("Entity Annotation successfull created", MediaType.APPLICATION_JSON).build();

		}

		if (jsonAttributes.get("eventtype").get("value").textValue().equalsIgnoreCase("OPERATION")) {
			try {
				CamSparqlRequest.getInstance().createAnnotationForOperation(headers, actualObj);
			} catch (RuntimeException e) {
				log.error("Unable to execute SPARQL query to create an Annotation for an operation", e);
				throw new EventAdapterException(e.getMessage());
			} catch (Exception e) {
				log.error("Unable to execute SPARQL query to create an Annotation for an operation", e);
				throw new EventAdapterException(e.getMessage());
			}

			return Response.ok("Entity Annotation successfull created", MediaType.APPLICATION_JSON).build();

		}

		return null;
	}

}
