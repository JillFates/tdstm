package net.transitionmanager.service.dataview

import com.tdsops.etl.FieldSpecValidateableTrait
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.common.Timezone
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.imports.Dataview
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification

class DataviewSpecSpec extends Specification implements FieldSpecValidateableTrait, DataTest, AllAssetsFilterUnitTest {

	@Shared
	Project defaultProject
	@Shared
	FieldSpecProject fieldSpecProject

	void setupSpec() {
		mockDomains(Project, Person, Dataview)
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
		fieldSpecProject = createFieldSpecProject()
	}

	void 'test can create a DataviewSpec from DataviewUserParamsCommand'() {

		given: 'an instance of DataviewUserParamsCommand'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, null, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
	}

	void 'test can create a DataviewSpec from DataviewUserParamsCommand and a Dataview'() {

		given: 'an instance of DataviewUserParamsCommand'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewJsonContent
			)
			//dataview.save()

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
	}

	void 'test can create a DataviewSpec from DataviewUserParamsCommand and a Dataview adding named filters'() {

		given: 'an instance of DataviewUserParamsCommand with named filters added'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand
			// Filter on UI like this: ?_filter=physicalServer
			command.filters.named = 'physicalServer,validateTo'

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewJsonContent
			)

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
			dataviewSpec.namedFilters == ['physicalServer', 'validateTo']
	}

	void 'test can create a DataviewSpec from DataviewUserParamsCommand and a Dataview adding named filters and extra filters'() {

		given: 'an instance of DataviewUserParamsCommand with named filters and extra filters added'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand
			command.filters.named = 'physicalServer,validateTo'
			command.filters.extra = [
				[
					domain  : 'common',
					filter  : 'FOO',
					property: 'assetName'
				]
			]

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewJsonContent
			)

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
			dataviewSpec.namedFilters == ['physicalServer', 'validateTo']
			dataviewSpec.extraFilters == [
				[
					domain  : 'common',
					filter  : 'FOO',
					property: 'assetName'
				]
			]
	}

}
