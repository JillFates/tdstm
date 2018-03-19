package net.transitionmanager.command

/**
 * This class represents the structure of the data structure that will be submitted by the frontend
 * to the controller for updating the fieldsInfo section of the ImportBatchRecord. This only deals with the current
 * value property within the data structure
 */
class ImportBatchRecordUpdateCommand implements CommandObject {

	List<ImportBatchRecordFieldUpdateCommand> fieldsInfo

}

/**
 * This class represents the individual field within the fieldsInfo which is essentially just a name/value pair
 */
class ImportBatchRecordFieldUpdateCommand implements CommandObject {

	// Field Name
	String fieldName

	// Value for the field
	Object value

	static constraints = {
		fieldName nullable: false, blank: false
		value nullable: false, blank: true
	}

}
