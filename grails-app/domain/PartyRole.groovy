class PartyRole {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {
			 version false
			 id column:'PARTY_ROLE_ID'
			role column:'ROLE_TYPE_ID'
				party column:'PARTY_ID'
	}
	 /*
	  * list of fields
	  */
	  Role role
	  Party party
	  
	/*
	 * Fields Validations
	 */
	 static constraints = {
			 role(blank:false,nullable:false)
		 party(blank:false,nullable:false)
	 }
}
