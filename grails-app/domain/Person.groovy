class Person extends Party {

	String firstName
	String lastName
	String nickName
	String active
	String title

	/*
	 * Fields Validations
	 */
	 static constraints = {
		 firstName( blank:false, nullable:false, maxLength:30 )
		 lastName( blank:true, nullable:true, maxLength:30 )
		 title( blank:true, nullable:true, maxLength:50 )
		 nickName( blank:true, nullable:true, maxLength:30 )
		 active( blank:false, nullable:false, inList:['Y','N'] )
	 }

	/*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {	
		version false
		tablePerHierarchy false
		id column:'person_id'
		columns {
			firstName sqlType:'varchar(30)'
			lastName sqlType:'varchar(30)'
			nickName sqlType:'varchar(30)'
			title sqlType:'varchar(50)'
			active sqlType:'char(1)'
		}
	}

	String toString(){
		"$firstName $lastName"
	}

}
