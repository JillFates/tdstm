package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class ETLFieldsValidator {

	Map<ETLDomain, List<Map<String, ?>>> assetClassFieldsSpecMap = [:]
	Map<ETLDomain, Map<String, ETLFieldDefinition>> fieldsDefinitionCache = [:]
	Map<String, Map<String, String>> fieldLabelMap = [:]

	/**
	 * Add fields specification for an ETLDomain instance
	 * @param domain an instance of ETLDomain
	 * @param fieldsSpec
	 */
	void addAssetClassFieldsSpecFor(ETLDomain domain, List<Map<String, ?>> fieldsSpec) {
		assetClassFieldsSpecMap[domain] = fieldsSpec
	}

	/**
	 * It looks a field specification up based on a ETLDomain
	 * @param domain : a ETL Domain used for looking a field spec up
	 * @param field : field name use to lookup
	 * @return an instance of ETLFieldDefinition with field name, label and type
	 */
	ETLFieldDefinition lookup(ETLDomain domain, String field) {

		if (cacheContains(domain, field)) {
			return getFromCache(domain, field)
		}

		ETLFieldDefinition fieldDefinition

		Map<String, ?> fieldSpec

		// Try finding the fieldspec for asset classes
		if (domain.isAsset()) {
			fieldSpec = assetClassFieldsSpecMap[domain].find {
				it.field == field || it.label == field
			}

			if (fieldSpec) {
				fieldDefinition = new ETLFieldDefinition(fieldSpec){}
			}
		} else {
			GrailsDomainClassProperty domainProperty = GormUtil.getDomainProperty(domain.clazz, field)
			if (domainProperty) {
				fieldDefinition = new ETLFieldDefinition(domainProperty)
			}

		}

		if (! fieldDefinition) {
			throw ETLProcessorException.unknownDomainFieldName(domain, field)
		}

		saveInCache(domain, field, fieldDefinition)
		return fieldDefinition
	}

	/**
	 * Check if the internal cache for fieldDefinitions contains an entry
	 * for the ETLDomain instance and a field name/label
	 * @param domain and instance of ETLDomain
	 * @param field a String content with a field name or a field label
	 * @return true if fieldsDefinitionCache contains the pair of ETLDomain + field name/label
	 */
	private boolean cacheContains(ETLDomain domain, String field) {
		return fieldsDefinitionCache.containsKey(domain) && fieldsDefinitionCache[domain].containsKey(field)
	}

	/**
	 * Save in an internal cache a field definitions for a specific field in a particular domain
	 * @param domain an instance of ETLDomain
	 * @param field field name or field label
	 * @param fieldDefinition ETLFieldDefinition instance for the field parameter
	 */
	private void saveInCache(ETLDomain domain, String field, ETLFieldDefinition fieldDefinition){
		if(!fieldsDefinitionCache.containsKey(domain)){
			fieldsDefinitionCache.put(domain, [:])
			fieldLabelMap.put(domain.name(), [:])
		}

		if (! fieldsDefinitionCache[domain].containsKey(field)) {
			fieldsDefinitionCache[domain].put(field, fieldDefinition)
			fieldLabelMap[domain.name()].put(fieldDefinition.name, fieldDefinition.label)
		}
	}

	/**
	 * Return from an internal cache an instance of ETLFieldDefinition
	 * @param domain an instance of ETLDomain
	 * @param field field name or field label
	 * @return ETLFieldDefinition instance for the field parameter
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