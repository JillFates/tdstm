package net.tds.util.jmesa;

import java.io.Serializable;
import java.util.Date;

public class AssetCommentBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String assetType;
	private String assetName;
	private String commentType;
	private String status;
	private String description;
	private Integer taskNumber;
	private String comment;
	private Date lastUpdated;
	private Date dueDate;
	private String assignedTo;
	private String role;
	private String category;
	private Integer succCount;
	private Long assetEntityId;
	
	// CSS class names used for the Updated, Due and Status columns
	private String updatedClass="";
	private String dueClass="";
	private String statusClass="";
	private String elapsedAgo="";
	private Integer score;
	private Boolean isRunbookTask=false;
	private Integer hardAssigned=0;
	
	
	public AssetCommentBean() {}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getAssetType() {
		return this.assetType;
	}
	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}

	public String getAssetName() {
		return this.assetName;
	}
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getCommentType() {
		return this.commentType;
	}
	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}

	public String getStatus() {
		return this.status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getScore() {
		return this.score;
	}
	public void setScore(Integer score) {
		this.score = score;
	}
	
	public Long getAssetEntityId() {
		return this.assetEntityId;
	}
	public void setAssetEntityId(Long assetEntityId) {
		this.assetEntityId = assetEntityId;
	}
	public Integer getTaskNumber() {
		return this.taskNumber;
	}
	public void setTaskNumber(Integer taskNumber) {
		this.taskNumber = taskNumber;
	}

	public String getComment() {
		return this.comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getLastUpdated() {
		return this.lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getDueDate() {
		return this.dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getAssignedTo() {
		return this.assignedTo;
	}
	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	public String getRole() {
		return this.role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	public String getCategory() {
		return this.category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getSuccCount() {
		return this.succCount;
	}
	public void setSuccCount(Integer succCount) {
		this.succCount = succCount;
	}
	
	// score
	public String getElapsedAgo() {
		return this.elapsedAgo;
	}
	public void setElapsedAgo(String ago) {
		this.elapsedAgo = ago;
	}
	
	// StatusClass
	public String getStatusClass() {
		return this.statusClass;
	}
	public void setStatusClass(String val) {
		this.statusClass = val;
	}
	
	// DueClass
	public String getDueClass() {
		return this.dueClass;
	}
	public void setDueClass(String val) {
		this.dueClass = val;
	}
	
	// UpdatedClass
	public String getUpdatedClass() {
		return this.updatedClass;
	}
	public void setUpdatedClass(String val) {
		this.updatedClass = val;
	}
	
	// isRunbookTask
	public Boolean isRunbookTask() {
		return this.isRunbookTask;
	}
	public void setRunbookTask( Boolean isRb ) {
		this.isRunbookTask = isRb;
	}

	// hardAssigned
	public Integer getHardAssigned() {
		return this.hardAssigned;
	}

	public void setHardAssigned(Integer hardAssigned) {
		this.hardAssigned = hardAssigned;
	}
}
