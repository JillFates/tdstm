package com.tdsops.tm.enums.domain
import groovy.transform.CompileStatic

/**
 * Represents the various statuses that an import batch can be in at any time
 */
@CompileStatic
enum ImportBatchStatusEnum {

	COMPLETED('Completed'),
	IGNORED('Ignored'),
	PENDING('Pending'),
	QUEUED('Queued'),
	RUNNING('Running')

	final String label

	private ImportBatchStatusEnum(String label) {
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
	static ImportBatchStatusEnum lookup(String value) {
		return ImportBatchStatusEnum.enumConstantDirectory().get(value)
	}
}
