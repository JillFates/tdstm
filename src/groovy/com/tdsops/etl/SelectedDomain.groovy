package com.tdsops.etl

import com.tdsops.etl.ETLProcessor.ReservedWord

/**
 * Defines a selected ETL domain behaviour.
 * <pre>
 *     domain Application
 *     domain Application as newest
 * </pre>
 */
class SelectedDomain {

	ETLDomain domain
	boolean isNew

	SelectedDomain(ETLDomain selected){
		this.domain = selected
		isNew = false
	}

	SelectedDomain using( ReservedWord  reservedWord) {
		if(reservedWord == ReservedWord.newer){
			this.isNew = true
			return this
		}
		throw ETLProcessorException.invalidDomainComand()
	}

}
