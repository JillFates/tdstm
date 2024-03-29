package net.transitionmanager.person

import com.tdsops.common.sql.SqlUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.common.EmailDispatch
import net.transitionmanager.common.ExceptionDates
import net.transitionmanager.imports.DataScript
import net.transitionmanager.imports.Dataview
import net.transitionmanager.imports.ImportBatch
import net.transitionmanager.imports.TaskBatch
import net.transitionmanager.model.Model
import net.transitionmanager.model.ModelSync
import net.transitionmanager.notice.Notice
import net.transitionmanager.notice.NoticeAcknowledgement
import net.transitionmanager.party.Party
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.party.PartyRole
import net.transitionmanager.project.MoveEventNews
import net.transitionmanager.project.MoveEventStaff
import net.transitionmanager.project.Project
import net.transitionmanager.security.PasswordReset
import net.transitionmanager.security.RoleType
import net.transitionmanager.security.UserLogin
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.CommentNote
import net.transitionmanager.task.RecipeVersion

class Person extends Party {

	// Data of Special Person Required by the System
	static final Map<String, String> SYSTEM_USER_AT = [lastName: 'Task', firstName: 'Automated']

	// The fullName valueMapKey when user is looking for a person using fullName within the ETL
	static final String FULLNAME_KEY = 'fullName'

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

	static hasMany = [blackOutDates: ExceptionDates]
	static hasOne = [userLogin: UserLogin]

	static constraints = {
		active blank: false, inList: ['Y', 'N']
		country nullable: true, size: 0..255
		department nullable: true, size: 0..255
		email matches: "(?:[a-zA-Z0-9!#\\\$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#\\\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}|(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\]))", nullable: true
		firstName blank: false, size: 1..34
		keyWords nullable: true, size: 0..255
		lastName size: 0..34
		location nullable: true, size: 0..255
		middleName size: 0..20
		mobilePhone nullable: true, phoneNumber: true, size: 0..255
		modelScore nullable: true
		nickName nullable: true, size: 0..34
		personImageURL nullable: true, size: 0..255
		staffType blank: false, inList: ['Contractor', 'Hourly', 'Salary']
		stateProv nullable: true, size: 0..255
		tdsLink nullable: true, size: 0..255
		tdsNote nullable: true, size: 0..255
		title nullable: true, size: 0..34
		travelOK range: 0..1
		workPhone nullable: true, phoneNumber: true, size: 0..255
		userLogin nullable: true
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
		'assignedTeams',
		'company',
		'domainReferences',
		'enabled',
		'lastNameFirst',
		'lastNameFirstAndTitle',
		'name',
		'suitableTeams',
		'systemUser'
		//'userLogin'
	]

	// The list of domains and the propert(y/ies) that have a reference the Person domain
	static final List<Map> domainReferences = [
			[domain: Application, 			onDelete: 'null',   properties: ['sme', 'sme2'] ],
			[domain: Application, 			onDelete: 'null',   properties: ['shutdownBy', 'startupBy', 'testingBy'], transform:{ it.id.toString() } ],
			[domain: AssetComment, 			onDelete: 'null',   properties: ['resolvedBy', 'createdBy', 'assignedTo'] ],
			[domain: AssetDependency, 		onDelete: 'null',   properties: ['createdBy','updatedBy'] ],
			[domain: AssetEntity, 			onDelete: 'null',   properties: ['appOwner', 'modifiedBy'] ],
			[domain: CommentNote, 			onDelete: 'null',   properties: ['createdBy'] ],
			[domain: DataScript,        	onDelete: 'null',   properties: ['createdBy', 'lastModifiedBy'] ],
			[domain: Dataview, 				onDelete: 'null',   properties: ['person'] ],
			[domain: EmailDispatch, 		onDelete: 'delete', properties: ['toPerson'] ],
			[domain: EmailDispatch, 		onDelete: 'null',   properties: ['createdBy'] ],
			[domain: ExceptionDates, 		onDelete: 'delete', properties: ['person'] ],
			[domain: ImportBatch, 			onDelete: 'null',   properties: ['createdBy'] ],
			[domain: Model, 				onDelete: 'null',   properties: ['createdBy', 'updatedBy', 'validatedBy'] ],
			[domain: ModelSync, 			onDelete: 'null',   properties: ['createdBy', 'updatedBy', 'validatedBy'] ],
			[domain: MoveEventNews, 		onDelete: 'null',   properties: ['archivedBy', 'createdBy'] ],
			[domain: MoveEventStaff, 		onDelete: 'delete', properties: ['person'] ],
			[domain: Notice, 				onDelete: 'null',   properties: ['createdBy'] ],
			[domain: NoticeAcknowledgement, onDelete: 'null', 	properties: ['person'] ],
			[domain: PartyRole, 			onDelete: 'delete', properties: ['party'] ],
			[domain: PartyRelationship, 	onDelete: 'delete', properties: ['partyIdFrom'] ],
			[domain: PartyRelationship, 	onDelete: 'delete', properties: ['partyIdTo'] ],
			[domain: PasswordReset, 		onDelete: 'null',   properties: ['createdBy'] ],
			[domain: RecipeVersion, 		onDelete: 'null',   properties: ['createdBy'] ],
			[domain: TaskBatch, 			onDelete: 'null',   properties: ['createdBy'] ],
			[domain: UserLogin, 			onDelete: 'delete', properties: ['person'] ]
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
		def staffRef = StringUtil.toLongIfString(this)
		boolean byId = (staffRef instanceof Long)
		String query = """select pr.partyIdFrom from
					PartyRelationship pr where
					pr.partyRelationshipType.id = 'STAFF'
					and pr.roleTypeCodeFrom.id = '$RoleType.CODE_PARTY_COMPANY'
					and pr.roleTypeCodeTo.id = '$RoleType.CODE_PARTY_STAFF'
					and pr.partyIdTo${(byId ? '.id' : '')} = :staff"""
		List<PartyGroup> company = PartyRelationship.executeQuery(query, [staff: staffRef])

		return (company ? company[0] : null)
	}

