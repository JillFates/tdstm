package com.tdsops.etl

class LookupElement {

	ETLProcessor processor
	List<String> propertyNames

	LookupElement(ETLProcessor processor, List<String> propertyNames) {
		this.processor = processor
		this.propertyNames = propertyNames.collect{ String propertyName ->
			processor.lookUpFieldDefinitionForCurrentDomain(propertyName)?.name
		}
	}

	LookupElement with(Object...values){

		List valuesAsList = values as List
		boolean found = processor.result.lookupInReference(propertyNames, valuesAsList)
		if (found) {
			processor.bindVariable(processor.DOMAIN_VARNAME, new DomainFacade(processor.result))
		}
		processor.addLocalVariableInBinding(processor.LOOKUP_VARNAME, new LookupFacade(found))

		return this
	}


}
