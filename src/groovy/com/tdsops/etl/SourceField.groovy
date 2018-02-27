package com.tdsops.etl

/**
 * Wrapper over a Dataset column value.
 * It is used based on the following syntax:
 * <pre>
 * 		find Application for assetId by id with SOURCE.'assetId'
 * <pre>
 */
class SourceField {

	@Delegate String value

	SourceField(Object value) {
		this.value = value
	}

}
