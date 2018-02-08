package com.tdsops.tm.enums.domain

enum ImportOperationEnum {

	INSERT('Insert'),
	UPDATE('Update'),
	DELETE('Delete'),
	UNDETERMINED('Undetermined')

	private String name

	private ImportOperationEnum(String value) {
		name = value
	}
}