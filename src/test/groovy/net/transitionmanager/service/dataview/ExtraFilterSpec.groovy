package net.transitionmanager.service.dataview

import com.tdsops.etl.FieldSpecValidateableTrait
import net.transitionmanager.asset.AssetType
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.service.dataview.ExtraFilter
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * TM-14768. It builds custom filters for Planning Dashboard defined by
 * https://docs.google.com/spreadsheets/d/11_DjdACYvy5IB7Zup3VH4mb6pChF1NgAnjHH2rgaSQ8/edit?ts=5cb64b90#gid=1016467595
 */
class ExtraFilterSpec extends Specification implements FieldSpecValidateableTrait, AssertionTest {

	@Shared
	FieldSpecProject fieldSpecProject

	void setup() {
		fieldSpecProject = createFieldSpecProject()
	}

	void 'test can create an ExtraFilter using field name and filter value'() {

		when: 'an instance of ExtraFilter is built by ExtraFilterBuilder'
			ExtraFilter extraFilter = ExtraFilter.builder()
				.withProperty('assetName')
				.withFilter('FOO')
				.build(['common', 'application'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, ExtraFilter) {
				domain == 'common'
				property == 'assetName'
				filter == 'FOO'
				referenceProperty == null
				assertWith(fieldSpec, FieldSpec) {
					field == 'assetName'
					label == 'Name'
				}
			}
	}

	void 'test can create an ExtraFilter using domain, field name and filter value'() {

		when: 'an instance of ExtraFilter is built by ExtraFilterBuilder'
			ExtraFilter extraFilter = ExtraFilter.builder()
				.withProperty('common_assetName')
				.withFilter('FOO')
				.build(['common', 'application'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, ExtraFilter) {
				domain == 'common'
				property == 'assetName'
				filter == 'FOO'
				referenceProperty == null
				assertWith(fieldSpec, FieldSpec) {
					field == 'assetName'
					label == 'Name'
				}
			}
	}

	void 'test can create an ExtraFilter using a custom property'() {
		when: 'an instance of ExtraFilter is built by ExtraFilterBuilder'
			ExtraFilter extraFilter = ExtraFilter.builder()
				.withProperty('_event')
				.withFilter('3233')
				.build(['common', 'device'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, ExtraFilter) {
				domain == null
				property == '_event'
				filter == '3233'
				referenceProperty == null
				fieldSpec == null
			}
	}

	void 'test can create an ExtraFilter using a referenced property'() {

		when: 'an instance of ExtraFilter is built by ExtraFilterBuilder'
			ExtraFilter extraFilter = ExtraFilter.builder()
				.withProperty('moveBundle.id')
				.withFilter('1810')
				.build(['common', 'application'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, ExtraFilter) {
				domain == 'common'
				property == 'moveBundle'
				filter == 'FOO'
				referenceProperty == 'id'
				assertWith(fieldSpec, FieldSpec) {
					field == 'moveBundle'
					label == 'Bundle'
				}
			}
	}


	@Unroll
	void 'test can prepare hql where statement and params for extra filters #namedFilter'() {

		setup: 'an instance of ExtraFilter created'
			ExtraFilter extraFilter = ExtraFilter.builder()
				.withProperty('_filter')
				.withFilter(namedFilter)
				.build(['common', 'application'], fieldSpecProject)
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


	void 'test can prepare hql where statement and params for extra filters using event filter'() {

		given: 'an extra field defined defined by ?_event=329 in url params'
			ExtraFilter extraFilter = ExtraFilter.builder()
				.withProperty('_event')
				.withFilter('329')
				.build(['common', 'application'], fieldSpecProject)

		when: 'builds results for extra filters'
			Map<String, ?> results = extraFilter.buildHQLQueryAndParams()

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.moveBundle.moveEvent.id = :extraFilterMoveEventId "
			results.hqlParams['extraFilterMoveEventId'] == 329
	}
}
