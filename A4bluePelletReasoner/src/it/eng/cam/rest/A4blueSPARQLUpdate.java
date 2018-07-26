package it.eng.cam.rest;

import it.eng.cam.pellet.sparql.A4blueJenaSPARQLUpdateFactory;
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
public class A4blueSPARQLUpdate {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(A4blueSPARQLUpdate.class);

	@POST
	@Path("/SPARQLInferenceUpdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response SPARQLInferenceQuery(PelletResonerJSON json) {
		// check if all form parameters are provided
		if (json.getDocumentURI() == null || json.getDocumentURI().isEmpty())
			return Response.status(400).entity("Invalid data input").build();
		if (json.getSparqlQuery() == null || json.getSparqlQuery().isEmpty())
			return Response.status(400).entity("Invalid data input").build();

		boolean result = false; 
		try {
			String rdfDocument = A4blueJenaSPARQLUpdateFactory.getInstance().executeInferenceUpdate(
					json.getDocumentURI(),json.getSparqlQuery());
			result = A4blueJenaSPARQLUpdateFactory.getInstance().putRDFData(
					json.getDocumentURI(), rdfDocument);
			if (!result)
				throw new CAMServiceWebException("Unable to execute SPARQL inference Update");
		} catch (Exception e) {
			log.error("Unable to execute SPARQL inference Update. Problem with repository",e);
			throw new CAMServiceWebException(e.getMessage());
		}
		return Response.ok("SPARQL Operation Performed", MediaType.APPLICATION_JSON).build();
	}

}
