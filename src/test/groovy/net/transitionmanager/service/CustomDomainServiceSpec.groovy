package net.transitionmanager.service

import com.tds.asset.Application
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import net.transitionmanager.domain.Timezone
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification

class CustomDomainServiceSpec extends Specification implements ServiceUnitTest<CustomDomainService>, DataTest {

	@Shared
	Project defaultProject
	@Shared
	FieldSpecProject fieldSpecProject

	void setupSpec(){
		mockDomains Application, Project, Setting
	}

	void setup() {
		service.settingService = [getAsMap: {Project project, SettingType type, String key -> applicationJsonFieldMap} ] as SettingService
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

		defaultProject.save(failOnError: true)

		[
			'Application': applicationJsonFieldSpec,
			'Device'     : '{"fields":[],"domain":"DEVICE"}',
			'Database'   : '{"fields":[],"domain":"DATABASE"}',
			'Storage'    : '{"fields":[],"domain":"STORAGE"}'
		].each {
			Setting setting = new Setting()
			setting.project = defaultProject
			setting.type = SettingType.CUSTOM_DOMAIN_FIELD_SPEC
			setting.key = it.key.toUpperCase()
			setting.json = it.value
			setting.save(failOnError: true)
		}

		fieldSpecProject = service.createFieldSpecProject(defaultProject)
	}

	void 'test can set default custom field values for an instance of Application Asset'() {

		given: 'an instance of an asset entity hierarchy'
			Class domainClass = Application
			Object entity = domainClass.newInstance()
			entity.project = defaultProject

		when: 'service set default field values'
			entity = service.setFieldsDefaultValue(fieldSpecProject, domainClass, entity)

		then: 'entity contains correct default value defined in field spec'
			entity.custom6 == 'Yes'
	}

	private static final String applicationJsonFieldSpec = """
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
		   "domain":"APPLICATION"
		}"""


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
