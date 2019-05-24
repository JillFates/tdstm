package net.transitionmanager.service

import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.AssetType
import net.transitionmanager.common.Timezone
import net.transitionmanager.imports.Dataview
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.service.dataview.ExtraFilter
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DataviewCustomFilterHQLBuilderSpec extends Specification implements DataTest {

	@Shared
	Project defaultProject

	void setupSpec() {
		mockDomains(Project, Dataview, MoveEvent)
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
		}
		defaultProject.save()
	}

	@Unroll
	void 'test can prepare hql where statement and params for named filters #namedFilter'() {

		setup: 'and instance of DataviewCustomFilterHQLBuilder'
			DataviewCustomFilterHQLBuilder builder = new DataviewCustomFilterHQLBuilder(defaultProject)
		and: 'an instance of ExtraFilter created'
			ExtraFilter extraFilter = new ExtraFilter('_filter', namedFilter)

		expect:
			builder.buildQueryNamedFilter(extraFilter) == [
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

	void 'test can prepare hql where statement nd params for extra filters using property ufp'() {

		given: 'and instance of DataviewCustomFilterHQLBuilder'
			DataviewCustomFilterHQLBuilder builder = new DataviewCustomFilterHQLBuilder(defaultProject)

		and: 'a Map with extra fields defined by ?_ufp=true url params'
			ExtraFilter extraFilter = new ExtraFilter('_moveBundle', '5689')

		when: 'builds results for extra filters'
			Map<String, ?> results = builder.buildQueryExtraFilters(extraFilter)

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.moveBundle.id = (:extraFilterMoveBundle) "
			results.hqlParams == [extraFilterMoveBundles: [5689l]]
	}

	void 'test can prepare hql where statement and params for extra filters using event filter'() {

		given: 'and instance of DataviewCustomFilterHQLBuilder'
			DataviewCustomFilterHQLBuilder builder = new DataviewCustomFilterHQLBuilder(defaultProject)

		and: 'an extra field defined'
			ExtraFilter extraFilter = new ExtraFilter('_event', '329')

		when: 'builds results for extra filters'
			Map<String, ?> results = builder.buildQueryExtraFilters(extraFilter)

		then: 'an hql sentence is created'
			results.hqlExpression == " AE.moveBundle.event.id = :extraFilterMoveEventId "
			results.hqlParams['extraFilterMoveEventId'] == 329

	}
}
