package com.tdsops.etl

import com.tdssrc.grails.GormUtil
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class DomainFieldsValidator implements ETLFieldsValidator{

	/**
	 *
	 * Checks if there is a Field spec for a domain and field name
	 *
	 * @param domain : a ETL Domain used for looking a field spec up
	 * @param field : field name use to lookup
	 *
	 * @return true if there is field spec for that field and domain
	 */
	@Override
	Boolean hasSpecs(ETLDomain domain, String field) {
		Class<?> domainClass = ETLDomain.lookupDomainClass(domain)
		return GormUtil.isDomainProperty(domainClass.newInstance(), field)
	}

	/**
	 *
	 * It looks a field specification up based on a ETLDomain
	 *
	 * @param domain : a ETL Domain used for looking a field spec up
	 * @param field : field name use to lookup
	 *
	 * @return a Map instance with fields name
	 */
	@Override
	Map<String, ?> lookup(ETLDomain domain, String field) {
		Class<?> domainClass = ETLDomain.lookupDomainClass(domain)
		GrailsDomainClassProperty domainProperty = GormUtil.getDomainProperty(domainClass.newInstance(), field)
		return [
			name: domainProperty.getName(),
			fieldName: domainProperty.getFieldName()
		]
	}
}
