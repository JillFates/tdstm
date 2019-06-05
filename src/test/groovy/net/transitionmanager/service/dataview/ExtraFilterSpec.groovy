package net.transitionmanager.service.dataview

import com.tdsops.etl.FieldSpecValidateableTrait
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.AssetType
import net.transitionmanager.common.Timezone
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * TM-14768. It builds custom filters for Planning Dashboard defined by
 * https://docs.google.com/spreadsheets/d/11_DjdACYvy5IB7Zup3VH4mb6pChF1NgAnjHH2rgaSQ8/edit?ts=5cb64b90#gid=1016467595
 */
class ExtraFilterSpec extends Specification implements FieldSpecValidateableTrait, DataTest, AssertionTest {

	@Shared
	Project defaultProject

	@Shared
	FieldSpecProject fieldSpecProject

	void setupSpec() {
		mockDomains(Project, Person)
	}

	void setup() {
		defaultProject = new Project()
		defaultProject.with {
			client = new PartyGroup(name: RandomStringUtils.randomAlphabetic(10))
			projectCode = RandomStringUtils.randomAlphabetic(10)
			name = 'Project ' + projectCode
			description = 'Test project created by the ProjectTestHelper'
			startDate = new Date()
			completionDate = startDate + 30
			guid = StringUtil.generateGuid()
			workflowCode = 'STD_PROCESS'
			timezone = Timezone.findByCode('GMT')
			guid = StringUtil.generateGuid()
			planMethodology = 'custom5'
		}
		fieldSpecProject = createFieldSpecProject()
	}

	@Unroll
	void 'test can lookup ExtraFilterName instances by filter #filterName'() {
		expect:
			ExtraFilterName.lookupByName(filterName) == extraFilterName

		where:
			filterName    || extraFilterName
			'_event'      || ExtraFilterName.EVENT
			'_filter'     || ExtraFilterName.FILTER
			'_planMethod' || ExtraFilterName.PLAN_METHOD
			'FOO'         || null
	}


	void 'test can create an ExtraFilter using field name and filter value'() {

		when: 'an instance of ExtraFilter is built by ExtraFilterBuilder'
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('assetName')
				.withFilter('FOO')
				.build(['common', 'application'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, AssetFieldExtraFilter) {
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
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('common_assetName')
				.withFilter('FOO')
				.build(['common', 'application'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, AssetFieldExtraFilter) {
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
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('_event')
				.withFilter('3233')
				.build(['common', 'device'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, EventExtraFilter) {
				property == '_event'
				filter == '3233'
			}
	}

	void 'test can create an ExtraFilter using a referenced property'() {

		when: 'an instance of ExtraFilter is built by ExtraFilterBuilder'
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('moveBundle.id')
				.withFilter('1810')
				.build(['common', 'application'], fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			assertWith(extraFilter, AssetFieldExtraFilter) {
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
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('_filter')
				.withFilter(namedFilter)
				.build(['common', 'application'], fieldSpecProject)
		expect:
			extraFilter.generateHQL(defaultProject) == [
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
			'other'          || " COALESCE(AE.assetType,'') NOT IN (:namedFilterNonOtherTypes) "      | ['namedFilterNonOtherTypes': AssetType.nonOtherTypes]

	}


	void 'test can prepare hql where statement and params for extra filters using event filter'() {

		given: 'an extra field defined defined by ?_event=329 in url params'
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('_event')
				.withFilter('329')
				.build(['common', 'application'], fieldSpecProject)

		when: 'builds results for extra filters'
			Map<String, ?> results = extraFilter.generateHQL(defaultProject)

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.moveBundle.moveEvent.id = :extraFilterMoveEventId "
			results.hqlParams['extraFilterMoveEventId'] == 329
	}

	void 'test can prepare hql where statement and params for extra filters using plan methodology filter Unknown'() {

		given: 'an extra field defined defined by ?_event=329 in url params'
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('_planMethod')
				.withFilter('Unknown')
				.build(['common', 'application'], fieldSpecProject)

		when: 'builds results for extra filters'
			Map<String, ?> results = extraFilter.generateHQL(defaultProject)

		then: 'an hql sentence is created'
			results.hqlExpression == " (AE.custom5 is NULL OR AE.custom5 = '') "
			results.hqlParams == [:]
	}

	void 'test can prepare hql where statement and params for extra filters using plan methodology filter defiend'() {

		given: 'an extra field defined defined by ?_event=329 in url params'
			ExtraFilterHqlGenerator extraFilter = new ExtraFilterBuilder()
				.withProperty('_planMethod')
				.withFilter('FOOBAR')
				.build(['common', 'application'], fieldSpecProject)

		when: 'builds results for extra filters'
			Map<String, ?> results = extraFilter.generateHQL(defaultProject)

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.custom5 = :planMethodology "
			results.hqlParams == [
				planMethodology: 'FOOBAR'
			]
	}

}
