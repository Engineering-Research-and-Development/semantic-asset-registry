package it.eng.ontorepo.reasoner.pellet;

import it.eng.cam.finder.FinderFactory;

import java.net.URL;
import java.util.MissingResourceException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author nsvulcan
 *
 */
public class PelletReasonerFactory {

	/**logger for this class **/
	private static final Logger logger = LogManager.getLogger(
			PelletReasonerFactory.class.getName());

    private static String SESAME_REPO_URL = null;
    private static String SESAME_REPO_NAME = null;
    private static String SESAME_DOCUMENT_IRI = null;
    private static String CONTENT_TYPE = "application/json";
    private static String ACCEPT_CONTENT = "application/json";
    
    static {
        try {
        	FinderFactory finder = FinderFactory.getInstance();
            SESAME_REPO_URL = finder.getString("sesame.url");
            SESAME_REPO_NAME = finder.getString("sesame.repository");
            SESAME_DOCUMENT_IRI = SESAME_REPO_URL + "repositories/" + SESAME_REPO_NAME + "/statements";
        } catch (MissingResourceException e) {
            logger.warn(e);
        }
    }

	/**
	 * static reference
	 */
	private static PelletReasonerFactory reasoner = null;
	
	/**
	 * @constructor for this calss
	 */
	private PelletReasonerFactory (){
	}
	
	/**
	 * get static instance of this class
	 * @return
	 */
	public static PelletReasonerFactory getInstance() {
		if (reasoner == null) {
			reasoner = new PelletReasonerFactory();
		}
		return reasoner;
	}
	
	/**
	 * To execute inference SPARQL Query on OWL Ontology. The level of reasoning is OWL_DL_MEM
	 * @param sparqlQuery - Query SPARQL to search results on inference
	 * @return JSON Object
	 * @throws Exception
	 */
	public String executeInferenceQuery(String sparqlQuery) throws Exception {
    	logger.info("Method executeInferenceQuery INIT");
		if (logger.isDebugEnabled())
			logger.debug("executeInferenceQuery --> " + sparqlQuery);
		String JsonResult = null;
		try {
			Client client = ClientBuilder.newClient();
			
			WebTarget webTarget = client.target(getInferenceServiceURL())
					.path("SPARQLInferenceQuery");
			
			Invocation.Builder invocationBuilder = webTarget.request(
					CONTENT_TYPE);
			invocationBuilder.header("Accept", ACCEPT_CONTENT);
			
			PelletResonerJSON inputJSON = new PelletResonerJSON();
			inputJSON.setDocumentURI(SESAME_DOCUMENT_IRI);
			inputJSON.setSparqlQuery(sparqlQuery);
			
			Response response = invocationBuilder.post(Entity.entity(
					inputJSON, CONTENT_TYPE));
			
			logger.info("HTTP Response STATUS: " + response.getStatus());
			
			if (null == response || response.getStatus() != Response.Status.OK.getStatusCode()) {
	            logger.error("Error in inference query: " + response.getStatus());
	        } else {
	        	JsonResult = response.readEntity(String.class);
	            if (logger.isDebugEnabled())
	            	logger.debug("Result Inference Query: " + JsonResult);
	        }
		} catch (Exception e) {
            logger.error("Unable to execute Inference Query");
            throw new Exception(e.getMessage());
        }
		return JsonResult;
    }
	
	/**
	 * extract server and port from SESAME_REPO_URL
	 * @return
	 */
	private String getInferenceServiceURL() 
			throws Exception {
		URL aURL = new URL(SESAME_DOCUMENT_IRI);
		String url = aURL.getProtocol() + "://" + aURL.getHost() + ":"
				+ aURL.getPort() + "/A4bluePelletReasoner";
		return url;
	}
	
}
