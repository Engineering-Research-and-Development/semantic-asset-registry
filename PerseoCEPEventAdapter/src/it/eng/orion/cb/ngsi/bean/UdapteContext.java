package it.eng.orion.cb.ngsi.bean;

import java.util.List;

public class UdapteContext {

	private String actionType;
	
	private List<Object> entities;

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public List<Object> getEntities() {
		return entities;
	}

	public void setEntities(List<Object> entities) {
		this.entities = entities;
	}
	
	
}
