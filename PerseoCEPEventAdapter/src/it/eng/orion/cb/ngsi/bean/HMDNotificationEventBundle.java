package it.eng.orion.cb.ngsi.bean;

import java.util.ArrayList;

public class HMDNotificationEventBundle {

	private String actionType;

	private ArrayList<HMDNotificationEvent> entities = new ArrayList<HMDNotificationEvent>();

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public ArrayList<HMDNotificationEvent> getEntities() {
		return entities;
	}

	public void setEntities(ArrayList<HMDNotificationEvent> entities) {
		this.entities = entities;
	}
	
	public void addHMDNotificationEvent(HMDNotificationEvent hmdNotificationEvent) {
		this.entities.add(hmdNotificationEvent);
	}

}
