package it.eng.cam.rest.sesame.dto;
import java.io.Serializable;


public class OCBSubscriptionJSON implements Serializable{
	
	private String description;
	private String urlCallback;
	private String className;
		
	private static final long serialVersionUID = -6695347893763015696L;
	
	public OCBSubscriptionJSON() {
    }

	public OCBSubscriptionJSON(String description, String urlCallback, String className) {
		super();
		this.description = description;
		this.urlCallback = urlCallback;
		this.className = className;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrlCallback() {
		return urlCallback;
	}

	public void setUrlCallback(String urlCallback) {
		this.urlCallback = urlCallback;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	
	
}
	