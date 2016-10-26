package net.transitionmanager.domain

import groovy.transform.EqualsAndHashCode

/**
 * Relates two parties into a relationship with roles.
 */
@EqualsAndHashCode(includes=['partyRelationshipType', 'partyIdFrom', 'partyIdTo', 'roleTypeCodeFrom', 'roleTypeCodeTo'])
class PartyRelationship implements Serializable {

	PartyRelationshipType partyRelationshipType
	Party partyIdFrom
	Party partyIdTo
	RoleType roleTypeCodeFrom
	RoleType roleTypeCodeTo
	String statusCode = 'ENABLED'
	String comment

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

	String toString() {
		"$partyRelationshipType : $roleTypeCodeFrom $partyIdFromId : $roleTypeCodeTo $partyIdToId"
	}
}
