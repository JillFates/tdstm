package com.tdsops.etl

class DomainFacade {

	ETLProcessorResult result

	DomainFacade(ETLProcessorResult result) {
		this.result = result
	}

	Object getProperty(String name) {

		Map<String,?> currentDataFields = result.currentData().fields

		if(!currentDataFields.containsKey(name)) {
			throw ETLProcessorException.unknownDomainProperty(name)
		}
		return new DomainField(currentDataFields[name].value)
	}

}
