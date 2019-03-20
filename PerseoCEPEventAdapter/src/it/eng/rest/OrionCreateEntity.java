package it.eng.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

import it.eng.orion.cb.ngsi.bean.Annotation;
import it.eng.orion.cb.ngsi.bean.AnnotationDes;
import it.eng.orion.cb.ngsi.bean.AnnotationId;
import it.eng.orion.cb.ngsi.bean.EquipmentID;
import it.eng.orion.cb.ngsi.bean.EventType;
import it.eng.orion.cb.ngsi.bean.NotificationEvent;
import it.eng.orion.cb.ngsi.bean.OrionEntityBundle;
import it.eng.util.Util;

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
	 * Add NotificationEvent on ORION Context Broker
	 * 
	 * @return
	 */
	public void addNotificationEvent(HttpHeaders headers, String json) throws Exception {
		log.info("Method addNotificationEvent init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		String TARGET_URL = actualObj.get("camPath").textValue() + "/SPARQLQuery";

		//String instanceType = actualObj.get("type").textValue();

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		String orionPath = actualObj.get("orionPath").textValue();

		// retrieve Annotation by the joborderid
		Annotation annotation = SparqlService
				.getAnnotationByJobOrderId(jsonAttributes.get("joborderid").get("value").asText(), TARGET_URL);

		// add NotificationEvent on Orion
		if (annotation != null && annotation.getAnnotationId() != null) {
			NotificationEvent notificatinEvent = new NotificationEvent();

			// set eventtype
			EventType eventType = new EventType();
			eventType.setType("String");
			eventType.setValue("BESTPRACTISE");
			notificatinEvent.setEventType(eventType);

			// set equipmentid
			if (jsonAttributes.has("equipmentid")) {
				EquipmentID equipmentId = new EquipmentID();
				equipmentId.setType("String");
				equipmentId.setValue(jsonAttributes.get("equipmentid").get("value").asText());
				notificatinEvent.setEquipmentID(equipmentId);
			}

			AnnotationId annotationId = new AnnotationId();
			annotationId.setType("String");
			annotationId.setValue(annotation.getAnnotationId());
			notificatinEvent.setAnnotationId(annotationId);

			AnnotationDes annotationDes = new AnnotationDes();
			annotationDes.setType("String");
			annotationDes.setValue(annotation.getAnnotationDes());
			notificatinEvent.setAnnotationDes(annotationDes);

			createEntityOnOrion(orionPath, headers, notificatinEvent);
		}

		log.info("Method addNotificationEvent end ...");

	}

	/**
	 * Create or update an entity on ORION
	 * 
	 * @return
	 */
	public <T> Response createEntityOnOrion(String orionPath, HttpHeaders headers, T orionEntity) throws IOException {

		log.info("Method createEntityOnOrion init ...");

		log.info("Header Fiware-ServicePath --> " + headers.getHeaderString(FIWARE_SERVICE_PATH));
		log.info("Header  Fiware-Service --> " + headers.getHeaderString(FIWARE_SERVICE));

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(orionPath);

		log.info("URI to create entity on Orion: " + webTarget.getUri());

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

		OrionEntityBundle<T> orionEntityBundle = new OrionEntityBundle<T>();

		//java reflection
		try {
			Method methodsetId = orionEntity.getClass().getMethod("setId", String.class);
			methodsetId.invoke(orionEntity, orionEntity.getClass().getSimpleName() + "-" + Util.getUUID());
			Method methodsetType = orionEntity.getClass().getMethod("setType", String.class);
			methodsetType.invoke(orionEntity, orionEntity.getClass().getSimpleName());
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}

		orionEntityBundle.setActionType("APPEND");
		orionEntityBundle.addEntity(orionEntity);

		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(System.out, orionEntityBundle);

		String bodyJson = mapper.writeValueAsString(orionEntityBundle).toString();

		log.info("JSON NGSI format for " + "create Entity on Orion --> " + bodyJson);

		Response response = invocationBuilder.post(Entity.entity(bodyJson, CONTENT_TYPE_APPLICATION_JSON));

		log.info("HTTP Response STATUS: " + response.getStatus());
		if (null == response || response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			log.warn("Error create Entity: " + response.getStatus());
		} else {
			log.info("Create NGSI NotificationEvent OK: " + response.getStatus());
		}

		log.info("Method createEntityOnOrion end ...");

		return response;

	}
}
