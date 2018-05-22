package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class DomainClassFieldsValidator implements ETLFieldsValidator {

	Map<ETLDomain, List<Map<String, ?>>> assetClassFieldsSpecMap = [:]
	Map<String, ETLFieldDefinition> fieldsDefinitionCache = [:]

	private String cacheKey(ETLDomain domain, String field){
		return domain.name().concat('.').concat(field)
	}

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

		String cacheKey = cacheKey(domain, field)
		if(fieldsDefinitionCache.containsKey(cacheKey)){
			return fieldsDefinitionCache.get(cacheKey)
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

		String cacheKey = cacheKey(domain, field)
		if(fieldsDefinitionCache.containsKey(cacheKey)){
			return fieldsDefinitionCache.get(cacheKey)
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

		fieldsDefinitionCache.put(cacheKey, fieldDefinition)
		return fieldDefinition
	}
}