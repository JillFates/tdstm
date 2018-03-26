package com.tdsops.etl

/**
 * Abstract implementation for whenFound and whenNotFound ETL command.
 * It manages common behaviour between both commands.
 * <pre>
 *  whenFound DOMAIN_PROPERTY_NAME update {
 * </pre>
 * The DOMAIN_PROPERTY_NAME can be createdBy, manufacturer, model, rack, bundle, etc.
 * @see WhenNotFoundElement
 * @see WhenFoundElement
 */
abstract class FoundElement {

	String domainPropertyName

	enum FoundElementType {
		update, create, unknown
	}

	/**
	 * Defines if the found element instance
	 * is 'update' or 'create'
	 */
	private FoundElementType action

	/**
	 * Map that collects properties to be added in the ETLProcessorResult
	 * @see FoundElement#result
	 */
	private Map<String, ?> propertiesMap

	/**
	 * Result add the result of this ETL command invocation
	 * @see ETLProcessorResult#addFoundElement(com.tdsops.etl.FoundElement)
	 */
	private ETLProcessorResult result

	FoundElement(String domainPropertyName, ETLProcessorResult result) {
		this.domainPropertyName = domainPropertyName
		this.result = result
		this.action = FoundElementType.unknown
		this.propertiesMap = [:]
	}

	/**
	 * Validates WhenNotFound create ETL command used incorrectly
	 * <pre>
	 *     // Invalid use of whenNotFound  command
	 *		whenNotFound asset update {
	 *			......
	 *		}
	 * </pre>
	 * @param closure
	 * @return the current find Element
	 */
	FoundElement create(Closure closure) {
		throw ETLProcessorException.invalidWhenFoundCommand(domainPropertyName)
	}

	/**
	 * Validates WhenFound create ETL command used incorrectly
	 * <pre>
	 *     // Invalid use of WhenFound  command
	 *		whenFound asset create {
	 *			....
	 *		}
	 * </pre>
	 * @param closure
	 * @return the current find Element
	 */
	FoundElement update(Closure closure) {
		throw ETLProcessorException.invalidWhenNotFoundCommand(domainPropertyName)
	}

	/**
	 * Creates an action { 'create', 'update' } in the current FoundElement
	 * @param action
	 * @param closure
	 * @return
	 */
	private FoundElement action(FoundElementType action, Closure closure) {
		this.action = action
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = this
		closure()
		result.addFoundElement(this)
		return this
	}

	/**
	 * Defines an assetClass for the current Found element.
	 * If the assetClass parameters is not a ETLDomain it throws an ETLProcessorException
	 * @param assetClass a String with a domain class
	 * @return the current found element
	 * @trows ETLProcessorException if assetClass parameter is not an ETLDomain
	 * @see ETLDomain
	 */
	FoundElement assetClass(String assetClass){
		ETLDomain domain = ETLDomain.lookup(assetClass)
		if(!domain){
			throw ETLProcessorException.invalidDomain(assetClass)
		}
		this.propertiesMap.assetClass = domain.name()
		this
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
			return ETLValueHelper.valueOf(args[0])
		} else {
			return args.collect{ ETLValueHelper.valueOf(it) }
		}
	}

	String getDomainPropertyName() {
		return domainPropertyName
	}

	String getActionName() {
		return action.name()
	}

	ETLProcessorResult getResult() {
		return result
	}

	Map<String, ?> getPropertiesMap() {
		return propertiesMap
	}
}
