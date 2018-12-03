package net.transitionmanager.search

import com.tdsops.common.sql.SqlUtil
import net.transitionmanager.domain.Project

abstract class DomainQueryBuilder {

	Class domain

	protected Map fieldsMap

	protected Map filterParams

	protected Project project

	protected Map sortingParams

	protected Map paginationParams

	protected String hqlSelect

	protected String hqlFrom

	protected String hqlWhereConditions

	protected Map hqlWhereParameters


	DomainQueryBuilder(Project project, Map filterParams, Map sortingParams, Map paginationParams) {
		domain = getDomain()
		fieldsMap = getDomainFieldsMap()
		this.project = project
		this.filterParams = filterParams
		this.sortingParams = sortingParams
		this.paginationParams = paginationParams
	}

	/**
	 * Query the domain using the filters provided when creating this instance.
	 * @return
	 */
	Map queryDomain() {
		if (!domainClass) {
			throw new RuntimeException("Build Query called with a null domain class")
		}

		// Create the 'SELECT' for the query.
		createHqlSelect()

		// Build the 'FROM' with all the required joins.
		createHqlFrom()

		// Create the 'WHERE' statements along with the parameters to be injected
		createHqlWhere()

		return [
		    total: getDomainsCount(),
			domains: getDomains()
		]
	}


	/**
	 * This method creates a string with all the fields to be included in the SELECT for
	 * the query using each key as the alias for the field and the 'property' in the corresponding map
	 * as the field being projected.
	 *
	 */
	protected void createHqlSelect() {
		List<String> projections = []
		fieldsMap.each { fieldAlias, fieldMap ->
			if (!fieldsMap.containsKey('property')) {
				throw new RuntimeException("No property found for querying ${fieldAlias}.")
			}
			projections << "${fieldsMap['property']} AS ${fieldAlias}"
		}
		hqlSelect = projections.join(",\n")
	}

	/**
	 * Create a string with the from statement for the query being built.
	 * This method will use the domain name and the alias, along with the domains map, to construct a series
	 * of "LEFT OUTER JOIN $domain as $alias" with the exception of the first domain, which won't have the
	 * actual LEFT OUTER JOIN.
	 *
	 */
	protected void createHqlFrom() {
		String domainName = domain.getSimpleName()
		String domainAlias = getDomainAlias()
		List<Map<String, String>> joinsList = getJoinsList()

		String domainFrom = domainAlias ? "${domainName} AS ${domainAlias}" : domainName
		List<String> joins = [domainFrom]
		joinsList.each { join ->
			joins << "${join['property']} ${join['alias']}"
		}

		hqlFrom = joins.join("\nLEFT OUTER JOIN ")
	}


	protected void createHqlWhere() {
		List<String> whereConditions = getDefaultWhereConditions()
		hqlWhereParameters = getDefaultWhereParameters()
		filterParams.each { fieldName, filterValue ->
			if (fieldsMap.containsKey(fieldName)) {
				FieldSearchData fieldSearchData = createFieldSearchData(fieldName, fieldsMap)
				SqlUtil.parseParameter(fieldSearchData)
				whereConditions << fieldSearchData.sqlSearchExpression
				hqlWhereParameters += fieldSearchData.sqlSearchParameters
			}
		}
		hqlWhereConditions = whereConditions.join(" AND \n")
	}

	protected String getHqlOrderBy() {
		return "ORDER BY ${sortingParams['sortIndex']} ${sortingParams['sortOrder']}"
	}

	protected FieldSearchData createFieldSearchData(String fieldName, Map fieldMap) {
		return new FieldSearchData([
			column: propertyFor(fieldName, fieldMap),
			columnAlias: namedParameterFor(fieldName, fieldMap),
			domain: domainFor(fieldName, fieldMap),
			filter: filterFor(fieldName),
			type: typeFor(fieldName, fieldMap),
			whereProperty: wherePropertyFor(fieldName, fieldMap),
			manyToManyQueries: manyToManyQueriesFor(fieldName, fieldMap),
		])
	}

	protected Long getDomainsCount() {
		String hqlQuery = """
							SELECT COUNT(*)
							FROM ${hqlFrom}
							WHERE ${hqlWhereParameters}
						"""
		return domain.executeQuery(hqlQuery, hqlWhereParameters)[0]

	}

	protected List getDomains() {
		String hqlQuery = """
							SELECT ${hqlSelect}
							FROM ${hqlFrom}
							WHERE ${hqlWhereParameters}
						"""
		return domain.executeQuery(hqlQuery, hqlWhereParameters, paginationParams)
	}

	protected String propertyFor(String fieldName, Map fieldMap) {
		return fieldMap['property']
	}

	protected String namedParameterFor(String fieldName, Map fieldMap) {
		return fieldName
	}

	protected String filterFor(String fieldName) {
		return filterParams['filter']
	}

	protected String manyToManyQueriesFor(String fieldName, Map fieldMap) {
		return fieldMap['manyToManyQueries']
	}

	protected String wherePropertyFor(String fieldName, Map fieldMap) {
		return fieldMap['whereProperty']
	}

	protected Class domainFor(String fieldName, Map fieldMap) {
		return fieldMap['domain']
	}

	protected Class typeFor(String fieldName, Map fieldMap) {
		return fieldMap['type']
	}

	protected List<String> getDefaultWhereConditions() {
		return []
	}

	Map getDefaultWhereParameters(){
		return [:]
	}

	abstract Class getDomain()
	abstract String getDomainAlias()
	abstract Map<String, Map> getDomainFieldsMap()
	abstract List<Map<String, String>> getJoinsList()
}
