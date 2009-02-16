/**
 * PartyRelationship is used to relate two parties into a relationship with roles.
 */
class PartyRelationship implements Serializable {
	PartyRelationshipType partyRelationshipType
	Party partyIdFrom
	Party partyIdTo
	RoleType roleTypeCodeFrom
	RoleType roleTypeCodeTo
	String statusCode
	String comment

	static constraints = {
		partyRelationshipType( nullable:false )
		partyIdFrom( nullable:false )
		partyIdTo( nullable:false )
		roleTypeCodeFrom( nullable:false )
		roleTypeCodeTo( nullable:false )
		statusCode( nullable:false, inList:['ENABLED', 'DISABLED'] )
		comment (blank:true,nullable:true)
	}
	
	static mapping  = {	
		version false
		id composite:['partyRelationshipType', 'partyIdFrom', 'partyIdTo', 'roleTypeCodeFrom', 'roleTypeCodeTo'], generator:'assigned', unique:true
		columns {
			roleTypeCodeFrom sqlType:'varchar(20)'
			roleTypeCodeTo sqlType:'varchar(20)'
			statusCode sqlType:'varchar(20)'
		}
		
	}
	
}
