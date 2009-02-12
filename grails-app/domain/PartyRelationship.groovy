class PartyRelationship {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 id column: 'PARTY_RELATIONSHIP_ID'
			 	partyIdFrom column: 'PARTY_ID_FROM'
			 		partyIdTo column: 'PARTY_ID_TO'
			 			roleTypeCodeFrom column: 'ROLE_TYPE_CODE_FROM'
			 				roleTypeCodeTo column: 'ROLE_TYPE_CODE_TO'
			 					partyRelationshipType column: 'PARTY_RELATIONSHIP_TYPE_ID'
	}
	 /*
	  * list of fields
	  */
	Party partyIdFrom
	Party partyIdTo
	RoleType roleTypeCodeFrom
	RoleType roleTypeCodeTo
	PartyRelationshipType partyRelationshipType
	String statusCode
	String comments
	String toString(){
		   return("$partyRelationshipTypeCode")
	}
	/*
	 * Field Validations
	 */
	 static constraints = {
		 partyRelationshipType(blank:false,nullable:false)
		 partyIdFrom(blank:false,nullable:false)
		 partyIdTo(blank:false,nullable:false)
		 roleTypeCodeFrom(blank:false,nullable:false)
		 roleTypeCodeTo(blank:false,nullable:false)
		 statusCode(blank:false,nullable:false)
		 comments(blank:true,nullable:true)
	 }
}
