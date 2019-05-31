package net.transitionmanager.service.dataview

import com.tdssrc.grails.NumberUtil
import groovy.transform.CompileStatic
import net.transitionmanager.asset.AssetType
import net.transitionmanager.dataview.FieldSpec

/**
 * Defines extra filter structure for {@code Dataview}
 */
@CompileStatic
class ExtraFilter {

	String domain
	/**
	 * This field defines a property for extra filters.
	 * It could have different meaning based on the following rules: <br/>
	 * 1)If it is a valid field spec name, it could be used for filtering using filter value
	 * <pre>
	 *{"property" : "assetName", "filter": "FOOBAR"}* </pre>
	 * 2) If it is a specific named filter
	 * <pre>
	 *{"property" : "_filter", "filter": "physicalServer"}*{"property" : "_event", "filter": "364"}*{"property" : "_ufp", "filter": "true"}* </pre>
	 * @see ExtraFilterBuilder#build(java.util.List, net.transitionmanager.dataview.FieldSpecProject)
	 */
	String property
	String filter
	/**
	 * Field used to to validate filters like:
	 * "moveBundle.id" or "sme.id"
	 */
	String referenceProperty
	FieldSpec fieldSpec

	boolean isAssetField() {
		return this.fieldSpec != null
	}

	/**
	 * Builds HQL sentence and HQL params to be used in {@code DataviewService}
	 * @return A Map with 2 pair of key/value.
	 * 		One for HQL sentence and the other one for hql params
	 */
	Map buildHQLQueryAndParams() {
		String hqlExpression
		Map<String, ?> hqlParams

		switch (this.property) {
			case '_moveBundle':
				hqlExpression = " AE.moveBundle.id = (:extraFilterMoveBundle) "
				hqlParams = [
					extraFilterMoveBundle: NumberUtil.toPositiveLong(this.filter, 0)
				]
				break
			case '_event':
				hqlExpression = " AE.moveBundle.moveEvent.id = :extraFilterMoveEventId "
				hqlParams = [
					extraFilterMoveEventId: NumberUtil.toPositiveLong(this.filter, 0)
				]
				break
			case '_filter':
				Map<String, ?> namedFilterResults = buildQueryNamedFilter()
				hqlExpression = namedFilterResults.hqlExpression
				hqlParams = (Map<String, ?>)namedFilterResults.hqlParams
				break
			default:
				throw new RuntimeException('Invalid filter definition:' + this.property)
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
	 * @return a Map with 2 values, sqlExpression and sqlParams
	 * 			to be used in an hql sentence
	 */
	Map<String, ?> buildQueryNamedFilter() {
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
				hqlExpression = " COALESCE(ae.assetType,'') NOT IN  (:namedFilterNonOtherTypes) "
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

	/**
	 * Creates a instance of {@ExtraFilterBuilder} to create
	 * new instances of {@code ExtraBuilder} from url params
	 * in Planning Dashboard and ASsets Summary
	 * @return an instance of {@ExtraFilterBuilder}
	 */
	static ExtraFilterBuilder builder() {
		return new ExtraFilterBuilder()
	}

}

