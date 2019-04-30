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

class DataviewSpecSpec extends Specification implements FieldSpecValidateableTrait, DataTest {

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
			DataviewUserParamsCommand command = allAssetDataviewDefinition as DataviewUserParamsCommand

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, null, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
	}

	void 'test can create a DataviewSpec from DataviewUserParamsCommand and a Dataview'() {

		given: 'an instance of DataviewUserParamsCommand'
			DataviewUserParamsCommand command = allAssetDataviewDefinition as DataviewUserParamsCommand

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssets
			)
			//dataview.save()

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
	}

	void 'test can create a DataviewSpec from DataviewUserParamsCommand and a Dataview adding custom UI filters'() {

		given: 'an instance of DataviewUserParamsCommand'
			DataviewUserParamsCommand command = allAssetDataviewDefinition as DataviewUserParamsCommand
			command.filters.columns.add([
				"domain"  : "database",
				"edit"    : false,
				"filter"  : "physicalServer",
				"label"   : "_filters",
				"locked"  : true,
				"property": "_filters",
				"width"   : 220
			])

		and: 'an instance of Dataview'
			Dataview dataview = new Dataview(
				project: defaultProject,
				name: 'ALL ASSETS',
				isSystem: false,
				isShared: false,
				reportSchema: allAssets
			)

		when: 'a dataviewSpec is created using only that instance of DataviewUserParamsCommand'
			DataviewSpec dataviewSpec = new DataviewSpec(command, dataview, fieldSpecProject)

		then: 'dataviewSpec is created correctly'
			dataviewSpec.spec.domains == ["common", "application", "database", "device", "storage"]
			dataviewSpec.spec.columns.findAll { it.property == DataviewSpec.CUSTOM_FILTER }*.filter == ['physicalServer']
	}

	@Shared
	String allAssets = """
		{
		   "offset":0,
		   "limit":25,
		   "sortDomain":"common",
		   "sortProperty":"assetName",
		   "sortOrder":"a",
		   "filters":{
			  "domains":[
				 "common",
				 "application",
				 "database",
				 "device",
				 "storage"
			  ],
			  "columns":[
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Name",
					"locked":true,
					"property":"assetName",
					"width":220
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Asset Class",
					"locked":true,
					"property":"assetClass",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Description",
					"locked":false,
					"property":"description",
					"width":220
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Environment",
					"locked":false,
					"property":"environment",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Bundle",
					"locked":false,
					"property":"moveBundle",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Tags",
					"locked":false,
					"property":"tagAssets",
					"width":220
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Validation",
					"locked":false,
					"property":"validation",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Plan Status",
					"locked":false,
					"property":"planStatus",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Support",
					"locked":false,
					"property":"supportType",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"External Ref Id",
					"locked":false,
					"property":"externalRefId",
					"width":140
				 },
				 {
					"domain":"common",
					"edit":false,
					"filter":"",
					"label":"Modified Date",
					"locked":false,
					"property":"lastUpdated",
					"width":140
				 }
			  ]
		   }
		}"""

	@Shared
	Map<String, ?> allAssetDataviewDefinition = [
		"offset"      : 0,
		"limit"       : 25,
		"sortDomain"  : "common",
		"sortProperty": "assetName",
		"sortOrder"   : "a",
		"filters"     : [
			"domains": ["common", "application", "database", "device", "storage"],
			"columns": [
				["domain": "common", "edit": false, "filter": "", "label": "Name", "locked": true, "property": "assetName", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Asset Class", "locked": true, "property": "assetClass", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Description", "locked": false, "property": "description", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Environment", "locked": false, "property": "environment", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Bundle", "locked": false, "property": "moveBundle", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Tags", "locked": false, "property": "tagAssets", "width": 220],
				["domain": "common", "edit": false, "filter": "", "label": "Validation", "locked": false, "property": "validation", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Plan Status", "locked": false, "property": "planStatus", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Support", "locked": false, "property": "supportType", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "External Ref Id", "locked": false, "property": "externalRefId", "width": 140],
				["domain": "common", "edit": false, "filter": "", "label": "Modified Date", "locked": false, "property": "lastUpdated", "width": 140]
			]
		]
	]
}
