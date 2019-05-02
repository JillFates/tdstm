package net.transitionmanager.service

import net.transitionmanager.asset.AssetType

/**
 * TM-14768. It builds custom filters for Planning Dashboard defined by
 * https://docs.google.com/spreadsheets/d/11_DjdACYvy5IB7Zup3VH4mb6pChF1NgAnjHH2rgaSQ8/edit?ts=5cb64b90#gid=1016467595
 */
class DataviewCustomFilterHQLBuilder {

	/**
	 * Defines a custom filter name used from the UI
	 * for adding custom filters like 'physicalServer' or 'virtualServer'
	 */
	public static final String CUSTOM_FILTER = '_filter'

	private Map column

	DataviewCustomFilterHQLBuilder(Map column) {
		this.column = column
	}

	/**
	 *
	 * @return
	 */
	Map buildQueryFilters() {

		Map<String, ?> queryFilters = [:]
		if (column.label == CUSTOM_FILTER) {
			queryFilters = hqlCustomFilters()
		}

		return queryFilters
	}

	/**
	 * Prepares a Custom filter used by UI for filtering assets using business rules about assets.
	 * For example, filtering by 'physicalServer' or 'storage'
	 *
	 * @param column a Column map definition
	 * @return a Map with 2 values, sqlExpression and sqlParams
	 * 			to be used in an hql sentence
	 */
	private hqlCustomFilters() {
		String sqlExpression = ''
		Map<String, ?> sqlParams = [:]

		switch (column.filter) {
			case 'physical':
				sqlExpression = " COALESCE(AE.assetType,'') NOT IN (:virtualServerTypes) "
				sqlParams['virtualServerTypes'] = AssetType.virtualServerTypes
				break
			case 'physicalServer':
				sqlExpression = " AE.assetType IN (:phyServerTypes) "
				sqlParams['phyServerTypes'] = AssetType.allServerTypes - AssetType.virtualServerTypes
				break
			case 'server':
				sqlExpression = " AE.assetType IN (:allServerTypes) "
				sqlParams['allServerTypes'] = AssetType.allServerTypes
				break
			case 'storage':
				sqlExpression = " AE.assetType IN (:storageTypes) "
				sqlParams['storageTypes'] = AssetType.storageTypes
				break
			case 'virtualServer':
				sqlExpression = " AE.assetType IN (:virtualServerTypes) "
				sqlParams['virtualServerTypes'] = AssetType.virtualServerTypes
				break
			case 'other':
				sqlExpression = " COALESCE(ae.assetType,'') NOT IN  (:nonOtherTypes) "
				sqlParams['nonOtherTypes'] = AssetType.nonOtherTypes
				break
			default:
				throw RuntimeException('Invalid filter definition:' + column.filter)
		}

		return [
			sqlExpression: sqlExpression,
			sqlParams    : sqlParams
		]
	}
}
