package net.transitionmanager.party

import groovy.transform.EqualsAndHashCode
import net.transitionmanager.security.RoleType

/**
 * PartyRole reflects the various roles that can be associated directly to a party.  Only disticted roles can be assigned.
 */
@EqualsAndHashCode(includes = ['party', 'roleType'])
class PartyRole implements Serializable {

	static belongsTo = [party: Party, roleType: RoleType]

	static mapping = {
		version false
		id composite: ['party', 'roleType'], generator: 'assigned', unique: true
		columns {
			roleType sqlType: 'varchar(20)'
		}
	}

	String toString() {
		"$party : $roleType"
	}
}
