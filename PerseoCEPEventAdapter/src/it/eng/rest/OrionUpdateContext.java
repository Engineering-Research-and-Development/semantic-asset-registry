package it.eng.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.orion.cb.ngsi.NGSIAdapter;

public class OrionUpdateContext {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(OrionUpdateContext.class);
	
	private static String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	private static String ACCEPT_CONTENT = "application/json";
	private static String FIWARE_SERVICE = "Fiware-Service";
	private static String FIWARE_SERVICE_PATH = "Fiware-ServicePath";
	private static String DEFAULT_FIWARE_SERVICE_VALUE = "a4blue";
	private static String DEFAULT_FIWARE_SERVICE_PATH_VALUE = "/a4blueevents";
	
	
	private static OrionUpdateContext instance = null;
	
	private OrionUpdateContext () {
	}
	
	public static OrionUpdateContext getInstance () {
		if (instance == null) {
			instance = new OrionUpdateContext();
		}
		return instance;
	}
	
	/**
	 * NGSI update context (v2 protocol) for NotificationEvent 
	 * @return
	 */
	public void updateContextforNEvent(HttpHeaders headers, String json)
			throws Exception {
		log.info("Method updateContextforNEvent init ...");
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);
		
		String TARGET_URL = actualObj.get("orionPath").textValue();

		log.info("TARGET_URL from JSON --> " + TARGET_URL);
		log.info("Header Fiware-ServicePath --> " + headers.getHeaderString(FIWARE_SERVICE_PATH));
		log.info("Header  Fiware-Service --> " + headers.getHeaderString(FIWARE_SERVICE));
		
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(TARGET_URL);
		
		log.info("URI for update context: " + webTarget.getUri());
		
		Invocation.Builder invocationBuilder = webTarget.request(
				CONTENT_TYPE_APPLICATION_JSON);
		invocationBuilder.header("Accept", ACCEPT_CONTENT);
		
		
		String fiwareServiceValue = headers.getHeaderString(
				FIWARE_SERVICE);
		if (fiwareServiceValue == null)
			fiwareServiceValue = DEFAULT_FIWARE_SERVICE_VALUE;
		
		String fiwareServicePathValue = headers.getHeaderString(
				FIWARE_SERVICE_PATH);
		if (fiwareServicePathValue == null)
			fiwareServicePathValue = DEFAULT_FIWARE_SERVICE_PATH_VALUE;
		
		invocationBuilder.header(FIWARE_SERVICE, fiwareServiceValue);
		invocationBuilder.header(FIWARE_SERVICE_PATH, fiwareServicePathValue);
		
		String otionNGSINEventJSON = NGSIAdapter.getInstance().adaptNEventNGSIFormat(json);
		
		Response response = invocationBuilder.post(Entity.entity(
				otionNGSINEventJSON, CONTENT_TYPE_APPLICATION_JSON));
		
		log.info("HTTP Response STATUS: " + response.getStatus());
		if (null == response || response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
			log.warn("Error in Orion Udpate Context: " + response.getStatus());
			
        } else {
			log.info("Update Context OK: " + response.getStatus());
        }
		
		log.info("Method updateContextforNEvent end ...");
		
	}
	
}
