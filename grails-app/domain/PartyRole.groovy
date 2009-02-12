/**
 * PartyRole reflects the various roles that can be associated directly to a party.  Only disticted roles can be assigned.
 */
class PartyRole implements Serializable {
	Party party
	RoleType roleType

	static mapping  = {	
		version false
		id composite:['party', 'roleType'], generator:'assigned', unique:true
		columns {
			roleType sqlType:'varchar(20)'
		}
		
	}
	
}
