package it.eng.orion.cb.ngsi.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationEvent {

	private String id;
	
	private String type;
	
	@JsonProperty("eventtype")
	private EventType eventType;
	
	@JsonProperty("personid")
	private PersonID personID;
	
	@JsonProperty("equipmentid")
	private EquipmentID equipmentID;
	
	@JsonProperty("equipmentdes")
	private EquipmentDes equipmentDes;
	
	@JsonProperty("verbosity")
	private Verbosity verbosity;
	
	@JsonProperty("timestamp")
	private Timestamp timestamp;
	
	@JsonProperty("payload")
	private Payload payload;
	
	@JsonProperty("location")
	private Location location;
	
	@JsonProperty("annotationid")
	private AnnotationId annotationId;
	
	@JsonProperty("annotationdes")
	private AnnotationDes annotationDes;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public PersonID getPersonID() {
		return personID;
	}

	public void setPersonID(PersonID personID) {
		this.personID = personID;
	}

	public EquipmentID getEquipmentID() {
		return equipmentID;
	}

	public void setEquipmentID(EquipmentID equipmentID) {
		this.equipmentID = equipmentID;
	}

	public EquipmentDes getEquipmentDes() {
		return equipmentDes;
	}

	public void setEquipmentDes(EquipmentDes equipmentDes) {
		this.equipmentDes = equipmentDes;
	}

	public Verbosity getVerbosity() {
		return verbosity;
	}

	public void setVerbosity(Verbosity verbosity) {
		this.verbosity = verbosity;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Payload getPayload() {
		return payload;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public AnnotationId getAnnotationId() {
		return annotationId;
	}

	public void setAnnotationId(AnnotationId annotationId) {
		this.annotationId = annotationId;
	}

	public AnnotationDes getAnnotationDes() {
		return annotationDes;
	}

	public void setAnnotationDes(AnnotationDes annotationDes) {
		this.annotationDes = annotationDes;
	}
	
	
	
}
