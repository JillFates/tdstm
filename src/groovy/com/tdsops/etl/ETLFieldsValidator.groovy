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
     * @return a Map instance with fields name
     */
    Map<String, ?> lookup (ETLDomain domain, String field)

}