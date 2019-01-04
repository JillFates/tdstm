package com.tdsops.tm.enums.domain
import groovy.transform.CompileStatic

/**
 * Represents the various statuses that an import batch can be in at any time
 */
@CompileStatic
enum ImportBatchRecordStatusEnum {

	COMPLETED('Completed'),
	IGNORED('Ignored'),
	PENDING('Pending'),

	final String label

	private ImportBatchRecordStatusEnum(String label) {
		this.label = label
	}

    String getKey() {
        return name()
    }

    String toString() {
        return label
    }

	/**
	 * Safely return the corresponding Enum constant.
	 * @param value
	 * @return
	 */
	static ImportBatchRecordStatusEnum lookup(String value) {
		// return ImportBatchRecordStatusEnum.enumConstantDirectory().get(value)
		ImportBatchRecordStatusEnum.valueOf(value)
	}
}
