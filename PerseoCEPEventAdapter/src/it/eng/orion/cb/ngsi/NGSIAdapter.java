package it.eng.orion.cb.ngsi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.orion.cb.ngsi.bean.EquipmentDes;
import it.eng.orion.cb.ngsi.bean.EquipmentID;
import it.eng.orion.cb.ngsi.bean.EventType;
import it.eng.orion.cb.ngsi.bean.HMDNotificationEvent;
import it.eng.orion.cb.ngsi.bean.InteractionDeviceId;
import it.eng.orion.cb.ngsi.bean.Location;
import it.eng.orion.cb.ngsi.bean.NotificationDes;
import it.eng.orion.cb.ngsi.bean.NotificationEvent;
import it.eng.orion.cb.ngsi.bean.Payload;
import it.eng.orion.cb.ngsi.bean.PersonID;
import it.eng.orion.cb.ngsi.bean.Timeout;
import it.eng.orion.cb.ngsi.bean.Timestamp;
import it.eng.orion.cb.ngsi.bean.UdapteContext;
import it.eng.util.Util;

public class NGSIAdapter {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(NGSIAdapter.class);

	private static NGSIAdapter instance = null;

	private NGSIAdapter() {
	}

	public static NGSIAdapter getInstance() {
		if (instance == null) {
			instance = new NGSIAdapter();
		}
		return instance;
	}

	/**
	 * Adapt Perseo Post JSON format to NGSY Update Context
	 * 
	 * @return JSON NGSI format for NotificationEvent
	 */
	public String adaptNEventNGSIFormat(String json) throws Exception {
		log.info("Method adaptNEventNGSIFormat init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		ObjectMapper mapperContextUpdate = new ObjectMapper();

		UdapteContext updateContext = new UdapteContext();
		updateContext.setActionType(actualObj.get("actionType").textValue());

		ArrayList<Object> entities = new ArrayList<Object>();

		NotificationEvent notificationEvent = new NotificationEvent();
		notificationEvent.setId(actualObj.get("id").textValue());
		notificationEvent.setType(actualObj.get("type").textValue());

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		ObjectNode objPersonID = (ObjectNode) jsonAttributes.get("PersonID");
		PersonID personID = new PersonID();
		personID.setType(objPersonID.get("type").textValue());
		personID.setValue(objPersonID.get("value").textValue());
		notificationEvent.setPersonID(personID);

		ObjectNode objPayload = (ObjectNode) jsonAttributes.get("Payload");
		Payload payload = new Payload();
		payload.setType(objPayload.get("type").textValue());
		payload.setValue(objPayload.get("value").textValue());
		notificationEvent.setPayload(payload);

		Timestamp timestamp = new Timestamp();
		timestamp.setType("String");

		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);
		timestamp.setValue(strDate);
		notificationEvent.setTimestamp(timestamp);

		entities.add(notificationEvent);
		updateContext.setEntities(entities);
		mapperContextUpdate.writeValue(System.out, updateContext);

		String jsonContextUpdate = mapper.writeValueAsString(updateContext);

		String jsonResult = jsonContextUpdate.toString();

		log.info("JSON NotitifcationEvent NGSI format for " + "update-context on Orion --> " + jsonResult);

		return jsonResult;

	}

