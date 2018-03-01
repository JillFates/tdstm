package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class DomainClassFieldsValidator implements ETLFieldsValidator {

	Map<ETLDomain, List<Map<String, ?>>> assetClassFieldsSpecMap = [:]

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
	Boolean hasSpecs(ETLDomain domain, String field) {

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
	ETLFieldSpec lookup(ETLDomain domain, String field) {

		if(domain.isAsset()){
			if(hasSpecs(domain, field)){
				Map<String, ?> fieldSpec = assetClassFieldsSpecMap[domain].find {
					it.field == field || it.label == field
				}
				return new ETLFieldSpec(fieldSpec)
			}
		} else{
			Class<?> domainClass = domain.clazz
			GrailsDomainClassProperty domainProperty = GormUtil.getDomainProperty(domainClass, field)
			return new ETLFieldSpec(domainProperty)
		}
	}
}