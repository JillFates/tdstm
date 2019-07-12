package net.transitionmanager.service.dataview

import com.tdsops.etl.FieldSpecValidateableTrait
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.AssetType
import net.transitionmanager.command.dataview.DataviewUserParamsCommand
import net.transitionmanager.common.Timezone
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.service.dataview.filter.ExtraFilterType
import net.transitionmanager.service.dataview.filter.FieldNameExtraFilter
import net.transitionmanager.service.dataview.filter.special.AssetTypeExtraFilter
import net.transitionmanager.service.dataview.filter.special.EventExtraFilter
import net.transitionmanager.service.dataview.filter.special.PlanMethodExtraFilter
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * TM-14768. It builds custom filters for Planning Dashboard defined by
 * https://docs.google.com/spreadsheets/d/11_DjdACYvy5IB7Zup3VH4mb6pChF1NgAnjHH2rgaSQ8/edit?ts=5cb64b90#gid=1016467595
 */
class ExtraFilterSpec extends Specification implements FieldSpecValidateableTrait, AllAssetsFilterUnitTest, DataTest {

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
	void 'test can lookup ExtraFilterType instances by filter #filterType'() {
		expect:
			ExtraFilterType.lookupByName(filterType) == extraFilterType

		where:
			filterType    || extraFilterType
			'_event'      || ExtraFilterType.EVENT
			'_assetType'  || ExtraFilterType.ASSET_TYPE
			'_planMethod' || ExtraFilterType.PLAN_METHOD
			'FOO'         || null
	}


	void 'test can create an ExtraFilter using field name and filter value'() {

		given:
			DataviewUserParamsCommand command = applicationsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: 'assetName',
					filter  : 'FOO'
				]
			]
		when:
			DataviewSpec dataviewSpec = new DataviewSpec(command, null, fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			dataviewSpec.specialExtraFilters.size() == 0
			dataviewSpec.fieldNameExtraFilters.size() == 1
			with(dataviewSpec.fieldNameExtraFilters[0], FieldNameExtraFilter) {
				domain == 'common'
				property == 'assetName'
				filter == 'FOO'
				referenceProperty == null
				with(fieldSpec, FieldSpec) {
					field == 'assetName'
					label == 'Name'
				}
			}
	}

	void 'test can create an ExtraFilter using domain, field name and filter value'() {

		given:
			DataviewUserParamsCommand command = applicationsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: 'common_assetName',
					filter  : 'FOO'
				]
			]

		when:
			DataviewSpec dataviewSpec = new DataviewSpec(command, null, fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			dataviewSpec.specialExtraFilters.size() == 0
			dataviewSpec.fieldNameExtraFilters.size() == 1
			with(dataviewSpec.fieldNameExtraFilters[0], FieldNameExtraFilter) {
				domain == 'common'
				property == 'assetName'
				filter == 'FOO'
				referenceProperty == null
				with(fieldSpec, FieldSpec) {
					field == 'assetName'
					label == 'Name'
				}
			}
	}

	void 'test can create an ExtraFilter using a special property'() {

		given:
			DataviewUserParamsCommand command = applicationsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: '_event',
					filter  : '3233'
				]
			]

		when:
			DataviewSpec dataviewSpec = new DataviewSpec(command, null, fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			dataviewSpec.specialExtraFilters.size() == 1
			dataviewSpec.fieldNameExtraFilters.size() == 0
			with(dataviewSpec.specialExtraFilters[0], EventExtraFilter) {
				property == '_event'
				filter == '3233'
			}
	}

	void 'test can create an ExtraFilter using a referenced property'() {

		given:
			DataviewUserParamsCommand command = applicationsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: 'moveBundle.id',
					filter  : '1810'
				]
			]

		when:
			DataviewSpec dataviewSpec = new DataviewSpec(command, null, fieldSpecProject)

		then: 'it contains a property name, filter and fieldSpec associated'
			dataviewSpec.specialExtraFilters.size() == 0
			dataviewSpec.fieldNameExtraFilters.size() == 1
			with(dataviewSpec.fieldNameExtraFilters[0], FieldNameExtraFilter) {
				domain == 'common'
				property == 'moveBundle'
				filter == '1810'
				referenceProperty == 'id'
				with(fieldSpec, FieldSpec) {
					field == 'moveBundle'
					label == 'Bundle'
				}
			}
	}


	@Unroll
	void 'test can prepare hql where statement and params for extra filters #assetTypeFilter'() {

		setup: 'an instance of ExtraFilter created'
			AssetTypeExtraFilter assetTypeExtraFilter = new AssetTypeExtraFilter(
				property: '_assetType',
				filter: assetTypeFilter
			)

		expect:
			assetTypeExtraFilter.generateHQL(defaultProject) == [
				hqlExpression: hqlExpression,
				hqlParams    : hqlParams
			]

		where:
			assetTypeFilter      || hqlExpression                                                         | hqlParams
			'physical'       || " COALESCE(AE.assetType,'') NOT IN (:namedFilterVirtualServerTypes) " | ['namedFilterVirtualServerTypes': AssetType.virtualServerTypes]
			'physicalServer' || " AE.assetType IN (:namedFilterPhyServerTypes) "                      | ['namedFilterPhyServerTypes': AssetType.allServerTypes - AssetType.virtualServerTypes]
			'server'         || " AE.assetType IN (:namedAllServerTypes) "                            | ['namedAllServerTypes': AssetType.allServerTypes]
			'storage'        || " AE.assetType IN (:namedStorageTypes) "                              | ['namedStorageTypes': AssetType.storageTypes]
			'virtualServer'  || " AE.assetType IN (:namedFilterVirtualServerTypes) "                  | ['namedFilterVirtualServerTypes': AssetType.virtualServerTypes]
			'other'          || " COALESCE(AE.assetType,'') NOT IN (:namedFilterNonOtherTypes) "      | ['namedFilterNonOtherTypes': AssetType.nonOtherTypes]

	}


	void 'test can prepare hql where statement and params for extra filters using event filter'() {

		given: 'an extra field defined defined by ?_event=329 in url params'
			EventExtraFilter eventExtraFilter = new EventExtraFilter(
				property: '_event',
				filter: '329'
			)

		when: 'builds results for extra filters'
			Map<String, ?> results = eventExtraFilter.generateHQL(defaultProject)

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.moveBundle.moveEvent.id = :extraFilterMoveEventId "
			results.hqlParams['extraFilterMoveEventId'] == 329
	}

	void 'test can prepare hql where statement and params for extra filters using plan methodology filter Unknown'() {

		given: 'an extra field defined defined by ?_planMethod=Unknown in url params'
			PlanMethodExtraFilter planMethodExtraFilter = new PlanMethodExtraFilter(
				property: '_planMethod',
				filter: 'Unknown'
			)

		when: 'builds results for extra filters'
			Map<String, ?> results = planMethodExtraFilter.generateHQL(defaultProject)

		then: 'an hql sentence is created'
			results.hqlExpression == " COALESCE(AE.custom5, '') = '' "
			results.hqlParams == [:]
	}

	void 'test can prepare hql where statement and params for extra filters using plan methodology filter defiend'() {

		given: 'an extra field defined defined by ?_event=329 in url params'

			PlanMethodExtraFilter planMethodExtraFilter = new PlanMethodExtraFilter(
				property: '_planMethod',
				filter: 'FOOBAR'
			)

		when: 'builds results for extra filters'
			Map<String, ?> results = planMethodExtraFilter.generateHQL(defaultProject)

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.custom5 = :planMethodology "
			results.hqlParams == [
				planMethodology: 'FOOBAR'
			]
	}

}
