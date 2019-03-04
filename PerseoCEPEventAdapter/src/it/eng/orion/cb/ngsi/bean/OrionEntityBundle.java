package it.eng.orion.cb.ngsi.bean;

import java.util.ArrayList;

public class OrionEntityBundle<T> {

	private String actionType;

	private ArrayList<T> entities = new ArrayList<T>();

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public ArrayList<T> getEntities() {
		return entities;
	}

	public void setEntities(ArrayList<T> entities) {
		this.entities = entities;
	}

	public void addEntity(T t) {
		this.entities.add(t);
	}


}
