class Party {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 id column:'PARTY_ID'
			partyName column:'PARTY_NAME'
	}
	 /*
	  * list of fields
	  */
	String partyName
	String toString(){
		   return("$partyName")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 partyName(blank:false,nullable:false)
	 }
}
