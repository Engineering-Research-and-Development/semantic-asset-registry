package it.eng.orion.cb.ngsi.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HMDNotificationEvent {

	private String id;

	private String type;

	@JsonProperty("interactiondeviceId")
	private InteractionDeviceId interactionDeviceId;

	@JsonProperty("notificationdes")
	private NotificationDes notificationdes;

	@JsonProperty("timeout")
	private Timeout timeout;

	@JsonProperty("timestamp")
	private Timestamp timestamp;

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

	public InteractionDeviceId getInteractionDeviceId() {
		return interactionDeviceId;
	}

	public void setInteractionDeviceId(InteractionDeviceId interactionDeviceId) {
		this.interactionDeviceId = interactionDeviceId;
	}

	public NotificationDes getNotificationdes() {
		return notificationdes;
	}

	public void setNotificationdes(NotificationDes notificationdes) {
		this.notificationdes = notificationdes;
	}

	public Timeout getTimeout() {
		return timeout;
	}

	public void setTimeout(Timeout timeout) {
		this.timeout = timeout;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

}
