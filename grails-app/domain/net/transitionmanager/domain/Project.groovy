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

	String projectCode
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

	// used to indicate which of the custom fields will represent the plan methodology setting
	String planMethodology=''

	// Custom field labels
	String custom1
	String custom2
	String custom3
	String custom4
	String custom5
	String custom6
	String custom7
	String custom8
	String custom9
	String custom10
	String custom11
	String custom12
	String custom13
	String custom14
	String custom15
	String custom16
	String custom17
	String custom18
	String custom19
	String custom20
	String custom21
	String custom22
	String custom23
	String custom24
	String custom25
	String custom26
	String custom27
	String custom28
	String custom29
	String custom30
	String custom31
	String custom32
	String custom33
	String custom34
	String custom35
	String custom36
	String custom37
	String custom38
	String custom39
	String custom40
	String custom41
	String custom42
	String custom43
	String custom44
	String custom45
	String custom46
	String custom47
	String custom48
	String custom49
	String custom50
	String custom51
	String custom52
	String custom53
	String custom54
	String custom55
	String custom56
	String custom57
	String custom58
	String custom59
	String custom60
	String custom61
	String custom62
	String custom63
	String custom64
	String custom65
	String custom66
	String custom67
	String custom68
	String custom69
	String custom70
	String custom71
	String custom72
	String custom73
	String custom74
	String custom75
	String custom76
	String custom77
	String custom78
	String custom79
	String custom80
	String custom81
	String custom82
	String custom83
	String custom84
	String custom85
	String custom86
	String custom87
	String custom88
	String custom89
	String custom90
	String custom91
	String custom92
	String custom93
	String custom94
	String custom95
	String custom96

	static hasMany = [dataTransferBatch: DataTransferBatch]

	static constraints = {
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


		// custom fields

		// we can save some redundant space by dynamically setting all 96 columns to be nullable in a loop;
		// since the constraints DSL is implemented as method calls with the property name as the method
		// name and a single Map argument containing the data for the various constraints, it's simple
		// to dynamically create a method call to be called on the closure's delegate:

		Map nullableTrue = Collections.singletonMap('nullable', true)
		(1..96).each { "custom$it"(nullableTrue) }

		lastAssetId nullable: true
		runbookOn nullable: true
		depConsoleCriteria nullable: true
	}

	static mapping = {
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
		"$id : $projectCode : $name"
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
