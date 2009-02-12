class Party {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 id column:'PARTY_ID'
				 partyCreatedDate column:'PARTY_CREATED_DATE'
	}
	 /*
	  * list of fields
	  */
	String partyName
	Date partyCreatedDate
	String toString(){
		   return("$partyName")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 partyName(blank:false,nullable:false)
		 partyCreatedDate(blank:false,nullable:false)
	 }
}
