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

import it.eng.orion.cb.ngsi.bean.Annotation;
import it.eng.orion.cb.ngsi.bean.OperatorInfo;
import it.eng.util.Util;

public class SparqlService {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(SparqlService.class);

	public static OperatorInfo getOperatorByInteractionDeviceId(String interactionDeviceid, String camPath)
			throws IOException {
		log.info("Method getOperatorByInteractionDeviceId init ...");

		log.info("TARGET_URL from JSON --> " + camPath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI for create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("getOperatorByInteractionDeviceId").replace("#INTERACTION_DEVICE_ID",
				interactionDeviceid);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
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

		// instance name
		if (responseBody.get(0).has("person")) {
			String operatorInstanceName = responseBody.get(0).get("person").get("value").asText();
			operator.setOperatorInstanceName(operatorInstanceName.substring(operatorInstanceName.indexOf("#") + 1));
		}

		// name
		if (responseBody.get(0).has("personname")) {
			operator.setOperatorName(responseBody.get(0).get("personname").get("value").asText());
		}

		// operator id
		if (responseBody.get(0).has("personid")) {
			operator.setOperatorId(responseBody.get(0).get("personid").get("value").asText());
		}

		// interaction device
		if (responseBody.get(0).has("interactiondevice")) {
			String interactionDevice = responseBody.get(0).get("interactiondevice").get("value").asText();
			operator.setInteractionDevice(interactionDevice.substring(interactionDevice.indexOf("#") + 1));
		}
		
		// joborder
		if (responseBody.get(0).has("joborder")) {
			String jobOrder = responseBody.get(0).get("joborder").get("value").asText();
			operator.setJobOrder(jobOrder.substring(jobOrder.indexOf("#") + 1));
		}
		
		

		log.info("Method getOperatorByInteractionDeviceId end ...");

		return operator;
	}
	
	public static OperatorInfo getOperatorAndAnnotationByAnnotationId(String annotationId, String camPath)
			throws IOException {
		log.info("Method getOperatorAndAnnotationByAnnotationId init ...");

		log.info("TARGET_URL from JSON --> " + camPath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI for create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("getOperatorAndAnnotationByAnnotationId").replace("#ANNOTATION_ID",
				annotationId);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
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

		// instance name
		if (responseBody.get(0).has("person")) {
			String operatorInstanceName = responseBody.get(0).get("person").get("value").asText();
			operator.setOperatorInstanceName(operatorInstanceName.substring(operatorInstanceName.indexOf("#") + 1));
		}

		// annotation
		if (responseBody.get(0).has("annotation")) {
			String annotation = responseBody.get(0).get("annotation").get("value").asText();
			operator.setAnnotation(annotation.substring(annotation.indexOf("#") + 1));
		}

		log.info("Method getOperatorAndAnnotationByAnnotationId end ...");

		return operator;
	}

	public static Annotation getAnnotationByJobOrderId(String jobOrderId, String camPath) throws IOException {
		log.info("Method getAnnotationByJobOrderId init ...");

		log.info("TARGET_URL from JSON --> " + camPath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI for create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("getAnnotationByJobOrderId").replace("#JOB_ORDER_ID",
				jobOrderId);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		String sparqlResponse = response.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseBody = mapper.readTree(sparqlResponse).get("results").get("bindings");

		Annotation annotation = new Annotation();
		if (responseBody.size() == 0) {
			return annotation;
		}

		// instance name
		if (responseBody.get(0).has("annotation")) {
			String annotationInstance = responseBody.get(0).get("annotation").get("value").asText();
			annotation.setAnnotationInstance(annotationInstance.substring(annotationInstance.indexOf("#") + 1));
		}

		// annotationid
		if (responseBody.get(0).has("annotationid")) {
			annotation.setAnnotationId(responseBody.get(0).get("annotationid").get("value").asText());
		}

		// annotationdes
		if (responseBody.get(0).has("annotationdes")) {
			annotation.setAnnotationDes(responseBody.get(0).get("annotationdes").get("value").asText());
		}

		log.info("Method getAnnotationByJobOrderId end ...");

		return annotation;
	}

	public static Response createJobOrderAnnotationOnCAM(String operatorInstanceName, String annotatationText,
			String jobOrder, String camPath) throws IOException {
		log.info("Method createJobOrderAnnotationOnCAM to create a FeedBack for a JobOrder init ...");

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI to create CAM sparql query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("joborderfeedback")
				.replace("FEED_ID", "FEED_" + Util.getUUIDAsNumber())
				.replace("OPERATOR_INSTANCE_NAME", operatorInstanceName).replace("ANNOTATION_TEXT", annotatationText).replace("JOBORDER_INSTANCE_NAME", jobOrder);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		log.info("Method createJobOrderAnnotationOnCAM to create a FeedBack for a JobOrder init end ...");
		return response;

	}

	public static Response deleteAnnotationOnCAM(String operatorIstanceName, String annotationIstanceName,
			String camPath) throws IOException {

		log.info("Method deleteAnnotationOnCAM to delete an Annotation(FeedBack) init ...");
		log.info("TARGET_URL from JSON --> " + camPath);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(camPath);

		log.info("URI for CAM sparql update query: " + webTarget.getUri());

		Invocation.Builder invocationBuilder = webTarget.request();

		String query = Util.getSparqlQuery().getProperty("deletefeedback")
				.replace("OPERATOR_INSTANCE_NAME", operatorIstanceName)
				.replace("ANNOTATION_INSTANCE_NAME", annotationIstanceName);

		Response response = invocationBuilder.post(Entity.entity(query, "text/plain"));

		log.info("HTTP Response STATUS: " + response.getStatus());

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			log.warn("Error SPARQL query: " + response.getStatus());
		} else {
			log.info(" SPARQL query executed: " + response.getStatus());
		}

		log.info("Method deleteAnnotationOnCAM to delete an Annotation(FeedBack) end ...");
		return response;

	}

}