	/**
	 * Create a new NGSI flow NotificationEvent
	 * 
	 * @return JSON NGSI format for NotificationEvent
	 */
	public NotificationEvent createNotificationEvent(String json) throws Exception {
		log.info("Method createNotificationEvent init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		ObjectMapper mapperCreateEntity = new ObjectMapper();
		NotificationEvent notificationEvent = new NotificationEvent();

		notificationEvent.setId(actualObj.get("type").textValue() + "-" + Util.getUUID());
		notificationEvent.setType(actualObj.get("type").textValue());

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		// EventType
		ObjectNode objEventType = (ObjectNode) jsonAttributes.get("eventtype");
		EventType eventType = new EventType();
		eventType.setType(objEventType.get("type").textValue());
		eventType.setValue(objEventType.get("value").textValue());
		notificationEvent.setEventType(eventType);

		// PersonID
		ObjectNode objPersonID = (ObjectNode) jsonAttributes.get("personid");
		PersonID personID = new PersonID();
		personID.setType(objPersonID.get("type").textValue());
		personID.setValue(objPersonID.get("value").textValue());
		notificationEvent.setPersonID(personID);

		// EquipmentID
		ObjectNode objEquipmentID = (ObjectNode) jsonAttributes.get("equipmentid");
		EquipmentID equipmentID = new EquipmentID();
		equipmentID.setType(objEquipmentID.get("type").textValue());
		equipmentID.setValue(objEquipmentID.get("value").textValue());
		notificationEvent.setEquipmentID(equipmentID);

		// EquipmentDes
		ObjectNode objEquipmentDes = (ObjectNode) jsonAttributes.get("equipmentdes");
		EquipmentDes equipmentDes = new EquipmentDes();
		equipmentDes.setType(objEquipmentDes.get("type").textValue());
		equipmentDes.setValue(objEquipmentDes.get("value").textValue());
		notificationEvent.setEquipmentDes(equipmentDes);

		// Verbosity
		/*
		 * ObjectNode objVerbosity = (ObjectNode)jsonAttributes.get("verbosity");
		 * Verbosity verbosity = new Verbosity();
		 * verbosity.setType(objVerbosity.get("type").textValue());
		 * verbosity.setValue(Integer.parseInt(objVerbosity.get("value").textValue()));
		 * notificationEvent.setVerbosity(verbosity);
		 */

		// Payload
		/*
		 * ObjectNode objPayload = (ObjectNode)jsonAttributes.get("Payload"); Payload
		 * payload = new Payload(); payload.setType(objPayload.get("type").textValue());
		 * payload.setValue(objPayload.get("value").textValue());
		 * notificationEvent.setPayload(payload);
		 */

		// Location
		ObjectNode objLocation = (ObjectNode) jsonAttributes.get("location");
		Location location = new Location();
		location.setType(objLocation.get("type").textValue());
		location.setValue(objLocation.get("value").textValue());
		notificationEvent.setLocation(location);

		notificationEvent.setTimestamp(Util.getTimeStamp());

		log.info("Method createNotificationEvent init ...");

		return notificationEvent;
	}

	/**
	 * Create a new NGSI flow NotificationEvent
	 * 
	 * @return JSON NGSI format for NotificationEvent
	 */
	public HMDNotificationEvent createHMDNotificationEvent(String json, String operatorName, String message)
			throws Exception {
		log.info("Method createNewNGSIHMDNotificationEvent init ...");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);

		ObjectMapper mapperCreateEntity = new ObjectMapper();
		HMDNotificationEvent hmdnotificationEvent = new HMDNotificationEvent();

		hmdnotificationEvent.setId(actualObj.get("type").textValue() + "-" + Util.getUUID());
		hmdnotificationEvent.setType(actualObj.get("type").textValue());

		ObjectNode jsonAttributes = (ObjectNode) actualObj.get("attributes");

		// InteractionDeviceId
		ObjectNode objInteractionDeviceId = (ObjectNode) jsonAttributes.get("interactiondeviceid");
		InteractionDeviceId interactionDeviceId = new InteractionDeviceId();
		interactionDeviceId.setType(objInteractionDeviceId.get("type").textValue());
		interactionDeviceId.setValue(objInteractionDeviceId.get("value").textValue());
		hmdnotificationEvent.setInteractionDeviceId(interactionDeviceId);

		// notificationdes
		NotificationDes notificationdes = new NotificationDes();
		if (message.startsWith("Welcome")) {
			notificationdes.setType("String");
			notificationdes.setValue(message + " " + operatorName);
			hmdnotificationEvent.setNotificationdes(notificationdes);
		} else {
			if (jsonAttributes.has("notificationdes")) {
				ObjectNode objNotificationDes = (ObjectNode) jsonAttributes.get("notificationdes");
				notificationdes.setType(objInteractionDeviceId.get("type").textValue());
				notificationdes.setType(message + " " + objInteractionDeviceId.get("value").textValue());
				hmdnotificationEvent.setNotificationdes(notificationdes);
			}
		}

		// Timeout
		// ObjectNode objTimeout = (ObjectNode) jsonAttributes.get("timeout");
		Timeout timeout = new Timeout();
		timeout.setType("int");
		timeout.setValue("1");
		hmdnotificationEvent.setTimeout(timeout);

		hmdnotificationEvent.setTimestamp(Util.getTimeStamp());

		log.info("Method createNewNGSIHMDNotificationEvent end ...");

		return hmdnotificationEvent;
	}

}
