package com.tdsops.etl

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
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
			return isDomainProperty(domain.clazz, field)
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
			GrailsDomainClassProperty domainProperty = getDomainProperty(domainClass, field)
			return new ETLFieldSpec(domainProperty)
		}
	}

	/**
	 *
	 * @param domainClass
	 * @param propertyName
	 * @return
	 */
	@Override
	boolean isDomainProperty(Class domainClass, String propertyName) {
		DefaultGrailsDomainClass grailsDomainClass = grailsDomainClass(domainClass)
		return grailsDomainClass.hasPersistentProperty(propertyName) ||
			grailsDomainClass.identifier.name == propertyName
	}

	/**
	 *
	 * @param domainClass
	 * @param propertyName
	 * @return
	 */
	@Override
	boolean isDomainIdentifier(Class domainClass, String propertyName) {
		DefaultGrailsDomainClass grailsDomainClass = grailsDomainClass(domainClass)
		return grailsDomainClass.identifier.name == propertyName
	}

	/**
	 *
	 * @param domainClass
	 * @param propertyName
	 * @return
	 */
	@Override
	boolean isReferenceProperty(Class domainClass, String propertyName) {
		DefaultGrailsDomainClass grailsDomainClass = grailsDomainClass(domainClass)
		GrailsDomainClassProperty grailsDomainClassProperty = grailsDomainClass.getPropertyByName(propertyName)
		return grailsDomainClassProperty.getReferencedDomainClass() != null || grailsDomainClassProperty.isAssociation()
	}

	/**
	 *
	 * @param domainClass
	 * @return
	 */
	private DefaultGrailsDomainClass grailsDomainClass(Class domainClass) {
		return new DefaultGrailsDomainClass(domainClass)
	}

	/**
	 *
	 * @param domainClass
	 * @param propertyName
	 * @return
	 */
	GrailsDomainClassProperty getDomainProperty(Class domainClass, String propertyName) {
		DefaultGrailsDomainClass grailsDomainClass = grailsDomainClass(domainClass)
		return grailsDomainClass.getPropertyByName(propertyName)
	}

}