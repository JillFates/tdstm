package net.tds.util.jmesa;

import java.io.Serializable;


public class AssetEntityBean implements Serializable {
private static final long serialVersionUID = 1L;
	
	private long id;
	private String assetTag;
	private String assetName;
	private String commentType;
	private Integer priority;
	private String sourceTeamMt;
	private String targetTeamMt;
	private String status;
	private String cssClass;
	private boolean checkVal;
	
	public AssetEntityBean() {
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAssetTag() {
		return assetTag;
	}

	public void setAssetTag(String assetTag) {
		this.assetTag = assetTag;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getCommentType() {
		return commentType;
	}

	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getSourceTeamMt() {
		return sourceTeamMt;
	}

	public void setSourceTeamMt(String sourceTeamMt) {
		this.sourceTeamMt = sourceTeamMt;
	}

	public String getTargetTeamMt() {
		return targetTeamMt;
	}

	public void setTargetTeamMt(String targetTeamMt) {
		this.targetTeamMt = targetTeamMt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public boolean getCheckVal() {
		return checkVal;
	}

	public void setCheckVal(boolean checkVal) {
		this.checkVal = checkVal;
	}
}
