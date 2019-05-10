package net.transitionmanager.service

import net.transitionmanager.asset.AssetType
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project

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

		if (domain == 'common' && property == 'assetName') {

			hqlExpression = " AE.assetName like :extraFilterAssetName "
			hqlParams = [extraFilterAssetName: "%$filter%"]

		} else if (domain == 'common' && property == 'validation') {

			hqlExpression = " AE.assetName like :extraFilterAssetName "

			hqlParams = [extraFilterAssetName: "%$filter%"]

		} else {

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
			default:
				throw new RuntimeException('Invalid filter definition:' + namedFilter)
		}

		return [
			hqlExpression: hqlExpression,
			hqlParams    : hqlParams
		]
	}
}
