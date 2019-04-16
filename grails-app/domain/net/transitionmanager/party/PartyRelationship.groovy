package net.transitionmanager.party

import groovy.transform.EqualsAndHashCode
import net.transitionmanager.security.RoleType

/**
 * Relates two parties into a relationship with roles.
 */
@EqualsAndHashCode(includes=['partyRelationshipType', 'partyIdFrom', 'partyIdTo', 'roleTypeCodeFrom', 'roleTypeCodeTo'])
class PartyRelationship implements Serializable {

	PartyRelationshipType partyRelationshipType
	Party                 partyIdFrom
	Party                 partyIdTo
	RoleType              roleTypeCodeFrom
	RoleType              roleTypeCodeTo
	String                statusCode = 'ENABLED'
	String                comment

	static constraints = {
		comment nullable: true
		statusCode inList: ['ENABLED', 'DISABLED']
	}

	static mapping = {
		version false
		id composite: ['partyRelationshipType', 'partyIdFrom', 'partyIdTo', 'roleTypeCodeFrom', 'roleTypeCodeTo'],
		   generator: 'assigned', unique: true
		partyIdFrom ignoreNotFound: true
		partyIdTo ignoreNotFound: true
		columns {
			roleTypeCodeFrom sqlType: 'varchar(20)'
			roleTypeCodeTo sqlType: 'varchar(20)'
			statusCode sqlType: 'varchar(20)'
		}
	}

	/**
	 * Used to get retrieve a single PartyRelationship object
	 * @param partyIdTo : instance of person for which need to get instance
	 * @param partyIdFrom : instance of  Project for which need to get instance
	 * @param roleTypeCodeTo : instance of Role for which need to get instance
	 * @param roleTypeCodeFrom : instance of Role for which need to get instance
	 * @param partyRelationshipType : instance of partyRelationshipType for which need to get instance
	 * @return the PartyRelationship object if found otherwise null
	 */

	static PartyRelationship getRelationshipInstance(
		Party partyIdTo,
		Party partyIdFrom,
		RoleType roleTypeCodeTo,
		RoleType roleTypeCodeFrom,
		PartyRelationshipType partyRelationshipType
	) {
		PartyRelationship result = PartyRelationship.createCriteria().get {
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

	String toString() {
		"$partyRelationshipType : ${roleTypeCodeFrom.id}/${partyIdFrom.id} : ${roleTypeCodeTo.id}/${partyIdTo.id}"
	}
}
