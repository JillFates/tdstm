class Person {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 party unique:true
			 id column:'PERSON_ID'
			 party column:'PARTY_ID'
	}
	 /*
	  * list of fields
	  */
	String firstName
	String lastName
	// Party object reference
	Party party
	String active
	String toString(){
		   return("$firstName")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 firstName(blank:false,nullable:false,maxLength:64)
		 lastName(blank:false,nullable:false,maxLength:64)
		 party(blank:false,nullable:false,unique:true)
		 active(blank:true,nullable:true)
	 }
}
