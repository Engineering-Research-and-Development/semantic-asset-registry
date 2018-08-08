package it.eng.cam.rest.sesame.dto;

import java.io.Serializable;
import java.util.List;

public class AssetByModelJSON implements Serializable {
	
	private static final long serialVersionUID = 5193319913496609626L;
	 
	private String assetName;
	private String className;
	private String modelName;
	private String domainName;
	private String orionConfigId;
	
	private List<AttributeJSON> attribute;
	
	public AssetByModelJSON() {
    }

	public AssetByModelJSON(String assetName, String className, String modelName, String domainName, String orionConfigId, List<AttributeJSON> attribute ) {
		
		this.assetName = assetName;
		this.className = className;
		this.modelName = modelName;
		this.domainName = domainName;
		this.orionConfigId = orionConfigId;
		this.attribute = attribute;
		
	}

	

	public String getOrionConfigId() {
		return orionConfigId;
	}

	public void setOrionConfigId(String orionConfigId) {
		this.orionConfigId = orionConfigId;
	}

	public List<AttributeJSON> getAttribute() {
		return attribute;
	}

	public void setAttribute(List<AttributeJSON> attribute) {
		this.attribute = attribute;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
}
