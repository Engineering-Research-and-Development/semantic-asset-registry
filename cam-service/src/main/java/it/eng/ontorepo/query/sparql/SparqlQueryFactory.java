package it.eng.ontorepo.query.sparql;

import it.eng.cam.finder.FinderFactory;

import java.util.MissingResourceException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SparqlQueryFactory {

	/**logger for this class **/
	private static final Logger logger = LogManager.getLogger(
			SparqlQueryFactory.class.getName());

	private static SparqlQueryFactory instance = null;
	
    private static String SESAME_REPO_URL = null;
    private static String SESAME_REPO_NAME = null;
    private static String CONTENT_TYPE_SPARQL_QUERY = "application/sparql-query";
    private static String ACCEPT_CONTENT = "application/json";
    
    static {
        try {
        	FinderFactory finder = FinderFactory.getInstance();
            SESAME_REPO_URL = finder.getString("sesame.url");
            SESAME_REPO_NAME = finder.getString("sesame.repository");
        } catch (MissingResourceException e) {
            logger.warn(e);
        }
    }

    /**
     * @constructor 
     */
    private SparqlQueryFactory (){
    }
    
    /**
     * get a static Clazz instance
     * @return
     */
    public static SparqlQueryFactory getInstance () {
    	if (instance == null) {
    		instance = new SparqlQueryFactory();
    	}
    	return instance;
    }
    
    /**
     * Execute a SPARQL Query on REPO (only static results)
     * @param sparqlQuery
     * @throws Exception
     * @return
     */
    public String sparqlQuery (String sparqlQuery) throws Exception {
    	logger.info("Method sparqlQuery INIT");
		if (logger.isDebugEnabled())
			logger.debug("sparqlQuery --> " + sparqlQuery);
		String JsonResult = null;
		try {
			
			Client client = ClientBuilder.newClient();
			
			WebTarget webTarget = client.target(SESAME_REPO_URL).path(
					"repositories").path(SESAME_REPO_NAME);
			
			Invocation.Builder invocationBuilder = webTarget.request(
					CONTENT_TYPE_SPARQL_QUERY);
			invocationBuilder.header("Accept", ACCEPT_CONTENT);
			
			Response response = invocationBuilder.post(Entity.entity(
					sparqlQuery, CONTENT_TYPE_SPARQL_QUERY));
			
			logger.info("HTTP Response STATUS: " + response.getStatus());
			
			if (null == response || response.getStatus() != Response.Status.OK.getStatusCode()) {
	            logger.error("Error in linking REDF4 Server: " + response.getStatus());
	        } else {
	        	JsonResult = response.readEntity(String.class);
	            if (logger.isDebugEnabled())
	            	logger.debug("Result SPARQL Query: " + JsonResult);
	        }
		} catch (Exception e) {
            logger.error("Unable to execute SPARQL Query");
            throw new Exception(e.getMessage());
        }
		return JsonResult;
    }
        
}
