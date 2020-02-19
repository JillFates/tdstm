package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.action.Provider
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.BulkAssetETLService
import net.transitionmanager.command.bulk.BulkETLCommand
import net.transitionmanager.command.dataview.DataviewUserParamsCommand
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.common.ProgressService
import net.transitionmanager.common.Setting
import net.transitionmanager.imports.DataScript
import net.transitionmanager.imports.Dataview
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.security.SecurityService
import net.transitionmanager.security.UserLogin
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.DataviewTestHelper
import test.helper.PersonTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper

@Integration
@Rollback
class BulkAssetETLServiceIntegrationSpec extends Specification {
	@Autowired
	BulkAssetETLService bulkAssetETLService

	@Autowired
	CustomDomainService customDomainService

	@Autowired
	ProgressService progressService

	@Autowired
	SecurityService securityService

	@Autowired
	UserPreferenceService userPreferenceService

	@Shared
	DataviewTestHelper dataviewTestHelper

	@Shared
	AssetEntityTestHelper assetEntityTestHelper

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper

	@Shared
	ProjectTestHelper projectTestHelper

	@Shared
	ProviderTestHelper providerTestHelper

	@Shared
	PersonTestHelper personHelper

	@Shared
	Project project

	@Shared
	UserLogin userLogin

	/**
	 * A move bundle that is usedForPlanning = 1
	 */
	@Shared
	MoveBundle moveBundle

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device2

	@Shared
	Date now

	@Shared
	DataviewUserParamsCommand dataviewUserParamsCommand

	@Shared
	BulkETLCommand bulkETLCommand

	@Shared
	Person person

	void setup() {
		assetEntityTestHelper = new AssetEntityTestHelper()
		moveBundleTestHelper = new test.helper.MoveBundleTestHelper()
		projectTestHelper = new test.helper.ProjectTestHelper()
		personHelper = new PersonTestHelper()
		providerTestHelper = new ProviderTestHelper()
		dataviewTestHelper = new DataviewTestHelper()
		JSONObject fieldSpec
		Provider provider
		Dataview dataView

		Project.withNewTransaction {
			project = projectTestHelper.createProject()

			provider = providerTestHelper.createProvider(project)

			moveBundle = moveBundleTestHelper.createBundle(project, null)

			device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

			now = TimeUtil.nowGMT().clearTime()

			dataviewUserParamsCommand = new DataviewUserParamsCommand([filters: [domains: []], sortDomain: 'device', sortProperty: 'id'])


			person = personHelper.createPerson(null, project.client, project)
			userLogin = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ROLE_ADMIN}"], project, true)
			securityService.assumeUserIdentity(userLogin.username, false)
			userPreferenceService.setCurrentProjectId(project.id)

			dataView = dataviewTestHelper.createDataview(project)
			dataView.reportSchema = Dataview.get(1).reportSchema
			dataView.save(flush:true, failOnError:true)

			fieldSpec = loadFieldSpecJson()

			Setting.findAllByProject(project)*.delete(flush: true)
		}

		customDomainService.saveFieldSpecs(project, CustomDomainService.ALL_ASSET_CLASSES, fieldSpec)

		DataScript dataScript = new DataScript(
			name: 'test etl',
			description: 'test etl',
			isAutoProcess: true,
			useWithAssetActions: true,
			createdBy: person,
			lastModifiedBy: person,
			project: project,
			provider: provider,
			etlSourceCode: '''
					read labels
					domain Device
					iterate {
						extract 'Id' load 'Id' set Id
						extract 'Name' transform with uppercase() load 'description'
						find Application by 'Id' eq Id into 'Id'
					}
				'''
		).save(flush: true, failOnError: true)

		bulkETLCommand = new BulkETLCommand(
			userParams: dataviewUserParamsCommand,
			dataViewId: dataView.id,
			dataScriptId: dataScript.id,
			ids: [device.id, device2.id]
		)
	}


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

	void 'Test runBulkETL'() {

		when: 'calling runBulkETL, with asset ids  and waiting for the process to complete'
			String progressKey = bulkAssetETLService.runBulkETL(project, bulkETLCommand)?.progressKey
			boolean checkProgress = true
			Map progress

			while (checkProgress) {
				progress = progressService.get(progressKey)

				if (!progress) {
					throw new RuntimeException('ETL failed to start and have progress.')
				}


				println progress
				if (progress && progress.status.toUpperCase() == progressService.COMPLETED.toUpperCase()) {
					checkProgress = false
					//TODO I wanted to check based on the progress but if I don't put a delay in even after the progress is complete, then the values haven't updated yet...
					sleep(3000)
					break //ETL done
				}


				if (progress && progress.status.toUpperCase() == progressService.FAILED.toUpperCase()) {
					throw new RuntimeException("$progress.status, $progress.detail, $progress.data")
				}

				sleep(500)
			}
			AssetEntity updatedDevice1
			AssetEntity updatedDevice2

			AssetEntity.withNewTransaction {
				updatedDevice1 = AssetEntity.get(device.id)
				updatedDevice2 = AssetEntity.get(device2.id)

			}

		then: 'The assets descriptions are updated per the etl script'
			bulkETLCommand.validate()
			updatedDevice1.description == updatedDevice1.assetName.toUpperCase()
			updatedDevice2.description == updatedDevice2.assetName.toUpperCase()

	}

	void 'Test runBulkETL all assets in filter'() {
		setup: 'given an edit command for adding tags, where allAssets it true, and a bulk change command holding the edit'

			bulkETLCommand.ids = []
			bulkETLCommand.allIds = true

		when: 'calling runBulkETL, with a filter and waiting for the process to complete'
			String progressKey = bulkAssetETLService.runBulkETL(project, bulkETLCommand)?.progressKey
			boolean checkProgress = true

			while (checkProgress) {
				Map progress = progressService.get(progressKey)

				if (progress && progress.status.toUpperCase() == progressService.COMPLETED.toUpperCase()) {
					//TODO I wanted to check based on the progress but if I don't put a delay in even after the progress is complete, then the values haven't updated yet...
					sleep(3000)
					break //ETL done
				}

				if (!progress) {
					throw new RuntimeException('ETL failed to start and have progress.')
				}

				if (progress && progress.status.toUpperCase() == progressService.FAILED.toUpperCase()) {
					throw new RuntimeException("$progress.status, $progress.detail, $progress.data")
				}

				sleep(500)
			}

			AssetEntity updatedDevice1
			AssetEntity updatedDevice2

			AssetEntity.withNewTransaction {
				updatedDevice1 = AssetEntity.get(device.id)
				updatedDevice2 = AssetEntity.get(device2.id)
			}


		then: 'The assets descriptions are updated per the etl script'
			bulkETLCommand.validate()
			updatedDevice1.description == updatedDevice1.assetName.toUpperCase()
			updatedDevice2.description == updatedDevice2.assetName.toUpperCase()
	}

}
