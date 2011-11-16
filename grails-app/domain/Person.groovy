class Person extends Party {

	String firstName
	String lastName
	String nickName
	String active = "Y"
	String title
	String email
	String department
	String location
	String workPhone
	String mobilePhone
	Integer modelScore = 0
	Integer modelScoreBonus = 0


	/*
	 * Fields Validations
	 */
	 static constraints = {
		 firstName( blank:false, nullable:false, maxLength:34 )
		 lastName( blank:true, nullable:true, maxLength:34 )
		 title( blank:true, nullable:true, maxLength:34 )
		 nickName( blank:true, nullable:true, maxLength:34 )
		 active( blank:false, nullable:false, inList:['Y','N'] )
		 email(email:true, blank:true, nullable:true)
		 department( blank:true, nullable:true )
		 location( blank:true, nullable:true )
		 workPhone(blank:true, nullable:true, phoneNumber:true)
		 mobilePhone(blank:true, nullable:true, phoneNumber:true)
		 modelScore( blank:true, nullable:true )
		 modelScoreBonus( blank:true, nullable:true )
		 
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
			lastName sqlType:'varchar(34)'
			nickName sqlType:'varchar(34)'
			title sqlType:'varchar(34)'
			active sqlType:'char(1)'
		}
	}

	String toString(){
		"$firstName $lastName"
	}

}
