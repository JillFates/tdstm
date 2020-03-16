package net.transitionmanager.service

import com.tdsops.tm.enums.ControlType
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.FieldSpecsCacheService
import net.transitionmanager.asset.Files
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.Setting
import net.transitionmanager.common.SettingService
import net.transitionmanager.common.Timezone
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

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

		service.fieldSpecsCacheService = Mock(FieldSpecsCacheService)
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


	@ConfineMetaClassChanges([CustomDomainService])
	@Unroll
	void "test updateFieldData from #oldFieldSpec.control to #newFieldSpec.control"() {

		setup: 'given flags to track data change calls, and meta class overrides for the calls'
			boolean dataDateToDateTimeCalled = false
			boolean dataDateTimeToDateCalled = false
			boolean dataToYesNoCalled = false
			boolean dataToStringCalled = false
			boolean clearCustomFieldsCalled = false

			service.metaClass.dataDateToDateTime = { String fieldName -> dataDateToDateTimeCalled = true; return ""; }
			service.metaClass.dataDateTimeToDate = { String fieldName -> dataDateTimeToDateCalled = true; return ""; }
			service.metaClass.dataToYesNo = { String fieldName -> dataToYesNoCalled = true; return ""; }
			service.metaClass.dataToString = { String fieldName, Integer maxLength -> dataToStringCalled = true; return ""; }
			service.metaClass.anyTypeToNull = { String fieldName -> clearCustomFieldsCalled = true; return ""; }

		expect: 'when updateFieldData is called the flags are set appropriately'
			String updateString = service.updateFieldData(oldFieldSpec, newFieldSpec)
			dataDateToDateTimeCalled == dataDateToDateTimeSet
			dataDateTimeToDateCalled == dataDateTimeToDateSet
			dataToYesNoCalled == dataToYesNoSet
			dataToStringCalled == dataToStringSet
			clearCustomFieldsCalled == clearCustomFieldsSet

		where:
			oldFieldSpec                                              | newFieldSpec                                                       || dataDateToDateTimeSet | dataDateTimeToDateSet | dataToYesNoSet | dataToStringSet | clearCustomFieldsSet
			[field: 'testField', control: ControlType.DATE.value]     | [control: ControlType.DATETIME.value, constraints: [maxSize: 255]] || true                  | false                 | false          | false           | false

			[field: 'testField', control: ControlType.DATETIME.value] | [control: ControlType.DATE.value, constraints: [maxSize: 255]]     || false                 | true                  | false          | false           | false

			[field: 'testField', control: ControlType.STRING.value]   | [control: ControlType.YES_NO.value, constraints: [maxSize: 255]]   || false                 | false                 | true           | false           | false
			[field: 'testField', control: ControlType.LIST.value]     | [control: ControlType.YES_NO.value, constraints: [maxSize: 255]]   || false                 | false                 | true           | false           | false

			[field: 'testField', control: ControlType.NUMBER.value]   | [control: ControlType.LIST.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | false
			[field: 'testField', control: ControlType.STRING.value]   | [control: ControlType.LIST.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | false
			[field: 'testField', control: ControlType.YES_NO.value]   | [control: ControlType.LIST.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | false
			[field: 'testField', control: ControlType.DATETIME.value] | [control: ControlType.LIST.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | false
			[field: 'testField', control: ControlType.DATE.value]     | [control: ControlType.LIST.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | false

			[field: 'testField', control: ControlType.NUMBER.value]   | [control: ControlType.STRING.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | true            | false
			[field: 'testField', control: ControlType.YES_NO.value]   | [control: ControlType.STRING.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | true            | false
			[field: 'testField', control: ControlType.DATE.value]     | [control: ControlType.STRING.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | true            | false
			[field: 'testField', control: ControlType.DATETIME.value] | [control: ControlType.STRING.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | true            | false
			[field: 'testField', control: ControlType.LIST.value]     | [control: ControlType.STRING.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | true            | false

			[field: 'testField', control: ControlType.NUMBER.value]   | [control: ControlType.DATETIME.value, constraints: [maxSize: 255]] || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.YES_NO.value]   | [control: ControlType.DATETIME.value, constraints: [maxSize: 255]] || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.LIST.value]     | [control: ControlType.DATETIME.value, constraints: [maxSize: 255]] || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.STRING.value]   | [control: ControlType.DATETIME.value, constraints: [maxSize: 255]] || false                 | false                 | false          | false           | true

			[field: 'testField', control: ControlType.NUMBER.value]   | [control: ControlType.DATE.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.YES_NO.value]   | [control: ControlType.DATE.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.LIST.value]     | [control: ControlType.DATE.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.STRING.value]   | [control: ControlType.DATE.value, constraints: [maxSize: 255]]     || false                 | false                 | false          | false           | true

			[field: 'testField', control: ControlType.NUMBER.value]   | [control: ControlType.YES_NO.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.DATE.value]     | [control: ControlType.YES_NO.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | false           | true
			[field: 'testField', control: ControlType.DATETIME.value] | [control: ControlType.YES_NO.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | false           | true

			[field: 'testField', control: ControlType.STRING.value]   | [control: ControlType.NUMBER.value, constraints: [maxSize: 255]]   || false                 | false                 | false          | false           | true
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
