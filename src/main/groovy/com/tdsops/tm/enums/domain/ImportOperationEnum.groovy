package com.tdsops.tm.enums.domain
import groovy.transform.CompileStatic

/**
 * Indicates the type of operation that the ImportBatchRecord will perform when posting to the database
 */
@CompileStatic
enum ImportOperationEnum {

	INSERT('Insert'),
	UNCHANGED('Unchanged'),
	UPDATE('Update'),
	DELETE('Delete'),
	TBD('TBD')

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
	 * Safely return the corresponding Enum constant using the String Value
	 * It returns the correct instance by matching the first letters of the name (case insensitive)
	 * i.e.
	 *    'I'  : Insert
	 *    'UP' : Update
	 *    'InS': Insert
	 * @param value
	 * @return
	 */
	static ImportOperationEnum lookup(String value) {
		ImportOperationEnum opValue

		if (value) {
			value = value.toUpperCase()
			opValue = ImportOperationEnum.values().find {
				it.name().startsWith(value)
			}
		}

		return (opValue ?: ImportOperationEnum.TBD)
	}
}
