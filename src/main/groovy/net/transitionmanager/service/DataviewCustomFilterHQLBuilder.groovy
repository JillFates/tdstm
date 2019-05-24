package net.transitionmanager.service

import com.tdsops.common.sql.SqlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.asset.AssetType
import net.transitionmanager.imports.DataviewHqlWhereCollector
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.search.FieldSearchData
import net.transitionmanager.service.dataview.ExtraFilter

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
	 *
	 * @param extraFilter * @return
	 */
	Map buildQueryExtraFilters(ExtraFilter extraFilter) {
		String hqlExpression
		Map<String, ?> hqlParams

		switch (extraFilter.property) {
			case '_moveBundle':
				hqlExpression = " AE.moveBundle.id = (:extraFilterMoveBundle) "
				hqlParams = [
					extraFilterMoveBundle: NumberUtil.toPositiveLong(extraFilter.filter, 0)
				]
				break
			case '_event':
				hqlExpression = " AE.moveBundle.moveEvent.id = :extraFilterMoveEventId "
				hqlParams = [
					extraFilterMoveEventId: NumberUtil.toPositiveLong(extraFilter.filter, 0)
				]
				break
			case '_filter':
				Map<String, ?> namedFilterResults = buildQueryNamedFilter(extraFilter)
				hqlExpression = namedFilterResults.hqlExpression
				hqlParams = namedFilterResults.hqlParams
				break
			default:
				throw new RuntimeException('Invalid filter definition:' + extraFilter.property)
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
	public Map<String, ?> buildQueryNamedFilter(ExtraFilter extraFilter) {
		String hqlExpression
		Map<String, ?> hqlParams

		switch (extraFilter.filter) {
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
				throw new RuntimeException('Invalid filter definition:' + extraFilter.property)
		}

		return [
			hqlExpression: hqlExpression,
			hqlParams    : hqlParams
		]
	}
}
