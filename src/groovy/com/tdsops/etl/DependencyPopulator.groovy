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
	private ETLFieldDefinition createFieldDefinition(String fieldName){
		GrailsDomainClassProperty domainProperty = GormUtil.getDomainProperty(ETLDomain.Dependency.clazz, fieldName)
		return new ETLFieldDefinition(domainProperty)
	}

	private void processAsset() {
		if (this.asset) {

			processor.load(ASSET_FIELD_NAME).with(null)
			RowResult currentRow = processor.result.findOrCreateCurrentRow()
			FieldResult fieldResult = currentRow.findOrCreateFieldData(createFieldDefinition(ASSET_FIELD_NAME))

			// add find/elseFind results
			if(this.asset.fields.containsKey('id')){
				fieldResult.find = this.asset.fields['id'].find
			}

			fieldResult.create = [:]
			this.asset.fields.each { String fieldName, FieldResult field ->
				if(field.originalValue){
					fieldResult.create[fieldName] = field.originalValue
				}
			}
		}
	}

	private void processDependent() {
		validateParams()
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
