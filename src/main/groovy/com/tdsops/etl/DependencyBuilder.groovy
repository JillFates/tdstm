package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.grails.datastore.mapping.model.PersistentProperty

/**
 *
 * <code>
 * 	iterate {
 * 		domain Application
 * 		...
 * 		set assetResultVar with DOMAIN
 *
 * 	    domain Device
 * 	    ...
 * 	    set dependentResult with DOMAIN
 *
 * 		// Here's the cool stuff
 * 		domain Dependency with assetResultVar 'Runs on' dependentResult
 * 		...
 *}
 * </code>
 * @see TM-12031, TM-13542
 *
 */
@CompileStatic
class DependencyBuilder extends DomainBuilder {

	private RowResultFacade asset
	private RowResultFacade dependent
	private boolean withProcessed

	static final String ID_FIELD_NAME = 'id'
	static final String ASSET_FIELD_NAME = 'asset'
	static final String DEPENDENT_FIELD_NAME = 'dependent'
	static final String TYPE_FIELD_NAME = 'type'

	DependencyBuilder(ETLDomain domain, ETLProcessor processor) {
		super(domain, processor)
		validateDependencyDomain()
	}

	/**
	 * Defines dependent parameter
	 *
	 * <code>
	 *  // Load the asset side of the Dependency
	 *  domain Dependency with assetResultVar
	 * 	// or alternatively
	 * 	domain Dependency with assetResultVar and null
	 * <code>
	 * @param dependent
	 * @return
	 */
	DependencyBuilder with(RowResultFacade asset) {
		this.asset = validate(asset)
		process(ASSET_FIELD_NAME, this.asset)
		withProcessed = true
		return this
	}

	/**
	 * <p>Overrides {@code DependencyBuilder # with} to detect invalid asset parameter type.</p>
	 * If domain command is configured incorrectly with an incorrect parameter
	 * <pre>
	 * 	console on
	 * 	read labels
	 * 	domain Device
	 * 	iterate {
	 * 		extract 'name' load 'Name' set myVar
	 * 		domain Dependency with myVar
	 *	}
	 * </pre>
	 * @param asset
	 * @return
	 */
	DependencyBuilder with(Object asset) {
		throw ETLProcessorException.incorrectDomainVariableForDomainWithCommand()
	}

	/**
	 * Implementation of missing method is used by the dynamic behaviour in the Depdency command:
	 * <pre>
	 * 	domain Dependency with assetVar 'Runs On' dependentVar
	 * </pre>
	 * <p>This method is in charged to detect the correct scenario for Dependency command
	 * and also validate the {@code AssetDependency # type}</p>
	 * <p>Defines dependent parameter</p>
	 * <code>
	 *  // Load the asset side of the Dependency
	 *  domain Dependency with assetResultVar
	 * 	// or alternatively
	 * 	domain Dependency with assetResultVar and dependentResultVar
	 *  	...
	 * </code>
	 * @param methodName a method name used in the
	 * @param args an array with params for method missing
	 */
	def methodMissing(String methodName, Object args) {
		if (!withProcessed) {
			throw ETLProcessorException.unrecognizedDomainCommandArguments(methodName)
		}

		List argsList = args as List
		if (argsList.size() != 1 || argsList[0] in [RowResultFacade]) {
			throw ETLProcessorException.unrecognizedDomainCommandArguments(args)
		}

		if (!processor.assetDependencyTypesCache.isValidType(methodName)) {
			throw ETLProcessorException.invalidDependencyTypeInDomainDependencyWithCommand(methodName)
		}

		this.dependent = validate((RowResultFacade) argsList[0])
		process(DEPENDENT_FIELD_NAME, this.dependent, methodName)
		return this
	}

	/**
	 * Creates an instance of {@code ETLFieldDefinition} to be used in creation
	 * @param fieldName Dependency field name
	 * @return an instance of {@code ETLFieldDefinition}
	 */
	private ETLFieldDefinition createFieldDefinition(String fieldName) {
		PersistentProperty domainProperty = GormUtil.getDomainProperty(ETLDomain.Dependency.clazz, fieldName)
		return new ETLFieldDefinition(domainProperty)
	}

	/**
	 * Process an instance of {@code RowResult} copying logically all the necessary data to populate Dependency domain parameters.
	 * @param field
	 * @param rowResult
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	private void process(String field, RowResultFacade rowResultFacade, String dependencyType = null) {

		/**
		 * Initialize field in RowResult
		 */
		processor.load(field).with(null)

		RowResult currentRow = processor.result.findOrCreateCurrentRow()
		FieldResult fieldResult = currentRow.findOrCreateFieldData(createFieldDefinition(field))

		Map<String, FieldResult> fieldMap = rowResultFacade.getRowResult().fields
		/**
		 * Add Fields Part
		 */
		if (fieldMap.containsKey(ID_FIELD_NAME)) {
			fieldResult.originalValue = fieldMap[ID_FIELD_NAME].originalValue
			fieldResult.value = fieldMap[ID_FIELD_NAME].value
			fieldResult.init = fieldMap[ID_FIELD_NAME].init
			fieldResult.errors = fieldMap[ID_FIELD_NAME].errors
			fieldResult.warn = fieldMap[ID_FIELD_NAME].warn
			fieldResult.find = fieldMap[ID_FIELD_NAME].find
		}

		/**
		 * Add {@code AssetDependency # type}
		 */
		if (dependencyType) {
			processor.load(TYPE_FIELD_NAME).with(dependencyType)
		}

		/**
		 * Complete the whenNotFound part
		 */
		fieldResult.create = [:]
		fieldMap.each { String fieldName, FieldResult results ->
			if (ID_FIELD_NAME != fieldName) {
				fieldResult.create[fieldName] = (results.init != null) ? results.init : results.value
			}
		}
	}

	/**
	 * Validates if parameter is an instance of {@code RowResultFacade} is not null
	 * or if {@code RowResultFacade # rowResult} belongs to a domain in {@AssetEntity} hierarchy.
	 *
	 * @param dependencyFieldFacade
	 */
	private RowResultFacade validate(RowResultFacade rowResultFacade) {
		if (!rowResultFacade?.rowResult) {
			throw ETLProcessorException.incorrectDomainVariableForDomainWithCommand()
		}

		if (!ETLDomain.isDomainAsset(rowResultFacade.rowResult.domain)) {
			throw ETLProcessorException.invalidAssetEntityClassForDomainDependencyWithCommand()
		}

		return rowResultFacade
	}

	/**
	 * Validates if {@code DependencyBuilder # domain} is {@code ETLDomain # Dependency}.
	 * <pre>
	 * 	console on
	 * 	read labels
	 * 	domain Device
	 * 	iterate {
	 * 		extract 'name' load 'Name' set myVar
	 * 		domain Application with myVar
	 *	}
	 *	</pre>
	 */
	private void validateDependencyDomain() {
		if (ETLDomain.Dependency != this.domain) {
			throw ETLProcessorException.invalidDomainForDomainDependencyWithCommand()
		}
	}
}
