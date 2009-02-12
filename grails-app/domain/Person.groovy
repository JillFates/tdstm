class Person extends Party{
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 party unique:true
			 id column:'PERSON_ID'
				 personCreatedDate column:'CREATED_DATE'
					 personLastUpdated column:'LAST_UPDATED'
	}
	 /*
	  * list of fields
	  */
	String firstName
	String lastName
	String nickName
	Date personCreatedDate
	Date personLastUpdated
	String active
	String toString(){
		   return("$firstName")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 firstName(blank:false,nullable:false,maxLength:64)
		 active(blank:false,nullable:false)
		 personCreatedDate(blank:false,nullable:false)
		 lastName(blank:true,nullable:true,maxLength:64)
		 nickName(blank:true,nullable:true,maxLength:64)
		 personLastUpdated(blank:true,nullable:true)
	 }
}
