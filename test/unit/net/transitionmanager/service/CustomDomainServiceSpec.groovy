package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.StringUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import net.transitionmanager.domain.Timezone
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(CustomDomainService)
@Mock([Application, Project, Setting, SettingService])
class CustomDomainServiceSpec extends Specification {

	@Shared
	Project defaultProject
	@Shared
	FieldSpecProject fieldSpecProject

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
		defaultProject.save(failOnError: true)

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
			setting.save(failOnError: true)
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
}