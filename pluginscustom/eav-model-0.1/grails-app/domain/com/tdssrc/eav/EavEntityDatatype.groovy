package com.tdssrc.eav

/**
 * The base class for all EAV attribute datatypes.  This should not be extended directly but
 * instead use an EAV attribute datatype which extends this class.
 *
 * It was the intent that this be an abstract class but because of the behavior of
 * GORM/Hibernate the class must be concrete in order for the 1-to-many relationship
 * with the EavAttribute table.
 */
class EavEntityDatatype {

	static belongsTo = [attribute: EavAttribute]

	static mapping = {
		version false
		tablePerHierarchy false
		id column: 'value_id'
	}
}
