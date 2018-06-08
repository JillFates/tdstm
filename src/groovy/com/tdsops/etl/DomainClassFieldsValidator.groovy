package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class DomainClassFieldsValidator implements ETLFieldsValidator {

	Map<ETLDomain, List<Map<String, ?>>> assetClassFieldsSpecMap = [:]
	Map<ETLDomain, Map<String, ETLFieldDefinition>> fieldsDefinitionCache = [:]

	/**
	 * Add fields specification for an ETLDomain instance
	 * @param domain an instance of ETLDomain
	 * @param fieldsSpec
	 */
	void addAssetClassFieldsSpecFor(ETLDomain domain, List<Map<String, ?>> fieldsSpec) {
		assetClassFieldsSpecMap[domain] = fieldsSpec
	}

	/**
	 * Checks if it has fields specification for a domain instance
	 * @param domain
	 * @param field
	 * @return
	 */
	//TODO: rename validate fieldName exists
	Boolean hasSpecs(ETLDomain domain, String field) {

		if(cacheContains(domain, field)){
			return true
		}

		if(domain.isAsset()){
			return (assetClassFieldsSpecMap[domain].find { it.field == field || it.label == field } != null)
		} else{
			return GormUtil.isDomainProperty(domain.clazz, field)
		}
	}

	/**
	 * Looks up a fields specification for a domain instance
	 * @param domain
	 * @param field
	 * @return
	 */
	ETLFieldDefinition lookup(ETLDomain domain, String field) {

		if(cacheContains(domain, field)){
			return getFromCache(domain, field)
		}

		ETLFieldDefinition fieldDefinition

		if(domain.isAsset()){
			if(hasSpecs(domain, field)){
				Map<String, ?> fieldSpec = assetClassFieldsSpecMap[domain].find {
					it.field == field || it.label == field
				}
				fieldDefinition = new ETLFieldDefinition(fieldSpec)
			}
		} else{
			Class<?> domainClass = domain.clazz
			GrailsDomainClassProperty domainProperty = GormUtil.getDomainProperty(domainClass, field)
			fieldDefinition = new ETLFieldDefinition(domainProperty)
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
	private boolean cacheContains(ETLDomain domain, String field){
		return fieldsDefinitionCache.containsKey(domain) && fieldsDefinitionCache[domain].containsKey(field)
	}

	/**
	 * //TODO complete DMC
	 * @param domain
	 * @param field
	 * @param fieldDefinition
	 */
	private void saveInCache(ETLDomain domain, String field, ETLFieldDefinition fieldDefinition){
		if(!fieldsDefinitionCache.containsKey(domain)){
			fieldsDefinitionCache.put(domain, [:])
		}
		fieldsDefinitionCache[domain].put(field, fieldDefinition)
	}

	/**
	 * // TODO complete DMC
	 * @param domain
	 * @param field
	 * @return
	 */
	private ETLFieldDefinition getFromCache(ETLDomain domain, String field){
		return fieldsDefinitionCache[domain][field]
	}



}