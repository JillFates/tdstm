package net.transitionmanager.enums.controller

import groovy.transform.CompileStatic

/**
 * Represents the various actions that can be invoked against an ImportBatchRecord
 */
@CompileStatic
enum ImportRecordActionEnum {

	IGNORE,
	INCLUDE,
	PROCESS

	/**
	 * Safely return the corresponding Enum constant
	 * @param value
	 * @return the enum matching the constant name
	 */
	static ImportRecordActionEnum lookup(String value) {
		// return ImportRecordActionEnum.enumConstantDirectory().get(value)
		return ImportRecordActionEnum.valueOf(value)
	}

}
