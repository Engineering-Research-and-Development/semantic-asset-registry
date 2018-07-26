package it.eng.cam.rest;

import it.eng.cam.pellet.reasoner.A4bluePelletReasonerFactory;
import it.eng.cam.rest.exception.CAMServiceWebException;
import it.eng.cam.rest.json.PelletResonerJSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Path("/")
public class A4blueReasoner {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(A4blueReasoner.class);

	@POST
	@Path("/SPARQLInferenceQuery")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response SPARQLInferenceQuery(PelletResonerJSON json) {
		// check if all form parameters are provided
		if (json.getDocumentURI() == null || json.getDocumentURI().isEmpty())
			return Response.status(400).entity("Invalid data input").build();
		if (json.getSparqlQuery() == null || json.getSparqlQuery().isEmpty())
			return Response.status(400).entity("Invalid data input").build();

		String jsonResults = null;
		try {
			jsonResults = A4bluePelletReasonerFactory.getInstance()
					.executeInferenceQuery(json.getDocumentURI(), json.getSparqlQuery());
		} catch (Exception e) {
			log.error("Unable to execute inference Query. Problem with Reasoner",e);
			throw new CAMServiceWebException(e.getMessage());
		}
		return Response.ok(jsonResults, MediaType.APPLICATION_JSON).build();
	}
}
