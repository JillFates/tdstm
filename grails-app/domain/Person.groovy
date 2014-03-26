class Person extends Party {

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
	String staffType
	Integer travelOK = 1
	
	static Person loggedInPerson

	static hasMany =[
		blackOutDates : ExceptionDates
	]
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 firstName( blank:false, maxLength:34 )
		 middleName( blank:true, maxLength:20 )
		 lastName( blank:true, maxLength:34 )
		 title( blank:true, nullable:true, maxLength:34 )
		 nickName( blank:true, nullable:true, maxLength:34 )
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
	
	/**
	 * Get's the person's roles for a specified company
	 * @param Integer - company id
	 * @return Array of Staff Functions
	 */
	def getPersonRoles(companyId){
		partyRelationshipService.getCompanyStaffFunctions(companyId, this.id)
	}
	
	/**
	 * This method is used to get person's name in 'LastName, FirstName MiddleName' format 
	 * @return person name in 'LastName, FirstName MiddleName' format
	 */
	def getLastNameFirst(){
		return ( lastName ? "${lastName}, ": '' ) + firstName + (  middleName ? " $middleName" : '' )
	}
	
	/**
	 * This method is used to get person's name in 'LastName, FirstName MiddleName - Title' format
	 * @return person name in 'LastName, FirstName MiddleName - Title' format
	 */
	def getLastNameFirstAndTitle(){
		return lastNameFirst+ ( title ? " - $title" : '' )
	}
	
	String toString(){
		firstName + ( middleName ? " $middleName" : '' ) + ( lastName ? " $lastName" : '' )
	}
	
	
}
