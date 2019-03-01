package it.eng.rest;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.orion.cb.ngsi.bean.OperatorInfo;

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

		String TARGET_URL = actualObj.get("camPath").textValue();

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		OperatorInfo operator = SparqlService.getOperatorById(jsonAttributes.get("personid").get("value").asText(),
				TARGET_URL + "/SPARQLQuery");

		if (operator.getOperatorInstanceName() != null) {
			SparqlService.createJobOrderAnnotationOnCAM(operator.getOperatorInstanceName(),
					jsonAttributes.get("annotationdes").get("value").asText(), TARGET_URL + "/SPARQLUpdate");
		}

	}

	/**
	 * Execute query SPARQL to create an Annotation for an Operation
	 * 
	 * @return
	 */
	public void createAnnotationForOperation(HttpHeaders headers, JsonNode actualObj) throws Exception {

		String TARGET_URL = actualObj.get("camPath").textValue();

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		OperatorInfo operator = SparqlService.getOperatorById(jsonAttributes.get("personid").get("value").asText(),
				TARGET_URL + "/SPARQLQuery");

		if (operator.getProcessSegment() != null) {
			SparqlService.createOperationAnnotationOnCAM(operator.getProcessSegment(),
					jsonAttributes.get("annotationdes").get("value").asText(), TARGET_URL + "/SPARQLUpdate");
		}

	}

}
