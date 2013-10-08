package net.tds.util.jmesa;

import java.io.Serializable;
import java.util.Date;

public class AssetEntityBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private String assetTag;
	private String assetType;
	private String assetName;
	private String commentType;
	private Integer priority;
	private String sourceTeamMt;
	private String targetTeamMt;
	private String status;
	private String cssClass;
	private boolean checkVal;

	private String application;
	private String appOwner;
	private String appSme;
	private String moveBundle;
	private String planStatus;

	private String dbFormat;

	private String fileFormat;
	private Integer size;

	private Integer depUp; 		// dependencies support count
	private Integer depDown; 	// dependencies down count
	private Integer dependencyBundleNumber;

	private String model;
	private String sourceLocation;
	private String sourceRack;
	private String targetLocation;
	private String targetRack;
	private String serialNumber;
	private String validation;
	
	
	private String name;
	private String description;
	private String aka;
	private Integer modelCount;
	private Integer count;
	
	private String userName;
	private String person;
	private Date lastLogin;
	private String company;
	private Date dateCreated;
	private Date expiryDate;
	private String role;
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	// Default constructor
	public AssetEntityBean() {}
	
	public Integer getModelCount() {
		return modelCount;
	}

	public void setModelCount(Integer modelCount) {
		this.modelCount = modelCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAka() {
		return aka;
	}

	public void setAka(String aka) {
		this.aka = aka;
	}

	// count
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}

	// validation
	public String getValidation() {
		return validation;
	}
	public void setValidation(String validation) {
		this.validation = validation;
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

	public String getAssetType() {
		return assetType;
	}

	public void setAssetType(String assetType) {
		this.assetType = assetType;
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

	// Status
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String clazz) {
		this.cssClass = clazz;
	}

	public boolean getCheckVal() {
		return checkVal;
	}

	public void setCheckVal(boolean checkVal) {
		this.checkVal = checkVal;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getAppOwner() {
		return appOwner;
	}

	public void setAppOwner(String appOwner) {
		this.appOwner = appOwner;
	}

	public String getAppSme() {
		return appSme;
	}

	public void setAppSme(String appSme) {
		this.appSme = appSme;
	}

	public String getMoveBundle() {
		return moveBundle;
	}

	public void setMoveBundle(String moveBundle) {
		this.moveBundle = moveBundle;
	}

	public String getPlanStatus() {
		return planStatus;
	}

	public void setPlanStatus(String planStatus) {
		this.planStatus = planStatus;
	}

	public String getDbFormat() {
		return dbFormat;
	}

	public void setDbFormat(String dbFormat) {
		this.dbFormat = dbFormat;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getDepUp() {
		return depUp;
	}

	public void setDepUp(Integer depUp) {
		this.depUp = depUp;
	}

	public Integer getDepDown() {
		return depDown;
	}

	public void setDepDown(Integer depDown) {
		this.depDown = depDown;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSourceLocation() {
		return sourceLocation;
	}

	public void setSourceLocation(String sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public String getSourceRack() {
		return sourceRack;
	}

	public void setSourceRack(String sourceRack) {
		this.sourceRack = sourceRack;
	}

	public String getTargetLocation() {
		return targetLocation;
	}

	public void setTargetLocation(String targetLocation) {
		this.targetLocation = targetLocation;
	}

	public String getTargetRack() {
		return targetRack;
	}

	public void setTargetRack(String targetRack) {
		this.targetRack = targetRack;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public Integer getDependencyBundleNumber() {
		return dependencyBundleNumber;
	}

	public void setDependencyBundleNumber(Integer dependencyBundleNumber) {
		this.dependencyBundleNumber = dependencyBundleNumber;
	}
}
