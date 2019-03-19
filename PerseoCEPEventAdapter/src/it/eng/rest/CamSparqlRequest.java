package it.eng.rest;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.orion.cb.ngsi.bean.HMDNotificationEvent;
import it.eng.orion.cb.ngsi.bean.NotificationDes;
import it.eng.orion.cb.ngsi.bean.OperatorInfo;
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
	 * Add feedback (Annotation) on CAM
	 * 
	 * @return
	 */
	public void addFeedback(HttpHeaders headers, String json) throws Exception {
		log.info("Method addFeedback init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		String TARGET_URL = actualObj.get("camPath").textValue() + "/SPARQLQuery";
		String TARGET_URL_QueryUPDATE = actualObj.get("camPath").textValue() + "/SPARQLUpdate";

		// String instanceType = actualObj.get("type").textValue();

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		// retrieve information about operator
		OperatorInfo operator = SparqlService
				.getOperatorByInteractionDeviceId(jsonAttributes.get("interactiondeviceid").get("value").asText(), TARGET_URL);

		if (operator != null && operator.getOperatorInstanceName() != null && operator.getJobOrder()!=null) {
			String notificationdes = "";
			if (jsonAttributes.has("notificationdes")) {
				notificationdes = jsonAttributes.get("notificationdes").get("value").asText();
			}
			SparqlService.createJobOrderAnnotationOnCAM(operator.getOperatorInstanceName(), notificationdes, operator.getJobOrder(),
					TARGET_URL_QueryUPDATE);

		}

		log.info("Method addFeedback end ...");

	}

	/**
	 * Delete feedback (Annotation) from the CAM
	 * 
	 * @return
	 */
	public void deleteFeedback(HttpHeaders headers, String json) throws Exception {
		log.info("Method deleteFeedback init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		String TARGET_URL = actualObj.get("camPath").textValue() + "/SPARQLQuery";
		String TARGET_URL_QueryUPDATE = actualObj.get("camPath").textValue() + "/SPARQLUpdate";

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		// retrieve information about a specific operator
		OperatorInfo operator = SparqlService.getOperatorAndAnnotationByAnnotationId(
				jsonAttributes.get("notificationid").get("value").asText(), TARGET_URL);

		if (operator != null && operator.getOperatorInstanceName() != null && operator.getAnnotation() != null) {

			SparqlService.deleteAnnotationOnCAM(operator.getOperatorInstanceName(), operator.getAnnotation(),
					TARGET_URL_QueryUPDATE);

		}

		log.info("Method deleteFeedback end ...");

	}

}
