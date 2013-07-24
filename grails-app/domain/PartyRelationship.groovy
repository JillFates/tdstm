/**
 * PartyRelationship is used to relate two parties into a relationship with roles.
 */
class PartyRelationship implements Serializable {
	PartyRelationshipType partyRelationshipType
	Party partyIdFrom
	Party partyIdTo
	RoleType roleTypeCodeFrom
	RoleType roleTypeCodeTo
	String statusCode="ENABLED"
	String comment

	static constraints = {
		partyRelationshipType( nullable:false )
		partyIdFrom( nullable:false )
		roleTypeCodeFrom( nullable:false )
		partyIdTo( nullable:false )
		roleTypeCodeTo( nullable:false )
		statusCode( nullable:false, inList:['ENABLED', 'DISABLED'] )
		comment (blank:true,nullable:true)
	}
	
	static mapping  = {	
		version false
		id composite:['partyRelationshipType', 'partyIdFrom', 'partyIdTo', 'roleTypeCodeFrom', 'roleTypeCodeTo'], generator:'assigned', unique:true
		partyIdFrom ignoreNotFound: true
		partyIdTo ignoreNotFound: true
		columns {
			roleTypeCodeFrom sqlType:'varchar(20)'
			roleTypeCodeTo sqlType:'varchar(20)'
			statusCode sqlType:'varchar(20)'
		}
		
	}
	
	/*
	 * to get moveEventStaff object
	 * @partyIdTo : instance of person for which need to get instance
	 * @partyIdFrom : instance of  Project for which need to get instance
	 * @roleTypeCodeTo : instance of Role for which need to get instance
	 * @roleTypeCodeFrom : instance of Role for which need to get instance
	 * @partyRelationshipType : instance of partyRelationshipType for which need to get instance
	 * @return : PartyRelationship  instance
	 */
	
	static def getRelationshipInstance(partyIdTo, partyIdFrom, roleTypeCodeTo, roleTypeCodeFrom, partyRelationshipType){
		def result = PartyRelationship.createCriteria().get {
			and {
				 eq('partyIdTo', partyIdTo )
				 eq('partyIdFrom', partyIdFrom )
				 eq('roleTypeCodeTo', roleTypeCodeTo )
				 eq('roleTypeCodeFrom', roleTypeCodeFrom )
				 eq('partyRelationshipType', partyRelationshipType )
			}
		}
		return result
	}
	
}
