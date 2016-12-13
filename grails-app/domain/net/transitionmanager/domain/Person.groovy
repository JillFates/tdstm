package net.transitionmanager.domain

import net.transitionmanager.service.PartyRelationshipService

class Person extends Party {

	//COMPANION
	// Data of Special Person Required by the System
	static final Map<String, String> SYSTEM_USER_AT = [lastName: 'Task', firstName: 'Automated']

	String firstName
	String middleName = ''
	String lastName = ''
	String nickName
	String active = 'Y'
	String title
	String email
	String department
	String location
	String stateProv
	String country
	String workPhone
	String mobilePhone
	Integer modelScore = 0
	Integer modelScoreBonus = 0
	String personImageURL
	String keyWords
	String tdsNote
	String tdsLink
	String staffType = 'Salary'
	Integer travelOK = 1

	transient PartyRelationshipService partyRelationshipService
	transient UserLogin userLogin

	static hasMany = [blackOutDates: ExceptionDates]

	static constraints = {
		active blank: false, inList: ['Y', 'N']
		country nullable: true
		department nullable: true
		email email: true, nullable: true
		firstName blank: false, size: 1..34
		keyWords nullable: true
		lastName size: 0..34
		location nullable: true
		middleName size: 0..20
		mobilePhone nullable: true, phoneNumber: true
		modelScore nullable: true
		modelScoreBonus nullable: true
		nickName nullable: true, size: 0..34
		personImageURL nullable: true
		staffType blank: false, inList: ['Contractor', 'Hourly', 'Salary']
		stateProv nullable: true
		tdsLink nullable: true
		tdsNote nullable: true
		title nullable: true, size: 0..34
		travelOK nullable: true
		workPhone nullable: true, phoneNumber: true
	}

	static mapping = {
		version false
		autoTimestamp false
		tablePerHierarchy false
		id column: 'person_id'
		columns {
			active sqlType: 'char(1)'
			firstName sqlType: 'varchar(34)'
			lastName sqlType: 'varchar(34)'
			middleName sqlType: 'varchar(20)'
			nickName sqlType: 'varchar(34)'
			title sqlType: 'varchar(34)'
			travelOK sqlType: 'tinyint'
		}
	}

	static transients = [
		'assignedProjects',
		'automaticPerson',
		'company',
		'enabled',
		'lastNameFirst',
		'lastNameFirstAndTitle',
		'partyRelationshipService',
		'suitableTeams',
		'systemUser',
		'teamsCanParticipateIn',
		'userLogin'
	]

	/**
	 * This method was incorrectly implemented but not sure where it may be used so an exception has been
	 * added so that we may catch any possible location in testing. At some point we can blow away this method.
	 * JPM 2/26/2016
	 */
	String name() {
		throw new RuntimeException('Person.name method should not be used')
	}

	/**
	 * The company for this person.
	 */
	Party getCompany() {
		partyRelationshipService.getCompanyOfStaff(this)
	}

	/**
	 * The projects that the person is assigned to.
	 */
	List<Project> getAssignedProjects() {
		executeQuery('''
			SELECT pr.partyIdFrom
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='PROJ_STAFF'
			  AND pr.partyIdTo=?
			  AND pr.roleTypeCodeFrom='PROJECT'
			  AND pr.roleTypeCodeTo='STAFF'
		''', [this])
	}

	/**
	 * The teams that a person is assigned to for a given project
	 * @param project - the project to search for teams
	 * @return a list of the RoleType records that represent the teams that a person belongs to a project
	 */
	List<RoleType> getTeamsCanParticipateIn() {
		executeQuery('''
			SELECT pr.roleTypeCodeTo
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='STAFF'
			  AND pr.roleTypeCodeFrom='COMPANY'
			  AND pr.partyIdFrom=:company
			  AND pr.partyIdTo=:person
			  AND pr.roleTypeCodeTo.type=:team
		''', [company: company, person: this, team: RoleType.TEAM])
	}

	/**
	 * The teams that a person is assigned to for a given project
	 * @param project - the project to search for teams
	 */
	List<RoleType> getTeamsAssignedTo(Project project) {
		executeQuery('''
			SELECT pr.roleTypeCodeTo
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='PROJ_STAFF'
			  AND pr.roleTypeCodeFrom='PROJECT'
			  AND pr.partyIdFrom=:project
			  AND pr.partyIdTo=:person
			  AND pr.roleTypeCodeTo.type=:team
		''', [project: project, person: this, team: RoleType.TEAM])
	}

	/**
	 * The teams that a person has been indicated as being suitable to participate with.
	 */
	List<RoleType> getSuitableTeams() {
		executeQuery('''
			SELECT pr.roleTypeCodeTo
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='STAFF'
			  AND pr.roleTypeCodeFrom='COMPANY'
			  AND pr.partyIdFrom=:company
			  AND pr.partyIdTo=:person
			  AND pr.roleTypeCodeTo <> 'STAFF'
		''', [company: company, person: this])
	}

	boolean isEnabled() {
		active == 'Y'
	}

	void disable() {
		active = 'N'
	}

	UserLogin getUserLogin() {
		if (!userLogin) {
			userLogin = UserLogin.findByPerson(this)
		}
		userLogin
	}

	/**
	 * The person's name in 'LastName, FirstName MiddleName' format
	 */
	String getLastNameFirst() {
		(lastName ? lastName + ', ' : '') + firstName + (middleName ? ' ' + middleName : '')
	}

	/**
	 * The person's name in 'LastName, FirstName MiddleName - Title' format
	 */
	String getLastNameFirstAndTitle() {
		lastNameFirst + (title ? ' - ' + title : '')
	}

	/**
	 * The Person that completes automated tasks
	 * This method should not have been implemented here as it is in the TaskService
	 */
	Person getAutomaticPerson() {
		throw new RuntimeException('Person.getAutomaticPerson() deprecated - use TaskService.getAutomaticPerson()')
	}

	/**
	 * Whether the Person instance is deleteable, i.e. AutomaticPerson shouldn't be deleted.
	 */
	boolean isSystemUser() {
		SYSTEM_USER_AT.firstName == firstName && SYSTEM_USER_AT.lastName == lastName
	}

	String toString() {
		firstName + (middleName ? ' ' + middleName : '') + (lastName ? ' ' + lastName : '')
	}

	def beforeValidate() {
		if (middleName == null) {
			middleName = ''
		}
		if (lastName == null) {
			lastName = ''
		}
	}

	def beforeDelete = {
		if (isSystemUser()) {
			def msg = "${this}: is a System User and can't be Deleted"
			log.warn(msg)
			throw new UnsupportedOperationException(msg)
		}
	}
}
