package net.transitionmanager.service.dataview

import com.tdsops.etl.FieldSpecValidateableTrait
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import net.transitionmanager.command.dataview.DataviewUserParamsCommand
import net.transitionmanager.common.Timezone
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.imports.Dataview
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.service.dataview.filter.FieldNameExtraFilter
import net.transitionmanager.service.dataview.filter.special.AssetTypeExtraFilter
import net.transitionmanager.service.dataview.filter.special.SpecialExtraFilter
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
			timezone = Timezone.findByCode('GMT')
			guid = StringUtil.generateGuid()
		}
		defaultProject.save()
		fieldSpecProject = createFieldSpecProject()
	}

	void 'test can create a DataviewSpec'() {

		given: 'an instance of DataviewUserParamsCommand'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, null, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
	}

	void 'test can create a DataviewSpec from a Dataview'() {

		given: 'an instance of DataviewUserParamsCommand'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewReportSchema
			)

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
	}

	void 'test can create a DataviewSpec from a Dataview with extra filters physicalServer'() {

		given: 'an instance of DataviewUserParamsCommand with named filters added'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: '_assetType',
					filter  : 'physicalServer'
				]
			]

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewReportSchema
			)

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]

		and: 'extra filter where created'
			dataviewSpec.fieldNameExtraFilters.size() == 0
			dataviewSpec.specialExtraFilters.size() == 1
			dataviewSpec.specialExtraFilters.each { SpecialExtraFilter extraFilter ->
				with(extraFilter, AssetTypeExtraFilter) {
					property == '_assetType'
					filter == 'physicalServer'
				}
			}
	}

	void 'test can create a DataviewSpec from a Dataview with extra filters using asset field name defined'() {

		given: 'an instance of DataviewUserParamsCommand with named filters and extra filters added'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: 'assetName',
					filter  : '111-222-333'
				]
			]

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewReportSchema
			)

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]

		and: 'extra filter where created'
			dataviewSpec.fieldNameExtraFilters.size() == 1
			dataviewSpec.fieldNameExtraFilters.each { FieldNameExtraFilter extraFilter ->
				with(extraFilter, FieldNameExtraFilter) {
					property == 'assetName'
					filter == '111-222-333'
					with(fieldSpec, FieldSpec) {
						field == 'assetName'
						label == 'Name'
					}
				}
			}
	}

	void 'test can create a DataviewSpec from a Dataview with extra filters using asset field name and domain defined'() {

		given: 'an instance of DataviewUserParamsCommand with named filters and extra filters added'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: 'common_assetName',
					filter  : '111-222-333'
				]
			]

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewReportSchema
			)

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]

		and: 'extra filter where created'
			dataviewSpec.fieldNameExtraFilters.size() == 1
			dataviewSpec.fieldNameExtraFilters.each { FieldNameExtraFilter extraFilter ->
				with(extraFilter, FieldNameExtraFilter) {
					property == 'assetName'
					filter == '111-222-333'
					with(fieldSpec, FieldSpec) {
						field == 'assetName'
						label == 'Name'
					}
				}
			}
	}

	void 'test no exception is thrown when creating a DataviewSpec with extra incorrect filters'() {

		given: 'an instance of DataviewUserParamsCommand with named filters added'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: 'application_assetName',
					filter  : '111-222-333'
				]
			]

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewReportSchema
			)

		when: 'a dataviewSpec is created'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'no exception is thrown'
			noExceptionThrown()
		and: 'the dataviewSpec is not null'
			dataviewSpec != null
	}

	void 'test no exception is thrown when creating a DataviewSpec with extra unknown filters'() {

		given: 'an instance of DataviewUserParamsCommand with named filters added'
			DataviewUserParamsCommand command = allAssetsDataviewMap as DataviewUserParamsCommand
			command.filters.extra = [
				[
					property: 'unknown',
					filter  : 'unknown'
				]
			]

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssetsDataviewReportSchema
			)

		when: 'a dataviewSpec is created'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)
		then: 'no exception is thrown'
			noExceptionThrown()
		and: 'the dataviewSpec is not null'
			dataviewSpec != null
	}
}
