class PartyRelationshipType {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 id column: 'PARTY_RELATIONSHIP_TYPE_ID'
				 partyRelationshipTypeCode column: 'CODE'
	}
	 /*
	  * list of fields
	  */
	String partyRelationshipTypeCode
	String description
	String toString(){
		   return("$partyRelationshipTypeCode")
	}
	/*
	 * Field Validations
	 */
	 static constraints = {
		 partyRelationshipTypeCode(blank:false,nullable:false)
		 description(blank:true,nullable:true)
	 }
}
