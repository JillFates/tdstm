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
		 firstName( blank:false, nullable:false, maxLength:34 )
		 lastName( blank:true, nullable:true, maxLength:34 )
		 title( blank:true, nullable:true, maxLength:34 )
		 nickName( blank:true, nullable:true, maxLength:34 )
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
