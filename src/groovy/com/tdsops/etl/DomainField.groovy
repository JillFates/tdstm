package com.tdsops.etl
/**
 * Wrapper over a data set field valoe
 * to be used in an find ETL command
 */
class DomainField {

	Object value

	DomainField(Object value) {
		this.value = value
	}
}
