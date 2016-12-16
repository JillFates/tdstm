class Person extends Party {
	//COMPANION
	// Data of Special Person Required by the System
	private static SYSTEM_USER_AT = [
			lastName:'Task',
			firstName: 'Automated'
	]

	//INSTANCE
	def partyRelationshipService

	String firstName
	String middleName = ""
	String lastName = ""
	String nickName
	String active = "Y"
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

	// static Person loggedInPerson

	static hasMany =[
		blackOutDates : ExceptionDates
	]
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 firstName( blank:false, size:1..34 )
		 middleName( blank:true, size:0..20 )
		 lastName( blank:true, size:0..34 )
		 title( blank:true, nullable:true, size:0..34 )
		 nickName( blank:true, nullable:true, size:0..34 )
		 active( blank:false, nullable:false, inList:['Y','N'] )
		 email(email:true, blank:true, nullable:true)
		 department( blank:true, nullable:true )
		 location( blank:true, nullable:true )
		 stateProv( blank:true, nullable:true )
		 country( blank:true, nullable:true )
		 workPhone(blank:true, nullable:true, phoneNumber:true)
		 mobilePhone(blank:true, nullable:true, phoneNumber:true)
		 modelScore( nullable:true )
		 modelScoreBonus( nullable:true )
		 personImageURL( nullable:true )
		 keyWords( blank:true, nullable:true )
		 tdsNote( blank:true, nullable:true )
		 tdsLink( blank:true, nullable:true )
		 staffType( blank:false, nullable:false, inList:['Contractor', 'Hourly', 'Salary'])
		 travelOK( nullable:true )

	 }

	/*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {
		version false
		autoTimestamp false
		tablePerHierarchy false
		id column:'person_id'
		columns {
			firstName sqlType:'varchar(34)'
			middleName sqlType:'varchar(20)'
			lastName sqlType:'varchar(34)'
			nickName sqlType:'varchar(34)'
			title sqlType:'varchar(34)'
			active sqlType:'char(1)'
            travelOK sqlType: 'tinyint'
		}
	}

	static transients = [
		'assignedProjects',
		'assignedTeams',
		'company',
		'enabled',
		'lastNameFirst',
		'lastNameFirstAndTitle',
		'loginUser',
		'name',
		'suitableTeams',
		'teamsAssignedTo',
		'teamsCanParticipateIn'
	]

	/**
	 * This method was incorrectly implemented but not sure where it may be used so an exception has been
	 * added so that we may catch any possible location in testing. At some point we can blow away this method.
	 * JPM 2/26/2016
	 */
	String name() {
		throw new RuntimeException("Person.name method should not be used")
	}

	/**
	 * This method returns the company for this person.
	 */
	Party getCompany(){
		return partyRelationshipService.getCompanyOfStaff(this)
	}

	/**
	 * Used to get the projects that the person is assigned to
	 */
	List<Project> getAssignedProjects() {
		String query = "SELECT p.partyIdFrom.id \
			FROM PartyRelationship p \
			WHERE p.partyRelationshipType = 'PROJ_STAFF' AND \
			p.partyIdTo = :person AND \
			p.roleTypeCodeFrom = 'PROJECT' AND \
			p.roleTypeCodeTo = 'STAFF' ) \
			ORDER BY name"
		List projects = Project.executeQuery(query, [person:this])

		return projects
	}

	/**
	 * Used to get the teams that a person is assigned to for a given project
	 * @param project - the project to search for teams
	 * @return a list of the RoleType records that represent the teams that a person belongs to a project
	 */
	List<RoleType> getTeamsCanParticipateIn() {
		String query = """SELECT roleTypeCodeTo
			FROM PartyRelationship p
			WHERE p.partyRelationshipType = 'STAFF' AND
				p.roleTypeCodeFrom = 'COMPANY' AND
				p.partyIdFrom = :company AND
				p.partyIdTo = :person AND
				p.roleTypeCodeTo.type = :team )
			ORDER BY p.description"""
		List teams = RoleType.executeQuery(query, [company:this.company, person:this, team:RoleType.TEAM])
		return teams
	}

	/**
	 * Used to get the teams that a person is assigned to for a given project
	 * @param project - the project to search for teams
	 * @return a list of the RoleType records that represent the teams that a person belongs to a project
	 */
	List<RoleType> getTeamsAssignedTo(Project project) {
		String query = """SELECT roleTypeCodeTo
			FROM PartyRelationship p
			WHERE p.partyRelationshipType = 'PROJ_STAFF' AND
				p.roleTypeCodeFrom = 'PROJECT' AND
				p.partyIdFrom = :project AND
				p.partyIdTo = :person AND
				p.roleTypeCodeTo.type = :team )
			ORDER BY p.description"""
		List teams = RoleType.executeQuery(query, [project:project, person:this, team:RoleType.TEAM])
		return teams
	}

	/**
	 * Used to retrieve the teams that a person has been indicated as being suitable to participate on
	 * @return An array of the RoleType representing the teams that the person is associated with
	 */
	List<RoleType> getSuitableTeams() {
		String query = "SELECT p.roleTypeCodeTo \
			FROM PartyRelationship p \
			WHERE p.partyRelationshipType = 'STAFF' AND \
			p.roleTypeCodeFrom = 'COMPANY' AND \
			p.partyIdFrom = :company AND \
			p.partyIdTo = :person AND \
			p.roleTypeCodeTo <> 'STAFF' ) \
			ORDER BY description"
		List teams = RoleType.executeQuery(query, [company:this.company, person:this])
		return teams
	}

	/**
	 * Used to determine if the Person is enabled
	 * @return true if active otherwise false
	 */
	boolean isEnabled() {
		return active == 'Y'
	}

	/**
	 * Used to disable an person
	 */
	void disable() {
		active = 'N'
	}

	/**
	 * Used to retrieve the UserLogin for a given person
	 */
	UserLogin getUserLogin() {
		return UserLogin.findByPerson( this )
	}

	/**
	 * This method is used to get person's name in 'LastName, FirstName MiddleName' format
	 * @return person name in 'LastName, FirstName MiddleName' format
	 */
	String getLastNameFirst(){
		return ( lastName ? "${lastName}, ": '' ) + firstName + (  middleName ? " $middleName" : '' )
	}

	/**
	 * This method is used to get person's name in 'LastName, FirstName MiddleName - Title' format
	 * @return person name in 'LastName, FirstName MiddleName - Title' format
	 */
	String getLastNameFirstAndTitle(){
		return lastNameFirst+ ( title ? " - $title" : '' )
	}

	/**
	 * Retrieve special Person required by the System.
	 * Used to retrieve the Person object that represent the person that completes automated tasks
	 * @return
	 */
	Person getAutomaticPerson() {
		throw new RuntimeException('Person.getAutomaticPerson() deprecated - use TaskService.getAutomaticPerson()')
	}

	/**
	 * Check if the Person instance is deleteable
	 * i.e. AutomaticPerson shouldn't be deleted
	 * @return
	 */
	boolean isSystemUser(){
		if(SYSTEM_USER_AT.firstName == firstName && SYSTEM_USER_AT.lastName == lastName){
			return true
		}else{
			return false
		}
	}

	String toString(){
		firstName + ( middleName ? " $middleName" : '' ) + ( lastName ? " $lastName" : '' )
	}

	def beforeValidate() {
		if (middleName == null) {
			middleName = ""
		}
		if (lastName == null) {
			lastName = ""
		}
	}

	transient beforeDelete = {
		if(isSystemUser()){
			def msg = "${this}: is a System User and can't be Deleted"
			log.warn(msg)
			throw new UnsupportedOperationException(msg)
		}
	}
}
