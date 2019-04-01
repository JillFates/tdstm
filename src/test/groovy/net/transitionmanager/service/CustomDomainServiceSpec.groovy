package net.transitionmanager.service

import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.SettingService
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import net.transitionmanager.common.Setting
import net.transitionmanager.common.Timezone
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class CustomDomainServiceSpec extends Specification implements ServiceUnitTest<CustomDomainService>, DataTest {

	@Shared
	Project          defaultProject
	@Shared
	FieldSpecProject fieldSpecProject

	void setupSpec() {
		mockDomains Application, Project, Setting
	}

	void setup() {
		service.settingService = [getAsMap: { Project project, SettingType type, String key -> applicationJsonFieldMap }] as SettingService
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

		[
			'Application': createJSONFieldSpecFor(AssetClass.APPLICATION),
			'Device'     : createJSONFieldSpecFor(AssetClass.DEVICE),
			'Database'   : createJSONFieldSpecFor(AssetClass.DATABASE),
			'Storage'    : createJSONFieldSpecFor(AssetClass.STORAGE),
		].each {
			Setting setting = new Setting()
			setting.project = defaultProject
			setting.type = SettingType.CUSTOM_DOMAIN_FIELD_SPEC
			setting.key = it.key.toUpperCase()
			setting.json = it.value
			setting.save()
		}

		fieldSpecProject = service.createFieldSpecProject(defaultProject)
	}

	@Unroll
	void 'test can set default custom field values for an instance of #domainClass'() {

		setup: 'an instance of an asset entity hierarchy'
			Object entity = domainClass.newInstance()
			entity.project = defaultProject

		expect: 'service set default field values'
			service.setCustomFieldsDefaultValue(fieldSpecProject, domainClass, entity).custom6 == fieldDefaultValue

		where:
			domainClass || fieldDefaultValue
			Application || 'Yes'
			AssetEntity || 'Yes'
			Database    || 'Yes'
			Files       || 'Yes'
	}


	private static String createJSONFieldSpecFor(AssetClass assetClass) {
		return """
		{
		   "planMethodology":"",
		   "fields":[
			  {
				 "bulkChangeActions":[
					"replace"
				 ],
				 "constraints":{
					"required":1,
					"values":[
					   "Yes",
					   "No"
					]
				 },
				 "control":"YesNo",
				 "default":"Yes",
				 "field":"custom6",
				 "imp":"B",
				 "label":"Free",
				 "order":23,
				 "shared":0,
				 "show":1,
				 "udf":1
			  }
		   ],
		   "domain":"${assetClass.name()}"
		}"""
	}

	private static final Map applicationJsonFieldMap = [
		"planMethodology": "",
		"fields"         : [
			[
				"bulkChangeActions": [
					"replace"
				],
				"constraints"      : [
					"required": 1,
					"values"  : [
						"Yes",
						"No"
					]
				],
				"control"          : "YesNo",
				"default"          : "Yes",
				"field"            : "custom6",
				"imp"              : "B",
				"label"            : "Free",
				"order"            : 23,
				"shared"           : 0,
				"show"             : 1,
				"udf"              : 1
			]
		],
		"domain"         : "APPLICATION"
	]
}
