package it.eng.rest;

import java.math.BigInteger;
import java.util.UUID;

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
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.util.Util;

public class CamSparqlRequest {
	private static Log log = LogFactory.getLog(CamSparqlRequest.class);
	private static CamSparqlRequest instance = null;

	private CamSparqlRequest() {
	}

	public static CamSparqlRequest getInstance() {
		if (instance == null) {
			instance = new CamSparqlRequest();
		}
		return instance;
	}

	/**
	 * Execute query SPARQL to create an Annotation for a JobOrder
	 * 
	 * @return
	 */
	public void createAnnotationForJobOrder(HttpHeaders headers, JsonNode actualObj) throws Exception {
		log.info("Method executeSparqlQuery init ...");

		String TARGET_URL = actualObj.get("camPath").textValue() + "/SPARQLUpdate";

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		log.info("TARGET_URL from JSON --> " + TARGET_URL);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(TARGET_URL);

		log.info("URI for create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		Response response = invocationBuilder
				.post(Entity.entity(buildAnnotationForJobOrder(jsonAttributes.get("personid").get("value").asText(),
						jsonAttributes.get("annotationdes").get("value").asText()), "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		log.info("Method executeSparqlQuery end ...");

	}

	/**
	 * Execute query SPARQL to create an Annotation for an Operation
	 * 
	 * @return
	 */
	public void createAnnotationForOperation(HttpHeaders headers, JsonNode actualObj) throws Exception {
		log.info("Method executeSparqlQuery init ...");

		String TARGET_URL = actualObj.get("camPath").textValue() + "/SPARQLUpdate";

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		log.info("TARGET_URL from JSON --> " + TARGET_URL);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(TARGET_URL);

		log.info("URI for create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		Response response = invocationBuilder.post(
				Entity.entity(buildAnnotationForOperation(jsonAttributes.get("processsegmentid").get("value").asText(),
						jsonAttributes.get("annotationdes").get("value").asText()), "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		log.info("Method executeSparqlQuery end ...");

	}

	private static String buildAnnotationForJobOrder(String personid, String annotationdes) {
		String feedTestId = "FEED_" + String
				.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)).substring(0, 5);
		return "prefix var: <http://a4blue/ontologies/var.owl#>\r\n"
				+ "prefix owl:  <http://www.w3.org/2002/07/owl#>\r\n"
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + "INSERT DATA { var:" + feedTestId
				+ " rdf:type var:Annotation };\r\n" + "INSERT DATA { var:" + feedTestId
				+ " rdf:type owl:NamedIndividual };\r\n" + "INSERT DATA { var:" + feedTestId + " var:description \""
				+ annotationdes + "\" };\r\n" + "INSERT DATA { var:" + feedTestId + " var:status \"unread\" };\r\n"
				+ "INSERT DATA { var:" + feedTestId + " var:annotationId \"" + feedTestId + "\" };\r\n"
				+ "INSERT DATA { var:" + personid + " var:generatesAnnotation var:" + feedTestId + " };\r\n"
				+ "INSERT DATA { var:generatesAnnotation rdf:type owl:ObjectProperty };\r\n";
	}

	private static String buildAnnotationForOperation(String processsegmentid, String annotationdes) {
		String feedTestId = "FEED_" + String
				.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)).substring(0, 5);

		return "prefix var: <http://a4blue/ontologies/var.owl#>\r\n"
				+ "prefix owl:  <http://www.w3.org/2002/07/owl#>\r\n"
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + "INSERT DATA { var:" + feedTestId
				+ " rdf:type var:Annotation };\r\n" + "INSERT DATA { var:" + feedTestId
				+ " rdf:type owl:NamedIndividual }; \r\n" + "INSERT DATA { var:" + feedTestId + " var:description \""
				+ annotationdes + "\" };\r\n" + "INSERT DATA { var:" + feedTestId + " var:status \"unread\" };\r\n"
				+ "INSERT DATA { var:" + feedTestId + " var:annotationId \"" + feedTestId + "\" }; \r\n"
				+ "INSERT DATA { var:" + feedTestId + " var:relatedToProcessSegment var:" + processsegmentid + " };\r\n"
				+ "INSERT DATA { var:relatedToProcessSegment rdf:type owl:ObjectProperty }; \r\n";
	}

}
