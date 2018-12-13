package com.tdsops.etl

/**
 * Abstract implementation for whenFound and whenNotFound ETL command.
 * It manages common behaviour between both commands.
 * <pre>
 *  whenFound DOMAIN_PROPERTY_NAME update {* </pre>
 * The DOMAIN_PROPERTY_NAME can be createdBy, manufacturer, model, rack, bundle, etc.
 * @see WhenNotFoundElement* @see WhenFoundElement
 */
abstract class FoundElement implements ETLCommand {

	/**
	 * ETLProcessor instance used for fields validations
	 */
	ETLProcessor processor

	/**
	 * ETLDomain instance used for validate every field added using whenFound and whenNotFound commands
	 */
	ETLDomain domain

	/**
	 * Field definition used by whenFound and whenNotFound
	 * to define the field that will be used in the creation or update of an doamin class
	 * <pre>
	 *     find Dependency ....
	 *     whenFound 'asset' update { .... }
	 * </pre>
	 * Following the example above, 'asset' field belongs to a Dependency domain class
	 */
	ETLFieldDefinition fieldDefinition

	/**
	 * Found Element enum type used to define if whenFound and whenNotFound command
	 * <pre>
	 * 		whenFound 'asset' update { .... }
	 *  	whenNotFound 'asset' create { .... }
	 * </pre>
	 */
	enum FoundElementType {
		update, create, unknown
	}

	/**
	 * Defines if the found element instance
	 * is 'update' or 'create'
	 */
	FoundElementType action

	/**
	 * Map that collects properties to be added in the ETLProcessorResult
	 */
	private Map<String, Object> propertiesMap

	/**
	 * Result add the result of this ETL command invocation
	 * @see ETLProcessor#addFoundElement(com.tdsops.etl.FoundElement)
	 */
	private ETLProcessor processor

	FoundElement(String domainPropertyName, ETLDomain domain, ETLProcessor processor) {
		this.fieldDefinition = processor.lookUpFieldDefinitionForCurrentDomain(domainPropertyName)
		this.domain = domain
		this.processor = processor
		this.action = FoundElementType.unknown
		this.propertiesMap = [:]
	}

	/**
	 * Validates WhenNotFound create ETL command used incorrectly
	 * <pre>
	 *  // Invalid use of whenNotFound  command
	 * 	whenNotFound asset update {
	 * 			......
	 *	}
	 * </pre>
	 * @param closure
	 * @return the current find Element
	 */
	FoundElement create(Closure closure) {
		throw ETLProcessorException.invalidWhenFoundCommand(fieldDefinition.name)
	}

	/**
	 * Validates WhenFound create ETL command used incorrectly
	 * <pre>
	 *  // Invalid use of WhenFound  command
	 * 	whenFound asset create {
	 * 		....
	 *	}
	 *	</pre>
	 * @param closure
	 * @return the current find Element
	 */
	FoundElement update(Closure closure) {
		throw ETLProcessorException.invalidWhenNotFoundCommand(fieldDefinition.name)
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
		processor.addFoundElement(this)
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
	FoundElement assetClass(ETLDomain domain) {
		this.propertiesMap.assetClass = domain.name()
		this
	}

	/**
	 * Overriding methodMissing this class collects
	 * method names as properties keys and args as a value
	 *
	 * @see FoundElement#propertiesMap
	 * @param name missing method name
	 * @param args an array with method arguments
	 * @return the current instance of FoundElement
	 */
	def methodMissing(String name, def args) {
		if (args) {
			propertiesMap[fieldDefinitionNameFor(name)] = calculateValue(args)
		} else {
			throw ETLProcessorException.incorrectFoundUseWithoutArgValue(name)
		}
		this
	}

	/**
	 * Validate if fieldName parameter belongs to the assetClass parameter.
	 * If there is defined asset class,
	 * If not it throws an Exception.
	 * @param fieldName
	 * @return
	 * @see FoundElement#assetClass
	 */
	String fieldDefinitionNameFor(String fieldName) {
		if (!domain) {
			throw ETLProcessorException.incorrectFoundUseWithoutAssetClass()
		}
		ETLFieldDefinition fieldDefinition = processor.lookUpFieldDefinition(domain, fieldName)
		return fieldDefinition.name
	}

	String getActionName() {
		return action.name()
	}

	FoundElementType getAction() {
		return action
	}

	Map<String, Object> getPropertiesMap() {
		return propertiesMap
	}
}
