package net.transitionmanager.search

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.sql.SqlUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.Project
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.transform.Transformers

abstract class DomainQueryBuilder {

	/**
	 * The class corresponding to the domain being queried.
	 */
	Class domainClass

	/**
	 * A map with an entry for each field that needs to be selected, along with the required
	 * information for filtering, etc.
	 */
	protected Map fieldsMap

	/**
	 * A map with the filters provided by the user to narrow down the search.
	 */
	protected Map filterParams

	/**
	 * The user's current project.
	 */
	protected Project project

	/**
	 * A map with the parameters for sorting the results.
	 * Example: [sortIndex: 'c1', sortOrder: 'desc']
	 */
	protected Map sortingParams

	/**
	 * A map with the pagination information and additional information for the query.
	 * Example: [max: 25, offset: 100]
	 */
	protected Map paginationParams

	/**
	 * This string will contain the series of fields that are going to be selected.
	 */
	protected String hqlSelect

	/**
	 * String with the all the required joins for the query.
	 */
	protected String hqlFrom

	/**
	 * Series of conditions to filter the domains being queried.
	 */
	protected String hqlWhereConditions

	/**
	 * Parameters for the query.
	 */
	protected Map hqlWhereParameters

	Session session


	/**
	 * Constructor that takes in different parameters required for building the query and does some initialization.
	 * @param project - user's current project.
	 * @param filterParams - filters for the search.
	 * @param sortingParams - parameters for sorting the results.
	 * @param paginationParams - parameters for pagination.
	 */
	DomainQueryBuilder(Project project, Map filterParams, Map sortingParams, Map paginationParams) {
		this.project = project
		this.filterParams = filterParams
		this.paginationParams = paginationParams
		domainClass = getDomainClass()
		fieldsMap = getDomainFieldsMap()
		if (!sortingParams || !sortingParams['sortIndex']) {
			sortingParams = getDefaultSorting()
		}
		this.sortingParams = sortingParams
		session = ApplicationContextHolder.getBean("sessionFactory").getCurrentSession()
	}

