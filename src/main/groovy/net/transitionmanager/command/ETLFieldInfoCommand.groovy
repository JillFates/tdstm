package net.transitionmanager.command



/**
 * General purpose command object for updating an individual field
 */

class ETLFieldInfoCommand implements CommandObject{

	// Field Name
	String fieldName

	// Value for the field
	Object value

	static constraints = {
		fieldName nullable: false, blank: false
		value nullable: false
	}
}
