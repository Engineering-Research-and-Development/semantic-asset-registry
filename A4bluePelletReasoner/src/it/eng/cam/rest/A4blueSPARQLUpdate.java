package it.eng.cam.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ManagedAsync;

import it.eng.cam.pellet.sparql.A4blueJenaSPARQLUpdateFactory;
import it.eng.cam.rest.exception.CAMServiceWebException;
import it.eng.cam.rest.json.PelletResonerJSON;

@Path("/")
public class A4blueSPARQLUpdate {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(A4blueSPARQLUpdate.class);
	
	@POST
	@Path("/SPARQLInferenceUpdate")
	@ManagedAsync
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void SPARQLInferenceQuery(PelletResonerJSON json, @Suspended final AsyncResponse asyncResponse) {
		// check if all form parameters are provided
		if (json.getDocumentURI() == null || json.getDocumentURI().isEmpty())
			asyncResponse.resume(Response.status(400).entity("Invalid data input").build());
		if (json.getSparqlQuery() == null || json.getSparqlQuery().isEmpty())
			asyncResponse.resume(Response.status(400).entity("Invalid data input").build());

		synchronized (A4blueSPARQLUpdate.class) {
			log.info("synchronized block");
	 		A4blueJenaSPARQLUpdateFactory instance = 
					new A4blueJenaSPARQLUpdateFactory();
			
			try {
				String rdfDocument = instance.executeInferenceUpdate(
						json.getDocumentURI(), json.getSparqlQuery());
				if (rdfDocument != null) {
					if (!instance.putRDFData(json.getDocumentURI(), rdfDocument)) {
						log.error("Unable to execute SPARQL inference Update. Problem with repository");
						throw new CAMServiceWebException("Unable to execute SPARQL inference Update. Problem with repository");
					}
				}
			} catch (Exception e) {
				log.error("Unable to execute SPARQL inference Update. Problem with repository", e);
				throw new CAMServiceWebException(e.getMessage());
			}
		}
		
		asyncResponse.resume(Response.ok("SPARQL Operation Performed", MediaType.APPLICATION_JSON).build());		
		
	}

}
