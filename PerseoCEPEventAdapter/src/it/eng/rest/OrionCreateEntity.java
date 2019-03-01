package it.eng.rest;

import it.eng.orion.cb.ngsi.NGSIAdapter;
import it.eng.util.Util;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OrionCreateEntity {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(OrionCreateEntity.class);

	private static String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	private static String ACCEPT_CONTENT = "application/json";
	private static String FIWARE_SERVICE = "Fiware-Service";
	private static String FIWARE_SERVICE_PATH = "Fiware-ServicePath";
	private static String DEFAULT_FIWARE_SERVICE_VALUE = "a4blue";
	private static String DEFAULT_FIWARE_SERVICE_PATH_VALUE = "/a4blueevents";

	private static OrionCreateEntity instance = null;

	private OrionCreateEntity() {
	}

	public static OrionCreateEntity getInstance() {
		if (instance == null) {
			instance = new OrionCreateEntity();
		}
		return instance;
	}

	/**
	 * Execute query SPARQL for Operator login
	 * 
	 * @return
	 */
	public void login(HttpHeaders headers, String json) throws Exception {
		log.info("Method login init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		String TARGET_URL = actualObj.get("camPath").textValue() + "/SPARQLQuery";

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		log.info("TARGET_URL from JSON --> " + TARGET_URL);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(TARGET_URL);

		log.info("URI for create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("login").replace("OPERATOR_ID",
				jsonAttributes.get("personid").get("value").asText());

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		JsonNode responseBody = mapper.readTree(response.readEntity(String.class)).get("results").get("bindings");
		String operatorName = responseBody.get(0).get("personName").get("value").asText();

		createNotification(headers, json, operatorName, actualObj.get("type").textValue());

		log.info("Method loginy end ...");

	}

	/**
	 * Create an NGSI NotificationEvent
	 * 
	 * @return
	 */
	public void createNotification(HttpHeaders headers, String json, String operatorName, String notificationType)
			throws Exception {
		log.info("Method createNotificationEvent init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		String TARGET_URL = actualObj.get("orionPath").textValue();

		log.info("TARGET_URL from JSON --> " + TARGET_URL);
		log.info("Header Fiware-ServicePath --> " + headers.getHeaderString(FIWARE_SERVICE_PATH));
		log.info("Header  Fiware-Service --> " + headers.getHeaderString(FIWARE_SERVICE));

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(TARGET_URL);

		log.info("URI for create new NotificationEvent: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request(CONTENT_TYPE_APPLICATION_JSON);
		invocationBuilder.header("Accept", ACCEPT_CONTENT);

		String fiwareServiceValue = headers.getHeaderString(FIWARE_SERVICE);
		if (fiwareServiceValue == null)
			fiwareServiceValue = DEFAULT_FIWARE_SERVICE_VALUE;

		String fiwareServicePathValue = headers.getHeaderString(FIWARE_SERVICE_PATH);
		if (fiwareServicePathValue == null)
			fiwareServicePathValue = DEFAULT_FIWARE_SERVICE_PATH_VALUE;

		invocationBuilder.header(FIWARE_SERVICE, fiwareServiceValue);
		invocationBuilder.header(FIWARE_SERVICE_PATH, fiwareServicePathValue);

		
		String orionNGSINEventJSON = "";
		if (notificationType.equalsIgnoreCase("NotificatioNEvent")) {
			orionNGSINEventJSON = NGSIAdapter.getInstance().createNotificationEvent(json);
		}

		if (notificationType.equalsIgnoreCase("HMDNotificatioNEvent")) {
			orionNGSINEventJSON = NGSIAdapter.getInstance().createHMDNotificationEvent(json, operatorName);
		}

		Response response = invocationBuilder.post(Entity.entity(orionNGSINEventJSON, CONTENT_TYPE_APPLICATION_JSON));

		log.info("HTTP Response STATUS: " + response.getStatus());
		if (null == response || response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error create Entity: " + response.getStatus());

		} else {
			log.info("Create NGSI NotificationEvent OK: " + response.getStatus());
		}

		log.info("Method createNotificationEvent end ...");

	}
}
