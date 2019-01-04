package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import grails.core.GrailsDomainClassProperty

class ETLFieldsValidator {

	Map<ETLDomain, List<Map<String, ?>>> assetClassFieldsSpecMap = [:]
	Map<ETLDomain, Map<String, ETLFieldDefinition>> fieldsDefinitionCache = [:]
	/**
	 * TODO Add docs
	 */
	Map<String, Map<String, String>> fieldLabelMap = [:]
	/**
	 * TODO Add docs
	 */
	Map<String, Map<String, String>> labelFieldMap = [:]


	/**
	 * Add fields specification for an ETLDomain instance
	 * @param domain an instance of ETLDomain
	 * @param fieldsSpec
	 */
	void addAssetClassFieldsSpecFor(ETLDomain domain, List<Map<String, ?>> fieldsSpec) {
		assetClassFieldsSpecMap[domain] = fieldsSpec
	}

	/**
	 * It looks a fieldNameOrLabel specification up based on a ETLDomain
	 * @param domain : a ETL Domain used for looking a fieldNameOrLabel spec up
	 * @param fieldNameOrLabel : fieldNameOrLabel name use to lookup
	 * @return an instance of ETLFieldDefinition with fieldNameOrLabel name, label and type
	 */
	ETLFieldDefinition lookup(ETLDomain domain, String fieldNameOrLabel) {

		if (cacheContains(domain, fieldNameOrLabel)) {
			return getFromCache(domain, fieldNameOrLabel)
		}

		ETLFieldDefinition fieldDefinition

		Map<String, ?> fieldSpec

		// Try finding the fieldspec for asset classes
		if (domain.isAsset()) {
			fieldSpec = assetClassFieldsSpecMap[domain].find {
				it.field == fieldNameOrLabel || it.label == fieldNameOrLabel
			}

			if (fieldSpec) {
				fieldDefinition = new ETLFieldDefinition(fieldSpec)
			}


		} else {
			GrailsDomainClassProperty domainProperty = GormUtil.getDomainProperty(domain.clazz, fieldNameOrLabel)
			if (domainProperty) {
				fieldDefinition = new ETLFieldDefinition(domainProperty)
			}

		}

		if (! fieldDefinition) {
			throw ETLProcessorException.unknownDomainFieldName(domain, fieldNameOrLabel)
		}

		saveInCache(domain, fieldNameOrLabel, fieldDefinition)
		return fieldDefinition
	}

	/**
	 * Check if the internal cache for fieldDefinitions contains an entry
	 * for the ETLDomain instance and a fieldNameOrLabel name/label
	 * @param domain and instance of ETLDomain
	 * @param field a String content with a fieldNameOrLabel name or a fieldNameOrLabel label
	 * @return true if fieldsDefinitionCache contains the pair of ETLDomain + fieldNameOrLabel name/label
	 */
	private boolean cacheContains(ETLDomain domain, String field) {
		return fieldsDefinitionCache.containsKey(domain) && fieldsDefinitionCache[domain].containsKey(field)
	}

	/**
	 * Save in an internal cache a fieldNameOrLabel definitions for a specific fieldNameOrLabel in a particular domain
	 * @param domain an instance of ETLDomain
	 * @param field fieldNameOrLabel name or fieldNameOrLabel label
	 * @param fieldDefinition ETLFieldDefinition instance for the fieldNameOrLabel parameter
	 */
	private void saveInCache(ETLDomain domain, String field, ETLFieldDefinition fieldDefinition){
		if(!fieldsDefinitionCache.containsKey(domain)){
			fieldsDefinitionCache.put(domain, [:])
			fieldLabelMap.put(domain.name(), [:])
			labelFieldMap.put(domain.name(), [:])
		}

		if (! fieldsDefinitionCache[domain].containsKey(field)) {
			fieldsDefinitionCache[domain].put(field, fieldDefinition)
			fieldLabelMap[domain.name()].put(fieldDefinition.name, fieldDefinition.label)
			// Additionaly create label to fieldNameOrLabel map
			labelFieldMap[domain.name()].put(fieldDefinition.label, fieldDefinition.name)
		}
	}

	/**
	 * Return from an internal cache an instance of ETLFieldDefinition
	 * @param domain an instance of ETLDomain
	 * @param field fieldNameOrLabel name or fieldNameOrLabel label
	 * @return ETLFieldDefinition instance for the fieldNameOrLabel parameter
	 */
	private ETLFieldDefinition getFromCache(ETLDomain domain, String field){
		return fieldsDefinitionCache[domain][field]
	}

	/**
	 * Builds a map with label results used during the ETL script valuation
	 * Following this ETL script:
	 * <pre>
	 *  read labels
	 *  domain Application
	 *  iterate {
	 *      extract 1 load 'Vendor'
	 *      extract 2 load 'assetName'
	 *  }
	 * </pre>
	 *  It build this Map
	 * <pre>
	 *  fieldLabelMap : [
	 *      'Vendor': 'appVendor'
	 *  ]
	 * </pre>
	 * @return
	 */
	Map<String, Map<String, String>> fieldLabelMapForResults() {
		return fieldLabelMap
	}

	Map<String, String> fieldLabelMapForDomain(String domain){
		return fieldLabelMap[domain]
	}

}
