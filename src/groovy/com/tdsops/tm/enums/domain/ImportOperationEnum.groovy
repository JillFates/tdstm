package com.tdsops.tm.enums.domain
import groovy.transform.CompileStatic

/**
 * Indicates the type of operation that the ImportBatchRecord will perform when posting to the database
 */
@CompileStatic
enum ImportOperationEnum {

	INSERT('Insert'),
	UPDATE('Update'),
	DELETE('Delete'),
	UNDETERMINED('Undetermined')

	private String label

	private ImportOperationEnum(String value) {
		label = value
	}

    String getKey() {
        return name()
    }

    String toString() {
        return label
    }

	/**
	 * Safely return the corresponding Enum constant
	 * @param value
	 * @return
	 */
	static ImportOperationEnum lookup(String value) {
		return ImportOperationEnum.enumConstantDirectory().get(value)
	}
}