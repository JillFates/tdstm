package net.transitionmanager.service.dataview


import net.transitionmanager.asset.AssetType
import net.transitionmanager.service.dataview.ExtraFilter
import spock.lang.Specification
import spock.lang.Unroll

/**
 * TM-14768. It builds custom filters for Planning Dashboard defined by
 * https://docs.google.com/spreadsheets/d/11_DjdACYvy5IB7Zup3VH4mb6pChF1NgAnjHH2rgaSQ8/edit?ts=5cb64b90#gid=1016467595
 */
class ExtraFilterSpec extends Specification {

	@Unroll
	void 'test can prepare hql where statement and params for extra filters #namedFilter'() {

		setup: 'an instance of ExtraFilter created'
			ExtraFilter extraFilter = new ExtraFilter('_filter', namedFilter)

		expect:
			extraFilter.buildQueryNamedFilter() == [
				hqlExpression: hqlExpression,
				hqlParams    : hqlParams
			]

		where:
			namedFilter      || hqlExpression                                                         | hqlParams
			'physical'       || " COALESCE(AE.assetType,'') NOT IN (:namedFilterVirtualServerTypes) " | ['namedFilterVirtualServerTypes': AssetType.virtualServerTypes]
			'physicalServer' || " AE.assetType IN (:namedFilterPhyServerTypes) "                      | ['namedFilterPhyServerTypes': AssetType.allServerTypes - AssetType.virtualServerTypes]
			'server'         || " AE.assetType IN (:namedAllServerTypes) "                            | ['namedAllServerTypes': AssetType.allServerTypes]
			'storage'        || " AE.assetType IN (:namedStorageTypes) "                              | ['namedStorageTypes': AssetType.storageTypes]
			'virtualServer'  || " AE.assetType IN (:namedFilterVirtualServerTypes) "                  | ['namedFilterVirtualServerTypes': AssetType.virtualServerTypes]
			'other'          || " COALESCE(ae.assetType,'') NOT IN  (:namedFilterNonOtherTypes) "     | ['namedFilterNonOtherTypes': AssetType.nonOtherTypes]

	}

	void 'test can prepare hql where statement nd params for extra filters _moveBundle'() {

		given: 'a Map with extra fields defined by ?_moveBundle=5689 in url params'
			ExtraFilter extraFilter = new ExtraFilter('_moveBundle', '5689')

		when: 'builds results for extra filters'
			Map<String, ?> results = extraFilter.buildHQLQueryAndParams()

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.moveBundle.id = (:extraFilterMoveBundle) "
			results.hqlParams == [extraFilterMoveBundle: 5689]
	}

	void 'test can prepare hql where statement and params for extra filters using event filter'() {

		given: 'an extra field defined defined by ?_event=329 in url params'
			ExtraFilter extraFilter = new ExtraFilter('_event', '329')

		when: 'builds results for extra filters'
			Map<String, ?> results = extraFilter.buildHQLQueryAndParams()

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.moveBundle.moveEvent.id = :extraFilterMoveEventId "
			results.hqlParams['extraFilterMoveEventId'] == 329
	}
}
