package com.tdsops.etl

/**
 *
 * Interface for validation of fields in the ETL processor
 *
 */
interface ETLFieldsValidator {

    /**
     *
     * Checks if there is a Field spec for a domain and field name
     *
     * @param domain : a ETL Domain used for looking a field spec up
     * @param field : field name use to lookup
     *
     * @return true if there is field spec for that field and domain
     */
    Boolean hasSpecs (ETLDomain domain, String field)

    /**
     *
     * It looks a field specification up based on a ETLDomain
     *
     * @param domain : a ETL Domain used for looking a field spec up
     * @param field : field name use to lookup
     *
     * @return an instance of ETLFieldSpec with field name, label and type
     */
	ETLFieldSpec lookup (ETLDomain domain, String field)

	/**
	 *
	 * @param domainClass
	 * @param propertyName
	 * @return
	 */
    boolean isDomainProperty(Class domainClass, String propertyName)

	/**
	 *
	 * @param domainClass
	 * @param propertyName
	 * @return
	 */
	boolean isDomainIdentifier(Class domainClass, String propertyName)

	/**
	 *
	 * @param domainClass
	 * @param propertyName
	 * @return
	 */
	boolean isReferenceProperty(Class domainClass, String propertyName)
}