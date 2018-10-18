package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

/**
 *
 * <code>
 * 	iterate {* 		domain Application
 * 		...
 * 		set assetResultVar with DOMAIN
 *
 * 	    domain Device
 * 	    ...
 * 	    set dependentResult with DOMAIN
 *
 * 		// Here's the cool stuff
 * 		domain Dependency with assetResultVar and dependentResult
 * 		...
 *}* </code>
 * @see TM-12031.
 *
 */
@CompileStatic
class DependencyPopulator {

	private ETLProcessor processor
	private RowResult asset
	private RowResult dependent

	static final String ID_FIELD_NAME = 'id'
	static final String ASSET_FIELD_NAME = 'asset'
	static final String DEPENDENT_FIELD_NAME = 'dependent'

	DependencyPopulator(ETLProcessor processor) {
		this.processor = processor
	}

	/**
	 * Defines dependent parameter
	 *
	 * <code>
	 *  // Load the asset side of the Dependency
	 *  domain Dependency with assetResultVar
	 * 	// or alternatively
	 * 	domain Dependency with assetResultVar and null
	 *  }
	 * <code>
	 * @param dependent
	 * @return
	 */
	DependencyPopulator with(RowResult asset) {
		this.asset = asset
		this.processAsset()
		return this
	}

	/**
	 * Defines dependent parameter
	 *
	 * <code>
	 *  // Load the asset side of the Dependency
	 *  domain Dependency with assetResultVar
	 * 	// or alternatively
	 * 	domain Dependency with assetResultVar and null
	 *
	 *  	...
	 *  }
	 * <code>
	 * @param dependent
	 * @return
	 */
	DependencyPopulator and(RowResult dependent) {
		this.dependent = dependent
		this.processDependent()
		return this
	}

	/**
	 * Creates an instance of {@code ETLFieldDefinition} to be used in creation
	 * @param fieldName Dependency field name
	 * @return an instance of {@code ETLFieldDefinition}
	 */
	private ETLFieldDefinition createFieldDefinition(String fieldName) {
		GrailsDomainClassProperty domainProperty = GormUtil.getDomainProperty(ETLDomain.Dependency.clazz, fieldName)
		return new ETLFieldDefinition(domainProperty)
	}

	/**
	 * Process {@code Dependency#asset} field
	 */
	private void processAsset() {
		if (this.asset) {
			process(ASSET_FIELD_NAME, this.asset)
		}
	}

	/**
	 * Process {@code Dependency#dependent} field
	 */
	private void processDependent() {
		validateParams()
		if(this.dependent){
			process(DEPENDENT_FIELD_NAME, this.dependent)
		}
	}

	/**
	 *
	 * @param field
	 * @param rowResult
	 */
	private void process(String field, RowResult rowResult){
		processor.load(field).with(null)
		RowResult currentRow = processor.result.findOrCreateCurrentRow()
		FieldResult fieldResult = currentRow.findOrCreateFieldData(createFieldDefinition(field))

		// add find/elseFind results
		if (rowResult.fields.containsKey(ID_FIELD_NAME)) {
			fieldResult.find = rowResult.fields[ID_FIELD_NAME].find
		}

		fieldResult.create = [:]
		rowResult.fields.each { String fieldName, FieldResult results ->
			if (results.originalValue) {
				fieldResult.create[fieldName] = results.originalValue
			}
		}
	}

	/**
	 * Validate that the command arguments are correct
	 */
	private void validateParams() {
		if (!asset && !dependent) {
			throw ETLProcessorException.invalidDependentParamsCommand()
		}
	}
}
