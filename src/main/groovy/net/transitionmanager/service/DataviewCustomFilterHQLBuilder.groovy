package net.transitionmanager.service

import com.tdsops.common.sql.SqlUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.asset.AssetType
import net.transitionmanager.imports.DataviewHqlWhereCollector
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.search.FieldSearchData

import java.sql.Timestamp

/**
 * TM-14768. It builds custom filters for Planning Dashboard defined by
 * https://docs.google.com/spreadsheets/d/11_DjdACYvy5IB7Zup3VH4mb6pChF1NgAnjHH2rgaSQ8/edit?ts=5cb64b90#gid=1016467595
 */
class DataviewCustomFilterHQLBuilder {

	private Project queryProject

	DataviewCustomFilterHQLBuilder(Project project) {
		this.queryProject = project
	}

	/**
	 * Defines a custom filter name used from the UI
	 * for adding custom filters like 'physicalServer' or 'virtualServer'
	 */
	public static final String CUSTOM_FILTER = '_filter'

	/**
	 *
	 * @param extraFilter * @return
	 */
	Map buildQueryExtraFilters(Map extraFilter) {
		String hqlExpression
		Map<String, ?> hqlParams

		String domain = extraFilter['domain']
		String property = extraFilter['property']
		Object filter = extraFilter['filter']

		switch (property) {
			case 'ufp':
				hqlExpression = " AE.moveBundle in (:extraFilterMoveBundles) "
				hqlParams = [
					extraFilterMoveBundles: MoveBundle.where {
						project == queryProject && useForPlanning == filter
					}.list()
				]
				break
			default:
				throw new RuntimeException('Invalid filter definition:' + property)
		}

		return [
			hqlExpression: hqlExpression,
			hqlParams    : hqlParams
		]
	}

	/**
	 * <p>Prepares a named filter used by UI for filtering assets using business rules about assets.
	 * For example, filtering by 'physicalServer' or 'storage' in asset types.</p>
	 *
	 * @param namedFilter a String value used as a named filter
	 * @return a Map with 2 values, sqlExpression and sqlParams
	 * 			to be used in an hql sentence
	 */
	public Map<String, ?> buildQueryNamedFilters(String namedFilter) {
		String hqlExpression
		Map<String, ?> hqlParams

		switch (namedFilter) {
			case 'physical':
				hqlExpression = " COALESCE(AE.assetType,'') NOT IN (:namedFilterVirtualServerTypes) "
				hqlParams = ['namedFilterVirtualServerTypes': AssetType.virtualServerTypes]
				break
			case 'physicalServer':
				hqlExpression = " AE.assetType IN (:namedFilterPhyServerTypes) "
				hqlParams = ['namedFilterPhyServerTypes': AssetType.allServerTypes - AssetType.virtualServerTypes]
				break
			case 'server':
				hqlExpression = " AE.assetType IN (:namedAllServerTypes) "
				hqlParams = ['namedAllServerTypes': AssetType.allServerTypes]
				break
			case 'storage':
				hqlExpression = " AE.assetType IN (:namedStorageTypes) "
				hqlParams = ['namedStorageTypes': AssetType.storageTypes]
				break
			case 'virtualServer':
				hqlExpression = " AE.assetType IN (:namedFilterVirtualServerTypes) "
				hqlParams = ['namedFilterVirtualServerTypes': AssetType.virtualServerTypes]
				break
			case 'other':
				hqlExpression = " COALESCE(ae.assetType,'') NOT IN  (:namedFilterNonOtherTypes) "
				hqlParams = ['namedFilterNonOtherTypes': AssetType.nonOtherTypes]
				break
			case 'justPlanning':
				hqlExpression = " AE.moveBundle in (:namedFilterMoveBundles) "
				hqlParams = [
					namedFilterMoveBundles: MoveBundle.where {
						project == queryProject && useForPlanning == true
					}.list()
				]
				break

			default:
				throw new RuntimeException('Invalid filter definition:' + namedFilter)
		}

		return [
			hqlExpression: hqlExpression,
			hqlParams    : hqlParams
		]
	}

	/**
	 * <p>Add a column filter results in HSQL sentence.</p>
	 * <p>It uses {@code DataviewHqlWhereCollector} </p>
	 * @param column
	 * @param project
	 * @param whereCollector
	 * @param mixedKeys
	 * @param mixedFieldsInfo
	 */
	private void addColumnFilter(Map<String, ?> column,
								 Project project,
								 DataviewHqlWhereCollector whereCollector,
								 Map<String, List> mixedFieldsInfo) {

		// The keys for all the declared mixed fields.
		Set mixedKeys = mixedFields.keySet()

		Class type = typeFor(column)
		String filter = filterFor(column)

		if (StringUtil.isNotBlank(filter) && !(type in [Date, Timestamp])) {
			// TODO: dcorrea: TM-13471 Turn off filter by date and datetime.
			// Create a basic FieldSearchData with the info for filtering an individual field.
			FieldSearchData fieldSearchData = new FieldSearchData([
				column           : propertyFor(column),
				columnAlias      : namedParameterFor(column),
				domain           : domainFor(column),
				filter           : filterFor(column),
				type             : type,
				whereProperty    : wherePropertyFor(column),
				manyToManyQueries: manyToManyQueriesFor(column),
				fieldSpec        : column.fieldSpec
			])

			String property = propertyFor(column)
			// Check if the current column requires special treatment (e.g. startupBy, etc.)

			if (property in mixedKeys) {
				// Flag the fieldSearchData as mixed.
				fieldSearchData.setMixed(true)
				// Retrieve the additional results (e.g: persons matching the filter).
				Closure sourceForField = sourceFor(property)
				Map additionalResults = sourceForField(project, filterFor(column), mixedFieldsInfo)
				if (additionalResults) {
					// Keep a copy of this results for later use.
					mixedFieldsInfo[property] = additionalResults
					// Add additional information for the query (e.g: the staff ids for IN clause).
					Closure paramsInjector = injectWhereParamsFor(property)
					paramsInjector(fieldSearchData, property, additionalResults)
					// Add the sql where clause for including the additional fields in the query
					Closure whereInjector = injectWhereClauseFor(property)
					whereInjector(fieldSearchData, property)
				} else {
					// If no additional results, then unset the flag as no additional filtering should be required.
					fieldSearchData.setMixed(false)
				}
			}

			// Trigger the parsing of the parameter.
			SqlUtil.parseParameter(fieldSearchData)

			if (fieldSearchData.sqlSearchExpression) {
				// Append the where clause to the list of conditions.
				// hqlWhereConditions << fieldSearchData.sqlSearchExpression
				whereCollector.addCondition(fieldSearchData.sqlSearchExpression)
			}

			if (fieldSearchData.sqlSearchParameters) {
				// Add the parameters required for this field.
				// hqlWhereParams += fieldSearchData.sqlSearchParameters
				whereCollector.addParams(fieldSearchData.sqlSearchParameters)
			}

			// If the filter for this column is empty, some logic/transformation might still be required for the mixed fields
		} else {
			String property = propertyFor(column)
			if (property in mixedKeys) {
				Closure sourceForField = sourceFor(property)
				Map additionalResults = sourceForField(project, filterFor(column), mixedFieldsInfo)
				// Keep a copy of this results for later use.
				mixedFieldsInfo[property] = additionalResults
			}
		}
	}
}
