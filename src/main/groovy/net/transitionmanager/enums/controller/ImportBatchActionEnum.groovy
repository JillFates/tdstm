package net.transitionmanager.enums.controller

import groovy.transform.CompileStatic

/**
 * Represents the various actions that can be invoked against an ImportBatch
 */
@CompileStatic
enum ImportBatchActionEnum {
	ARCHIVE,
	UNARCHIVE,
	QUEUE,
	EJECT,		// Remove from Queue
	STOP

	/**
	 * Safely return the corresponding Enum constant
	 * @param value
	 * @return the enum matching the constant name
	 */
	static ImportBatchActionEnum lookup(String value) {
		// return ImportBatchActionEnum.enumConstantDirectory().get(value)
		return ImportBatchActionEnum.valueOf(value)
	}

}
