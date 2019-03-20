package net.transitionmanager.bulk.change


import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.NumberUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.InvalidParamException
import org.apache.commons.lang3.RandomStringUtils
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.ProjectTestHelper

@Integration
@Rollback
class BulkChangeNumberIntegrationSpec extends Specification {

	@Autowired
	CustomDomainService customDomainService

	@Shared
	AssetEntityTestHelper assetEntityTestHelper

	@Shared
	MoveBundleTestHelper moveBundleTestHelper

	@Shared
	ProjectTestHelper projectTestHelper

	@Shared
	Project project

	@Shared
	Project otherProject

	/**
	 * A move bundle that is usedForPlanning = 1
	 */
	@Shared
	MoveBundle moveBundle

	/**
	 * A move bundle that is usedForPlanning = 0
	 */
	@Shared
	MoveBundle moveBundle2

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device

	@Shared
	boolean initialized = false

	// Note that this JSON file is managed by the /misc/generateDomainFieldSpecs.groovy script
	// After generating the file it needs to be copied to the /grails-app/conf/ directory so it can be read
	// as a resource for the device.
	private static final String fieldSpecDefinitionJson = '/CustomDomainServiceTests_FieldSpec.json'

	/**
	 * This will load the JSON file that accompanies this test suite and will return
	 * it as a JSONObject.
	 */
	private JSONObject loadFieldSpecJson() {
		// Determine where we can write the resource file for testing
		// this.class.classLoader.rootLoader.URLs.each{ println it }
		// We'll only load it the first time and cache it into fieldSpecJson
		JSONObject fieldSpecJson
		if (!fieldSpecJson) {
			String jsonText = this.getClass().getResource(fieldSpecDefinitionJson).text
			fieldSpecJson = new JSONObject(jsonText)
			assert fieldSpecJson
			// println "\n\n$fieldSpecJson\n\n"
		}
		return fieldSpecJson
	}


	void setup() {
		assetEntityTestHelper = new AssetEntityTestHelper()
		moveBundleTestHelper = new MoveBundleTestHelper()
		projectTestHelper = new ProjectTestHelper()

		project = projectTestHelper.createProject()
		otherProject = projectTestHelper.createProject()

		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		Project project = projectTestHelper.createProjectWithDefaultBundle()
		JSONObject fieldSpec = loadFieldSpecJson()
		Setting.findByProject(project).delete(flush:true)
		customDomainService.saveFieldSpecs(project, CustomDomainService.ALL_ASSET_CLASSES, fieldSpec)
	}


	void 'Test clear'() {
		when: 'clear is called with a list of assets'
			BulkChangeNumber.clear(AssetEntity.class, null, 'custom8', [device.id], null)

		then: 'the bulkClear function is invoked and specified field on assets will be null out'
			[device].each {
				it.refresh()
				null == it.custom8
			}
	}


	void 'Test replace'() {
		given:
			Number testNumber

		when: 'replace is called with a random integer, and a list of assets'
			testNumber = NumberUtil.toPositiveInteger(RandomStringUtils.randomNumeric(5))
			BulkChangeNumber.replace(AssetEntity.class, testNumber, 'custom8', [device.id], null)

		then: 'the bulkReplace function is invoked and specified field and assets will be updated'
			[device].each {
				it.refresh()
				testNumber == it.custom8
			}
		when: 'replace is called with an integer and, list of assets'
			testNumber = 15644321
			BulkChangeNumber.replace(AssetEntity.class, testNumber, 'custom8', [device.id], null)

		then: 'the bulkReplace function is invoked and specified field and assets will be updated'
			[device].each {
				it.refresh()
				testNumber == it.custom8
			}

		when: 'replace is called with a decimal number and, list of assets'
			testNumber = 1.59876864
			BulkChangeNumber.replace(AssetEntity.class, testNumber, 'custom8', [device.id], null)

		then: 'the bulkReplace function is invoked and specified field and assets will be updated'
			[device].each {
				it.refresh()
				testNumber == it.custom8
			}

		when: 'replace is called with a negative number, and a list of assets'
			testNumber = -126.321
			BulkChangeNumber.replace(AssetEntity.class, testNumber, 'custom8', [device.id], null)

		then: 'the bulkReplace function is invoked and specified field and assets will be updated'
			[device].each {
				it.refresh()
				testNumber == it.custom8
			}

		when: 'replace is called with a null replacement value'
			BulkChangeNumber.replace(AssetEntity.class, null, 'custom8', [device.id], null)

		then: 'an InvalidParamException is thrown'
			thrown InvalidParamException
	}
}
