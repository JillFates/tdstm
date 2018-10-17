package com.tdsops.etl

/**
 * Helper class used to determined if a particular value, within the script,
 * is a String value, a DoaminField value, Element value or a SourceField value.
 * Takes the string value of value instance. It's used in different ETL commands
 * For example, using initialize command:
 * <pre>
 *      initialize environment with 'Production'
 *	    initialize environment with Production
 *	    initialize environment with SOURCE.'application id'
 *	    initialize environment with DOMAIN.id
 * </pre>
 */
class ETLValueHelper {

	/**
	 * Defines a value from a different types of object types
	 * <pre>
	 *		// results values
	 *		DOMAIN.'application id'
	 *		// Or dataset values
	 *		SOURCE.assetName
	 *		// Or a bound element
	 *		extract AssetType set primaryType
	 * <pre>
	 * DomainField#value used as a wrapper over a domain result value
	 * SourceField#value used as a wrapper over Dataset source field value
	 * Element#value a dynamic variable bound in the ETLBinding context
	 * @param value a instance to be used in value calculation
	 * @return the value depending on instance type
	 */
	static Object valueOf(def value) {

		Object fieldValue

		switch(value){
			case DomainField: //DOMAIN.name // Label name or property name from fieldSpecs
				fieldValue = ((DomainField)value).value
				break
			case Element:      // LocalVariable
				fieldValue = ((Element)value).value
				break
			case DomainFacade: // set myVar with DOMAIN.
				fieldValue = ((DomainFacade)value).currentRowMap()
				break
			case SourceField:
				fieldValue = ((SourceField)value).value // SOURCE.'application id'
				break
			case FindingsFacade:
				fieldValue = ((FindingsFacade)value).result() // FINDINGS.result()
				break
			case NOW:
				fieldValue = String.valueOf(value)
				break
			default:
				fieldValue = value
				break
		}

		return fieldValue
	}
}
