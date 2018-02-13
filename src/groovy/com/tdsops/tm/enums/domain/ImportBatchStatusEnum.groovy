package com.tdsops.tm.enums.domain

enum ImportBatchStatusEnum {

	COMPLETED('Completed'),
	IGNORED('Ignored'),
	PENDING('Pending'),
	QUEUED('Queued'),
	RUNNING('Running')


	final String name

	private ImportBatchStatusEnum(String name) {
		this.name = name
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