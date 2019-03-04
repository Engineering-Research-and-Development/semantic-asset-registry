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
	 * Execute query SPARQL to create an Annotation for a JobOrder
	 * 
	 * @return
	 */
	public void createAnnotationForJobOrder(HttpHeaders headers, String json) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		String TARGET_URL = actualObj.get("camPath").textValue();	
		

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");
		
		OperatorInfo operator = SparqlService.getOperatorById(jsonAttributes.get("personid").get("value").asText(),
				TARGET_URL + "/SPARQLQuery");

		if (operator.getOperatorInstanceName() != null) {
			Response response = SparqlService.createJobOrderAnnotationOnCAM(operator.getOperatorInstanceName(),
					jsonAttributes.get("annotationdes").get("value").asText(), TARGET_URL + "/SPARQLUpdate");
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				String ORION_PATH = actualObj.get("orionPath").textValue();
				HMDNotificationEvent hmdNotificationEvent = new HMDNotificationEvent();
				hmdNotificationEvent.setTimestamp(Util.getTimeStamp());
				NotificationDes notDes = new NotificationDes();
				notDes.setType("String");
				notDes.setValue("workorder: " + jsonAttributes.get("annotationdes").get("value").asText());
				hmdNotificationEvent.setNotificationdes(notDes);
				OrionCreateEntity.getInstance().createEntityOnOrion(ORION_PATH, headers,
						hmdNotificationEvent);

			}
		}

	}

	/**
	 * Execute query SPARQL to create an Annotation for an Operation
	 * 
	 * @return
	 */
	public void createAnnotationForOperation(HttpHeaders headers, String json) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

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
