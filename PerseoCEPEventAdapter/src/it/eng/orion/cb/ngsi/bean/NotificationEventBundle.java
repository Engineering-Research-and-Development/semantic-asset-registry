package it.eng.orion.cb.ngsi.bean;

import java.util.ArrayList;

public class NotificationEventBundle {
	
	private String actionType;
	
	private ArrayList<NotificationEvent> entities = new ArrayList<NotificationEvent>();

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public ArrayList<NotificationEvent> getEntities() {
		return entities;
	}

	public void setEntities(ArrayList<NotificationEvent> entities) {
		this.entities = entities;
	}
	
	public void addNotificationEvent(NotificationEvent notificationEvent) {
		this.entities.add(notificationEvent);
	}

	
}
