package com.tdsops.etl


class FoundElement {

	String dependentId
	/**
	 * Defines if the found element instance
	 * is 'update' or 'create'
	 */
	private String action
	private Map<String, ?> propertiesMap

	FoundElement(String dependentId) {
		this.dependentId = dependentId
		this.action = 'unknown'
		propertiesMap = [:]
	}

	/**
	 * WhenFound create ETL command. It defines what should based on find command results
	 * <pre>
	 *		whenFound asset create {
	 *			assetClass Application
	 *			assetName primaryName
	 *			assetType primaryType
	 *			"SN Last Seen": NOW
	 *		}
	 * </pre>
	 * @param dependentId
	 * @return the current find Element
	 */
	FoundElement create(Closure closure) {
		return action('create', closure)
	}
	/**
	 * WhenNotFound update ETL command. It defines what should based on find command results
	 * <pre>
	 *		whenFound asset update {
	 *			"TM Last Seen" NOW
	 *		}
	 * </pre>
	 * @param dependentId
	 * @return the current find Element
	 */
	FoundElement update(Closure closure) {
		return action('update', closure)
	}

	/**
	 * Creates an action { 'create', 'update' } in the current FoundElement
	 * @param action
	 * @param closure
	 * @return
	 */
	private FoundElement action(String action, Closure closure) {
		this.action = action
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = this
		closure()
		return this
	}

	/**
	 * Overriding methodMissing this class collects
	 * method names as properties keys
	 * and args as a value
	 * @see FoundElement#propertiesMap
	 * @param name missing method name
	 * @param args an array with method arguments
	 * @return the current instance of FoundElement
	 */
	def methodMissing(String name, def args) {
		if(args){
			propertiesMap[name] = calculateValue(args)
		}
		this
	}

	private def calculateValue(def args) {

		if(args.size() == 1) {
			return calculateFieldValue(args[0])
		} else {
			return args.collect{ calculateFieldValue(it) }
		}
	}
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
	 * Element#value a dynamic varibale bound in the ETLBinding context
	 * @param value a instance to be used in value calculation
	 * @return the value depending on instance type
	 */
	private def calculateFieldValue(def value){

		def fieldValue

		switch (value) {
			case DomainField:     //DOMAIN.name // Label name or property name from fieldSpecs
				fieldValue = ((DomainField) value).value
				break
			case Element:            // LocalVariable
				fieldValue = ((Element) value).value
				break
			case SourceField:
				fieldValue = ((SourceField) value).value // SOURCE.'application id'
				break
			default:
				fieldValue = value
				break
		}

		return fieldValue

	}
}