	/**
	 * The projects that the person is assigned to.
	 */
	List<Project> getAssignedProjects() {
		executeQuery("""
			SELECT pr.partyIdFrom
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='PROJ_STAFF'
			  AND pr.partyIdTo=?0
			  AND pr.roleTypeCodeFrom='$RoleType.CODE_PARTY_PROJECT'
			  AND pr.roleTypeCodeTo='$RoleType.CODE_PARTY_STAFF'
		""".toString(), [this])
	}

	/**
	 * The teams that a person is assigned to for a given project
	 * @param project - the project to search for teams
	 * @return a list of the RoleType records that represent the teams that a person belongs to a project
	 */
	List<RoleType> getTeamsCanParticipateIn() {
		executeQuery("""
			SELECT pr.roleTypeCodeTo
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='STAFF'
			  AND pr.roleTypeCodeFrom='$RoleType.CODE_PARTY_COMPANY'
			  AND pr.partyIdFrom=:company
			  AND pr.partyIdTo=:person
			  AND pr.roleTypeCodeTo.type=:team
		""".toString(), [company: company, person: this, team: RoleType.TYPE_TEAM])
	}

	/**
	 * The teams that a person is assigned to for a given project
	 * @param project - the project to search for teams
	 */
	List<RoleType> getTeamsAssignedTo(Project project) {
		executeQuery("""
			SELECT pr.roleTypeCodeTo
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='PROJ_STAFF'
			  AND pr.roleTypeCodeFrom='$RoleType.CODE_PARTY_PROJECT'
			  AND pr.partyIdFrom=:project
			  AND pr.partyIdTo=:person
			  AND pr.roleTypeCodeTo.type=:team
		""".toString(), [project: project, person: this, team: RoleType.TYPE_TEAM])
	}

	/**
	 * The teams that a person has been indicated as being suitable to participate with.
	 */
	List<RoleType> getSuitableTeams() {
		executeQuery("""
			SELECT pr.roleTypeCodeTo
			FROM PartyRelationship pr
			WHERE pr.partyRelationshipType='STAFF'
			  AND pr.roleTypeCodeFrom='$RoleType.CODE_PARTY_COMPANY'
			  AND pr.partyIdFrom=:company
			  AND pr.partyIdTo=:person
			  AND pr.roleTypeCodeTo <> '$RoleType.CODE_PARTY_STAFF'
		""".toString(), [company: company, person: this])
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
	 * Whether person is automatic user used to execute tasks
	 *
	 * @return boolean
	 */
	boolean isAutomatic() {
		return SYSTEM_USER_AT.firstName == firstName && SYSTEM_USER_AT.lastName == lastName
	}

	/**
	 * Whether the Person instance is deleteable, i.e. AutomaticPerson shouldn't be deleted.
	 */
	boolean isSystemUser() {
		SYSTEM_USER_AT.firstName == firstName && SYSTEM_USER_AT.lastName == lastName
	}

	String toString() {
		return getFullName()
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
	Map toMap() {
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
				stateProv: stateProv ?: '',
				keyWords: keyWords,
				tdsNote: tdsNote,
				tdsLink: tdsLink,
				travelOK: travelOK,
				teams: teams,
				staffType: staffType,
				personImageURL: personImageURL
		]
		return data.asImmutable()
	}

	/**
	 * Construct and escape this person's full name.
	 */
	String getFullName() {
		return HtmlUtil.escape(buildFullName())
	}

	/**
	 * Put together this person's full name (no escaping).
	 * @return
	 */
	private String buildFullName() {
		return StringUtil.join([firstName, middleName, lastName])
	}
}
