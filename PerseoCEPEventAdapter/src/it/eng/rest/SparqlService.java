package it.eng.rest;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.orion.cb.ngsi.bean.OperatorInfo;
import it.eng.util.Util;

public class SparqlService {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(SparqlService.class);

	public static OperatorInfo getOperatorById(String operatorId, String camPath) throws IOException {
		log.info("Method get operator info init ...");

		log.info("TARGET_URL from JSON --> " + camPath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI for create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("login").replace("OPERATOR_ID", operatorId);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		String sparqlResponse = response.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseBody = mapper.readTree(sparqlResponse).get("results").get("bindings");

		OperatorInfo operator = new OperatorInfo();
		if (responseBody.size() == 0) {
			return operator;
		}
		// id
		operator.setOperatorId(operatorId);
		// name
		if (responseBody.get(0).has("personname")) {
			operator.setOperatorName(responseBody.get(0).get("personname").get("value").asText());
		}
		// instance name
		if (responseBody.get(0).has("person")) {
			String operatorInstanceName = responseBody.get(0).get("person").get("value").asText();
			operator.setOperatorInstanceName(operatorInstanceName.substring(operatorInstanceName.indexOf("#")+1));
		}
		// workorder
		if (responseBody.get(0).has("workorder")) {
			String workOrder = responseBody.get(0).get("workorder").get("value").asText();
			operator.setWorkOrder(workOrder.substring(workOrder.indexOf("#")+1));
		}
		// processsegment
		if (responseBody.get(0).has("processsegment")) {
			String processSegment = responseBody.get(0).get("processsegment").get("value").asText();
			operator.setProcessSegment(processSegment.substring(processSegment.indexOf("#")+1));
		}

		log.info("Method get operator info end ...");

		return operator;
	}

	public static Response createJobOrderAnnotationOnCAM(String operatorInstanceName, String annotatationText,
			String camPath) throws IOException {
		log.info("Method createJobOrderAnnotationOnCAM to create a FeedBack for a JobOrder init ...");

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI to create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("joborderfeedback")
				.replace("FEED_ID", "FEED_" + Util.getUUIDAsNumber())
				.replace("OPERATOR_INSTANCE_NAME", operatorInstanceName).replace("ANNOTATION_TEXT", annotatationText);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		log.info("Method createJobOrderAnnotationOnCAM to create a FeedBack for a JobOrder init end ...");
		return response;

	}

	public static Response createOperationAnnotationOnCAM(String processSegmentId, String annotatationText,
			String camPath) throws IOException {

		log.info("Method createOperationAnnotationOnCAM to create a FeedBack for an Operation init ...");
		log.info("TARGET_URL from JSON --> " + camPath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI to create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("operatorJobOrder")
				.replace("FEED_ID",	"FEED_" + Util.getUUIDAsNumber())
				.replace("ANNOTATION_TEXT", annotatationText)
				.replace("PROCESS_SEGMENT_ID", processSegmentId);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		log.info("Method createOperationAnnotationOnCAM to create a FeedBack for an Operation end ...");
		return response;

	}

}
