package it.eng.orion.cb.ngsi.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationEvent {

	private String id;
	
	private String type;
	
	@JsonProperty("EventType")
	private EventType eventType;
	
	@JsonProperty("PersonID")
	private PersonID personID;
	
	@JsonProperty("EquipmentID")
	private EquipmentID equipmentID;
	
	@JsonProperty("EquipmentDes")
	private EquipmentDes equipmentDes;
	
	@JsonProperty("Verbosity")
	private Verbosity verbosity;
	
	@JsonProperty("Timestamp")
	private Timestamp timestamp;
	
	@JsonProperty("Payload")
	private Payload payload;
	
	@JsonProperty("Location")
	private Location location;

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
	
}
