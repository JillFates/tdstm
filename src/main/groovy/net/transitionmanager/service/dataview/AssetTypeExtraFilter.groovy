package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic
import net.transitionmanager.asset.AssetType
import net.transitionmanager.project.Project

@CompileStatic
class AssetTypeExtraFilter extends NamedExtraFilter implements ExtraFilterHqlGenerator {

	/**
	 * <p>Prepares a named filter used by UI for filtering assets using business rules about assets.
	 * For example, filtering by 'physicalServer' or 'storage' in asset types.</p>
	 *
	 * @return a Map with 2 values, hqlExpression and hqlParams
	 * 			to be used in an hql sentence
	 */
	@Override
	Map<String, ?> generateHQL(Project project) {

		String hqlExpression
		Map<String, ?> hqlParams

		switch (this.filter) {
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
				hqlExpression = " COALESCE(AE.assetType,'') NOT IN (:namedFilterNonOtherTypes) "
				hqlParams = ['namedFilterNonOtherTypes': AssetType.nonOtherTypes]
				break
			default:
				throw new RuntimeException('Invalid filter definition:' + this.property)
		}

		return [
			hqlExpression: hqlExpression,
			hqlParams    : hqlParams
		]
	}
}