	/**
	 * Query the domain using the filters provided when creating this instance.
	 * This method will return a map with the following information:
	 * [
	 *      total: number of records matching the filters.
	 *      domains: List of domains found according to filters and pagination parameters.
	 * ]
	 *
	 * @return
	 */
	Map queryDomain() {

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
	 */
	protected void createHqlSelect() {
		List<String> projections = []
		fieldsMap.each { fieldAlias, fieldMap ->
			if (!fieldMap.containsKey('property')) {
				throw new RuntimeException("No property found for querying ${fieldAlias}.")
			}
			projections << "${fieldMap['property']} AS ${fieldAlias}"
		}
		hqlSelect = projections.join(",\n")
	}

	/**
	 * Create a string with the from statement for the query being built.
	 * This method will use the domain name and the alias, along with the domains map, to construct a series
	 * of "LEFT OUTER JOIN $domain as $alias" with the exception of the first domain, which won't have the
	 * actual LEFT OUTER JOIN.
	 */
	protected void createHqlFrom() {
		String domainName = domainClass.getSimpleName()
		String domainAlias = getDomainAlias()
		List<Map<String, String>> joinsList = getJoinsList()

		String domainFrom = domainAlias ? "${domainName} ${domainAlias}" : domainName
		List<String> joins = [domainFrom]
		joinsList.each { join ->
			joins << "${join['property']} ${join['alias']}"
		}

		hqlFrom = joins.join("\nLEFT OUTER JOIN ")
	}

	/**
	 * This method resolves and constructs the 'WHERE' conditions along with
	 * the parameters for querying the domain.
	 */
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

	/**
	 * Return the number of records matching the filters.
	 * @return a long number corresponding to the total number of records that match the criteria.
	 */
	protected Long getDomainsCount() {
		String hqlQuery = """
							SELECT COUNT(${getCountProperty()})
							FROM ${hqlFrom}
							WHERE ${hqlWhereConditions}
						"""
		return domainClass.executeQuery(hqlQuery, hqlWhereParameters)[0]

	}

	/**
	 * Query the database for the domain objects using a selection of fields, filters, sorting
	 * and pagination information.
	 * @return a list with the records found.
	 */
	protected List getDomains() {
		String groupByProperty = getGroupByProperty()
		String groupBy = ""
		if (!StringUtil.isBlank(groupByProperty)) {
			groupBy = "GROUP BY ${groupByProperty}"
		}

		String hqlQuery = """
							SELECT ${hqlSelect}
							FROM ${hqlFrom}
							WHERE ${hqlWhereConditions}
							${groupBy}
							ORDER BY ${sortingParams['sortIndex']} ${sortingParams['sortOrder']}		
						"""
		Query query = session.createQuery(hqlQuery)
			.setFirstResult(paginationParams['offset'])
			.setMaxResults(paginationParams['max'])
			.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
		hqlWhereParameters.each {key, value ->
			query.setParameter(key, value)
		}
		return query.list()
	}

	/**
	 * Construct and return a FieldSearchData for the current field.
	 * Subclasses can reimplement this if additional processing is required, such is the case
	 * for assets, where additional information needs to be set.
	 * @param fieldName
	 * @param fieldMap
	 * @return
	 */
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

	/**
	 * Return the property name that needs to be used for the query
	 * @param fieldName
	 * @param fieldMap
	 * @return
	 */
	protected String propertyFor(String fieldName, Map fieldMap) {
		return fieldMap['property']
	}

	/**
	 * Return the string that is going to be used for the parameters for this field.
	 * @param fieldName
	 * @param fieldMap
	 * @return
	 */
	protected String namedParameterFor(String fieldName, Map fieldMap) {
		return fieldName
	}

	/**
	 * Return the filter provided by the user for this field.
	 * @param fieldName
	 * @return
	 */
	protected String filterFor(String fieldName) {
		return filterParams['filter']
	}

	/**
	 * Return the String with the many-to-many query (should it apply) for the current field.
	 * @param fieldName
	 * @param fieldMap
	 * @return
	 */
	protected String manyToManyQueriesFor(String fieldName, Map fieldMap) {
		return fieldMap['manyToManyQueries']
	}

	/**
	 * Return the property that needs to be used in where subqueries (if needed).
	 * @param fieldName
	 * @param fieldMap
	 * @return
	 */
	protected String wherePropertyFor(String fieldName, Map fieldMap) {
		return fieldMap['whereProperty']
	}

	/**
	 * Return the domain for the current field.
	 * @param fieldName
	 * @param fieldMap
	 * @return
	 */
	protected Class domainFor(String fieldName, Map fieldMap) {
		return fieldMap['domain']
	}

	/**
	 * Return the type of the current field (String, Integer, etc.).
	 * @param fieldName
	 * @param fieldMap
	 * @return
	 */
	protected Class typeFor(String fieldName, Map fieldMap) {
		return fieldMap['type']
	}

	/**
	 * Return the default where contiditions that all queries for the domain must have.
	 * @return
	 */
	protected List<String> getDefaultWhereConditions() {
		return []
	}

	/**
	 * Return a map with the parameters corresponding to the default where clause.
	 * @return
	 */
	Map getDefaultWhereParameters(){
		return [:]
	}

	/**
	 * Return the property, or expression, that is needed for the count query.
	 * @return
	 */
	protected String getCountProperty() {
		return "*"
	}

	/**
	 * Return the property or expression that is going to be used for grouping results.
	 * @return
	 */
	protected String getGroupByProperty() {
		return ""
	}

	/**
	 * Return the class corresponding to the domain being queried.
	 * @return
	 */
	abstract Class getDomainClass()

	/**
	 * Return the alias for the domain.
	 * @return
	 */
	abstract String getDomainAlias()

	/**
	 * Return a map with all the fields that should be part of the 'SELECT' of the query, along with
	 * the necessary information for filtering them accordingly.
	 * @return
	 */
	abstract Map<String, Map> getDomainFieldsMap()

	/**
	 * Return a list of domains that are need to be joint for the query.
	 * @return
	 */
	abstract List<Map<String, String>> getJoinsList()

	/**
	 * Return with the default sorting criteria.
	 * @return
	 */
	abstract Map<String, String> getDefaultSorting()
}
