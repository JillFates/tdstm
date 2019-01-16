package net.transitionmanager.domain

import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService

class Project extends PartyGroup {

	//COMPANION
	// Data of Special Project Required by the System
	//Todo: Review, Can we guarantee that "TM DEFAULT PROJECT" will always have the ID(2)???
	static final long DEFAULT_PROJECT_ID = 2
	static final int CUSTOM_FIELD_COUNT = 96

	// a constant and non-persistent marker instance used to indicate that all projects should be used
	static final Project ALL = new Project()

	transient ProjectService projectService
	transient PartyRelationshipService partyRelationshipService

	// A code that is use to reference projects with export filename, etc
	String projectCode

	// A unique string value that will be used when aggregating data across multiple instances
	String guid

	String description
	Date startDate   // Date that the project will start
	Date completionDate   // Date that the project will finish
	PartyGroup client
	String workflowCode
	String projectType = 'Standard'
	Integer lastAssetId
	Integer runbookOn = 1 // Flag that indicates that the project should use the runbook mode for various screens
	String depConsoleCriteria
	MoveBundle defaultBundle
	Timezone timezone

	static String alternateKey = 'projectCode'

	// used to indicate which of the custom fields will represent the plan methodology setting
	String planMethodology=''

	// Flag to indicate if the Daily Reporting Metrics should be collected for the project
	Integer collectMetrics = 1

	static hasMany = [dataTransferBatch: DataTransferBatch]

	static constraints = {
		guid size: 36..36
		defaultBundle nullable: true
		client nullable: false
		depConsoleCriteria nullable: true
		description nullable: true
		lastAssetId nullable: true
		projectCode blank: false, unique: 'client'
		projectType blank: false, inList: ['Standard', 'Template', 'Demo']
		runbookOn nullable: true
		startDate nullable: true
		timezone nullable: true
		workflowCode blank: false
		planMethodology nullable: true
		lastAssetId nullable: true
		runbookOn nullable: true
		depConsoleCriteria nullable: true
		collectMetrics range: 0..1
	}

	static mapping = {
		autowire true
		autoTimestamp false
		id column: 'project_id'
		columns {
			projectCode sqlType: 'varchar(20)', index: 'project_projectcode_idx', unique: true
			runbookOn sqlType: 'tinyint'
			depConsoleCriteria sqlType: 'TEXT'
		}
	}

	static transients = ['active', 'defaultProject', 'owner', 'partners', 'partyRelationshipService',
	                     'projectDefaultBundle', 'projectService', 'status']

	String toString() {
		projectCode
	}

	/**
	 * Retrieve the default Project for the appliction.
	 */
	static Project getDefaultProject() {
		get(DEFAULT_PROJECT_ID)
	}

	/**
	 * Retrieve the default Project for the appliction using read().
	 */
	static Project readDefaultProject() {
		read(DEFAULT_PROJECT_ID)
	}

	/**
	 * Determine if this is the default project for the application.
	 * @return  true if the project is the default
	 */
	boolean isDefaultProject() {
		id == DEFAULT_PROJECT_ID
	}

	/**
	 * Used to determine if the parameter references is the Default project
	 * @param p - the project being checked. This can be the Project object or Project id (String or Long)
	 * @return true if the parameter is or references the Default project otherwise false
	 */
	static boolean isDefaultProject(def p) {
		if (p instanceof Project) {
			p.id == DEFAULT_PROJECT_ID
		} else {
			p == DEFAULT_PROJECT_ID
		}
	}

	/**
	 * Determine if this is an active project
	 * @return true if the project is active
	 */
	boolean isActive() {
		//TODO: check time GMT
		completionDate.compareTo(new Date()) > 0
	}

	/**
	 * The partners associated with a project
	 */
	List<PartyGroup> getPartners() {
		partyRelationshipService.getProjectPartners(this)
	}

	/**
	 * The default move bundle for the current project.
	 * @param defaultBundleName  in case a new one is to be created).
	 * @return the MoveBundle
	 */
	MoveBundle getProjectDefaultBundle(String defaultBundleName = null) {
		projectService.getDefaultBundle(this, defaultBundleName)
	}

	/**
	 * The owner of the project
	 */
	PartyGroup getOwner() {
		projectService.getOwner(this)
	}

	/**
	 * Set the owner of the project
	 * @param owner  the project owner
	 */
	void setOwner(PartyGroup owner) {
		projectService.setOwner(this, owner)
	}

	/**
	 * Determine project status, valid values are 'active' or 'completed'.
	 */
	String getStatus() {
		isActive() ? 'active' : 'completed'
	}

	def beforeDelete = {
		if (isDefaultProject()) {
			String msg = "${this}: project is Required by the system and can't be Deleted"
			log.warn(msg)
			throw new UnsupportedOperationException(msg)
		}
	}
}
