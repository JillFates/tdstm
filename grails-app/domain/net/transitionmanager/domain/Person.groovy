package net.transitionmanager.domain

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.CommentNote
import net.transitionmanager.EmailDispatch
import net.transitionmanager.PasswordReset
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.NoticeAcknowledgment
import net.transitionmanager.service.PartyRelationshipService

// Domain classes that Person is associated with that are impacted by merging which are not in
// this package.

class Person extends Party {

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
	String personImageURL
	String keyWords
	String tdsNote
	String tdsLink
	String staffType = 'Salary'
	Integer travelOK = 0

	transient PartyRelationshipService partyRelationshipService

	static hasMany = [blackOutDates: ExceptionDates]
	static hasOne = [userLogin: UserLogin]

	static constraints = {
		active blank: false, inList: ['Y', 'N']
		country nullable: true
		department nullable: true
		email matches: "(?:[a-zA-Z0-9!#\\\$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#\\\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}|(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\]))", nullable: true
		firstName blank: false, size: 1..34
		keyWords nullable: true
		lastName size: 0..34
		location nullable: true
		middleName size: 0..20
		mobilePhone nullable: true, phoneNumber: true
		modelScore nullable: true
		nickName nullable: true, size: 0..34
		personImageURL nullable: true
		staffType blank: false, inList: ['Contractor', 'Hourly', 'Salary']
		stateProv nullable: true
		tdsLink nullable: true
		tdsNote nullable: true
		title nullable: true, size: 0..34
		travelOK range: 0..1
		workPhone nullable: true, phoneNumber: true
		userLogin nullable: true
	}

	static mapping = {
		autowire true
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
		'assignedTeams',
		'automaticPerson',
		'company',
		'domainReferences',
		'enabled',
		'lastNameFirst',
		'lastNameFirstAndTitle',
		'name',
		'partyRelationshipService',
		'suitableTeams',
		'systemUser'
		//'userLogin'
	]

	// The list of domains and the propert(y/ies) that have a reference the Person domain
	static final List<Map> domainReferences = [
		[domain: Application, 		onDelete: 'null',   properties: ['sme', 'sme2'] ],
		[domain: Application, 		onDelete: 'null',   properties: ['shutdownBy', 'startupBy', 'testingBy'], transform:{ it.id.toString() } ],
		[domain: AssetComment, 		onDelete: 'null',   properties: ['resolvedBy', 'createdBy', 'assignedTo'] ],
		[domain: AssetDependency, 	onDelete: 'null',   properties: ['createdBy','updatedBy'] ],
		[domain: AssetEntity, 		onDelete: 'null',   properties: ['appOwner', 'modifiedBy'] ],
		[domain: CommentNote, 		onDelete: 'null',   properties: ['createdBy'] ],
		[domain: DataScript,        onDelete: 'null',   properties: ['createdBy', 'lastModifiedBy'] ],
		[domain: Dataview, 			onDelete: 'null',   properties: ['person'] ],
		[domain: EmailDispatch, 	onDelete: 'delete', properties: ['toPerson'] ],
		[domain: EmailDispatch, 	onDelete: 'null',   properties: ['createdBy'] ],
		[domain: ExceptionDates, 	onDelete: 'delete', properties: ['person'] ],
		[domain: ImportBatch, 		onDelete: 'null',   properties: ['createdBy'] ],
		[domain: Model, 			onDelete: 'null',   properties: ['createdBy', 'updatedBy', 'validatedBy'] ],
		[domain: ModelSync, 		onDelete: 'null',   properties: ['createdBy', 'updatedBy', 'validatedBy'] ],
		[domain: MoveEventNews, 	onDelete: 'null',   properties: ['archivedBy', 'createdBy'] ],
		[domain: MoveEventStaff, 	onDelete: 'delete', properties: ['person'] ],
		[domain: Notice, 			onDelete: 'null',   properties: ['createdBy'] ],
		[domain: NoticeAcknowledgment, onDelete: 'null',   properties: ['person'] ],
		[domain: PartyRole, 		onDelete: 'delete', properties: ['party'] ],
		[domain: PartyRelationship, onDelete: 'delete', properties: ['partyIdFrom'] ],
		[domain: PartyRelationship, onDelete: 'delete', properties: ['partyIdTo'] ],
		[domain: PasswordReset, 	onDelete: 'null',   properties: ['createdBy'] ],
		[domain: RecipeVersion, 	onDelete: 'null',   properties: ['createdBy'] ],
		[domain: TaskBatch, 		onDelete: 'null',   properties: ['createdBy'] ],
		[domain: UserLogin, 		onDelete: 'delete', properties: ['person'] ],
		[domain: Workflow, 			onDelete: 'null',   properties: ['updatedBy'] ]
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
			  AND pr.roleTypeCodeFrom='ROLE_COMPANY'
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
			  AND pr.roleTypeCodeFrom='ROLE_COMPANY'
			  AND pr.partyIdFrom=:company
			  AND pr.partyIdTo=:person
			  AND pr.roleTypeCodeTo <> 'ROLE_STAFF'
		''', [company: company, person: this])
	}

	boolean isEnabled() {
		active == 'Y'
	}

	void disable() {
		active = 'N'
	}

/*
	UserLogin getUserLogin() {
		if (!userLogin) {
			userLogin = UserLogin.findByPerson(this)
		}
		userLogin
	}
*/
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
			def msg = "${this} is a System User Account and can not be deleted"
			log.warn(msg)
			throw new UnsupportedOperationException(msg)
		}
	}

	/**
	 * Converts this person object to a map
	 * @return
	 */
	Map toMap(Project project) {
		def teams = getTeamsCanParticipateIn()
		Map data = [
				id: id,
				firstName: firstName,
				middleName: middleName,
				lastName: lastName,
				nickName: nickName,
				title: title,
				email: email,
				department: department,
				location: location,
				workPhone: workPhone,
				mobilePhone: mobilePhone,
				active: active,
				company: company.toString(),
				country: country,
				stateProv: stateProv,
				keyWords: keyWords,
				tdsNote: tdsNote,
				tdsLink: tdsLink,
				travelOK: travelOK,
				teams: teams,
				staffType: staffType
		]
		return data.asImmutable()
	}
}
